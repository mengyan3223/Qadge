import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// 签名口令不入库：本地从 local.properties（已 gitignore）读取；CI 从环境变量读取。
// 顺序：local.properties -> 环境变量；三者齐全且 keystore 存在才启用 release 签名，否则回退 debug 签名出包。
val signingProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingProp(propKey: String, envKey: String): String? =
    signingProps.getProperty(propKey) ?: System.getenv(envKey)

android {
    namespace = "me.lengyu.qedge"
    compileSdk = 37

    defaultConfig {
        applicationId = "me.lengyu.qedge"
        minSdk = 29
        targetSdk = 37
        versionCode = 32
        versionName = "0.3.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // 仅保留 64 位 arm64，去掉 v7a 减小体积；32 位设备在 XposedEntry 处拦截提示
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        val keystoreFile = file("qedge.jks").takeIf { it.exists() }
            ?: System.getenv("QEDGE_KEYSTORE")?.let { rootProject.file(it) }
        val storePass = signingProp("qedge.storePassword", "QEDGE_STORE_PASSWORD")
        val keyAlias = signingProp("qedge.keyAlias", "QEDGE_KEY_ALIAS")
        val keyPass = signingProp("qedge.keyPassword", "QEDGE_KEY_PASSWORD")
        if (keystoreFile != null && storePass != null && keyAlias != null && keyPass != null) {
            create("release") {
                storeFile = keystoreFile
                storePassword = storePass
                this.keyAlias = keyAlias
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // 有正式签名配置就用，否则回退 debug 签名，保证无密钥环境也能出可安装包
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            // 使用 AGP 默认 debug 签名
        }
    }

    androidResources {
        additionalParameters += listOf("--allow-reserved-package-id", "--package-id", "0x69")
        // 资源表(arsc)只保留中文，其他语言不打包
        localeFilters += listOf("zh")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        // AGP 默认以 STORED（不压缩）方式把 dex/so 放进 APK，导致安装包体积远大于 zip 结果。
        // 改为压缩入库后体积可降到约一半，代价是安装时需解压，安装耗时略增。
        dex {
            useLegacyPackaging = true
        }
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // 去除依赖 jar 打进来的无用资源：Rhino 调试/工具(test.js 等)、protobuf 源描述文本
            excludes += "org/mozilla/javascript/tools/**"
            excludes += "google/protobuf/**"
            // Rhino 只内置了英文与法语两套消息，模块面向中文用户，法语文案用不到
            excludes += "org/mozilla/javascript/resources/Messages_fr.properties"
            // 依赖 jar 的版本标记与 LICENSE 副本，与安装包运行无关
            excludes += "META-INF/*.version"
            excludes += "META-INF/androidx/**/LICENSE.txt"
            // kotlinx-coroutines 自带的协程调试探针元数据，release 用不到，纯冗余
            excludes += "DebugProbesKt.bin"
        }
    }
}

dependencies {
    // androidx
    implementation(libs.androidx.core.ktx)
    compileOnly(libs.androidx.savedstate)
    // compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    // ui
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    // material3
    implementation(libs.androidx.material3)
    // lifecycle
    compileOnly(libs.androidx.lifecycle.runtime.ktx)
    compileOnly(libs.androidx.lifecycle.viewmodel.ktx)
    compileOnly(libs.androidx.lifecycle.common.java8)
    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // dexkit
    implementation(libs.dexkit)
    // protobuf
    implementation(libs.protobuf.javalite)
    // legacy xposed api
    compileOnly(libs.xposed)
    // dx
    implementation(libs.dalvik.dx)
    // rhino js engine
    implementation(libs.rhino)
    // qq stub
    compileOnly(project(":qqinterface"))
}


val adb: String = androidComponents.sdkComponents.adb.get().asFile.absolutePath
val packageName = "com.tencent.mobileqq"
val killQQ = tasks.register<Exec>("killQQ") {
    group = "adb"
    description = "杀死QQ主进程"
    commandLine(adb, "shell", "am", "force-stop", packageName)
    isIgnoreExitValue = true
}

val openQQ = tasks.register<Exec>("openQQ") {
    group = "adb"
    description = "打开QQ"
    commandLine(adb, "shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
}

val restartQQ = tasks.register<Exec>("restartQQ") {
    group = "adb"
    description = "重新启动QQ"
    dependsOn(killQQ)
    commandLine(adb, "shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
}

val installDebugAndRestartQQ = tasks.register("installDebugAndRestartQQ") {
    group = "adb"
    description = "安装Debug版本并重启QQ"
    dependsOn(":app:installDebug")
    finalizedBy(restartQQ)
}