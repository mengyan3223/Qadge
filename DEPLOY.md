# Qadge 部署说明（GitHub Actions 出包 + LSPosed 加载）

## 1. 环境要求

| 项 | 要求 |
|----|------|
| JDK | 17（AGP 9.1.1 最低 17） |
| Android SDK | Platform 37、Build Tools 36.0.0 |
| Gradle | 9.5.0（wrapper 自带，无需单独安装） |
| 手机系统 | Android 10 ~ 16，arm64-v8a（32 位设备不支持） |
| Xposed 框架 | LSPosed / LSPatch / FPA / 原子 / 无极（Zygisk） |
| 宿主 | NT QQ 8.9.58 ~ 9.3.xx，或 NT TIM 3.9.0 ~ 4.1.0 |

## 2. 推送到你自己的 GitHub

本包已排除 `.git`，解压后是一份干净的二开工程，二选一即可。

### 方式 A：新建空仓库（推荐，品牌已全部改为 Qadge）

1. GitHub 新建空仓库（不要勾选 README / .gitignore / LICENSE）。
2. 解压本 zip，进入 `Qadge-main` 目录，初始化并关联远端：

```bash
cd Qadge-main
git init
git remote add origin https://github.com/<你的用户名>/<新仓库名>.git
git branch -M master
git add -A
git commit -m "init: Qadge 二开工程（基于 QEdge，品牌改为 Qadge，作者梦魇）"
git push -u origin master
```

### 方式 B：先 Fork 作者仓库再覆盖

1. 网页 Fork 作者仓库到你账号下并克隆到本地。
2. 解压本 zip，把 `Qadge-main/` 内全部文件（含隐藏目录 `.github` 和 `.gitignore`）覆盖到克隆目录。
3. 提交推送：

```bash
git add -A
git commit -m "chore: 品牌改为 Qadge，作者改为梦魇，适配 Actions 自动出包"
git push
```

## 3. 签名（可选，不配也能出包）

不配置 Secrets 时，CI 自动用 debug 签名出包，LSPosed 模块对签名没有强制要求，可直接安装。

需要正式签名时，在仓库 **Settings → Secrets and variables → Actions** 添加：

| Secret 名 | 内容 |
|-----------|------|
| `KEYSTORE_BASE64` | `base64 -w0 app/qedge.jks` 输出的整段内容 |
| `STORE_PASSWORD` | keystore 口令 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥口令 |

本地开发签名：把 `qedge.jks` 放到 `app/` 目录，在仓库根目录新建 `local.properties`（已被 gitignore，不会上传）：

```properties
qedge.storePassword=xxx
qedge.keyAlias=xxx
qedge.keyPassword=xxx
```

## 4. 出包方式

### 4.1 GitHub Actions 自动出包（已配好 `.github/workflows/build.yml`）

- **push 到 `master` / `main`**：自动构建 debug 包，在 Actions 页 **Artifacts** 里下载 `Qadge-debug`。
- **打 tag 推送**：自动构建 release 包并创建 GitHub Release，APK 直接挂在 Release 页：

```bash
git tag v0.3.3
git push origin v0.3.3
```

- **手动触发**：Actions 页选 `Build Qadge Module APK` → `Run workflow`。

### 4.2 本地命令构建

```bash
./gradlew :app:assembleDebug      # debug 包：app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease    # release 包：app/build/outputs/apk/release/app-release.apk
```

## 5. LSPosed 安装启用

1. 把 APK 装到手机。
2. 打开 LSPosed → **模块** → 找到 Qadge → 打开启用开关。
3. **作用域**勾选：QQ（`com.tencent.mobileqq`）、TIM（`com.tencent.tim`）；其它第三方 APP（KK 键盘、酷狗大字版/概念版、傲软抠图、讯飞输入法、设备信息X、无痛单词、木函）按需勾选。
4. 强制停止 QQ / TIM 后重新打开。
5. 首次进入会弹 DexKit 查找进度窗，扫描完成点确定杀进程，再次打开 QQ 即注入成功。
6. 入口：QQ 首页下拉 → QQ Plus → Qadge 设置；或桌面 Qadge 图标。

## 6. 常见问题

- **模块不生效**：先确认作用域勾了对应宿主、已强制停止并重启 QQ；版本不在适配区间内时 DexKit 定位不到方法，Hook 会失效。
- **DexKit 缓存缺失标红**：模块配置页点「重建 DexKit 缓存」，完成后重启 QQ。
- **32 位设备**：工程只打包 `arm64-v8a`，32 位会在入口处被拦截并提示。
- **release 包 R8 报错**：`app/proguard-rules.pro` 已配置 `dontobfuscate` 与 `keep me.lengyu.qedge.**`，新增 Hook 类时保持在 `me.lengyu.qedge` 包名下即可。

## 7. 风险提示

本项目仅供学习交流。防撤回、秒赞、自动加好友、本地解锁会员等功能可能违反腾讯用户协议，存在账号封禁风险；第三方 APP 去广告/解锁会员功能亦可能违反对应应用条款，后果自负。
