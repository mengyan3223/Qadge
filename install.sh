#!/data/data/com.termux/files/usr/bin/bash
clear
echo "====================================="
echo "          Qadge 工具集安装脚本        "
echo "====================================="
echo ""

# 检查git
if ! command -v git &> /dev/null; then
echo "[+] 正在安装 git..."
pkg update -y
pkg install git -y
fi

# 仓库地址
REPO_URL="https://github.com/mengyan3223/Qadge.git"
DIR_NAME="Qadge"

if [ -d "$DIR_NAME" ];then
echo "[!] 文件夹已存在，执行更新"
cd "$DIR_NAME"
git pull
else
echo "[+] 正在拉取项目..."
git clone "$REPO_URL"
cd "$DIR_NAME"
fi

echo ""
echo "[✅] Qadge 工具集安装/更新完成！"
echo "[💡] 进入目录：cd Qadge"
echo "====================================="
