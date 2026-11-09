#!/bin/bash
# ========================================================================
# start.sh - Libra Toolkit 发布入口脚本
# ========================================================================
# 功能说明：
#   提供交互式菜单或直接参数调用，选择执行不同的发布流程
#
# 用法：
#   ./start.sh       -> 进入交互菜单（默认选项 1）
#   ./start.sh 1     -> 直接执行普通发布
#   ./start.sh 2     -> 直接执行混淆发布
# ========================================================================

# 自动修复权限
if [ ! -x "$0" ]; then
    chmod +x "$0"
    exec "$0" "$@"
fi

# 获取脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BIN_DIR="$SCRIPT_DIR/.github"

# 颜色定义
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 执行动作函数
invoke_action() {
    local option=$1
    case "$option" in
        1)
            echo -e "\n${YELLOW}正在启动普通发布流程...${NC}"
            bash "$BIN_DIR/public.sh"
            return $?
            ;;
        2)
            echo -e "\n${YELLOW}正在启动混淆发布流程...${NC}"
            bash "$BIN_DIR/public_obfuscated.sh"
            return $?
            ;;
        0)
            echo "退出..."
            exit 0
            ;;
        *)
            echo -e "${RED}无效选项: $option${NC}"
            return 1
            ;;
    esac
}

# 模式 1：命令行参数模式
if [ -n "$1" ]; then
    invoke_action "$1"
    exit $?
fi

# 模式 2：交互模式
while true; do
    clear
    echo -e "${CYAN}========================================${NC}"
    echo -e "${CYAN}      Libra Toolkit - 发布菜单${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo -e "${GREEN}1. 普通发布 (Public Release)${NC}"
    echo "   - 自动递增版本"
    echo "   - 编译并发布到 GitHub"
    echo ""
    echo -e "${GREEN}2. 混淆发布 (Obfuscated Release)${NC}"
    echo "   - 自动递增版本"
    echo "   - 执行 ProGuard 混淆"
    echo "   - 发布混淆包到 GitHub"
    echo ""
    echo -e "${YELLOW}0. 退出${NC}"
    echo -e "${CYAN}========================================${NC}"

    printf "请输入选项 (默认: 1): "
    read choice

    # 默认值处理
    choice=${choice:-1}

    invoke_action "$choice"
    ret=$?

    if [ $ret -eq 0 ]; then
        echo ""
        read -p "按 Enter 返回菜单..." dummy
    else
        echo ""
        echo -e "${RED}执行出错 ($ret)，按 Enter 返回...${NC}"
        read dummy
    fi
done
