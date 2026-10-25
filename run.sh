#!/data/data/com.termux/files/usr/bin/bash
while true; do
clear
echo "=========================="
echo "        Qadge 工具集菜单      "
echo "=========================="
echo "1. 更新项目"
echo "0. 退出"
echo "=========================="
read -p "请输入选项：" opt
case $opt in
1)
echo "[+] 正在更新项目..."
git pull
read -p "回车返回菜单"
;;
0)
echo "[✓] 退出"
exit 0
;;
*)
echo "[!] 无效选项"
read -p "回车返回菜单"
;;
esac
done
