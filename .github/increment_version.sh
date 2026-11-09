#!/bin/bash

# 版本号自动递增工具 (适配独立模块模式)

cd "$(dirname "$0")/.."

# 颜色定义
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}[INFO]${NC} 读取当前版本号..."

# 从 Core POM 读取版本
current_version=$(mvn help:evaluate -f libra-toolkit-core/pom.xml -Dexpression=project.version -q -DforceStdout 2>/dev/null)

if [ -z "$current_version" ]; then
    echo -e "${YELLOW}[WARNING]${NC} 无法读取当前版本号"
    exit 1
fi

echo -e "${BLUE}[INFO]${NC} 当前版本: $current_version"

# 解析版本号
IFS='.' read -r major minor patch <<< "$current_version"
new_patch=$((patch + 1))
new_version="$major.$minor.$new_patch"

echo -e "${BLUE}[INFO]${NC} 新版本: $new_version"

update_pom() {
    local file=$1
    if [ -f "$file" ]; then
        echo -e "${BLUE}[INFO]${NC} 更新 $file..."
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s|<version>$current_version</version>|<version>$new_version</version>|g" "$file"
        else
            sed -i "s|<version>$current_version</version>|<version>$new_version</version>|g" "$file"
        fi
    fi
}

update_pom "libra-toolkit-core/pom.xml"
update_pom "libra-toolkit-demo/pom.xml"
update_pom "pom.xml"

echo -e "${GREEN}[SUCCESS]${NC} 版本号已更新: $current_version -> $new_version"
echo "$new_version"
