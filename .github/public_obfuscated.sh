#!/bin/bash
# Libra Toolkit - 混淆发布脚本 (Shell)

cd "$(dirname "$0")/.."

# 颜色定义
BLUE='\033[0;34m'
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# 1. 自增版本
echo -e "${BLUE}[INFO]${NC} 自动递增版本..."
new_version=$(bash .github/increment_version.sh | tail -n 1)
if [ -z "$new_version" ]; then
    echo -e "${RED}[ERROR]${NC} 版本递增失败"
    exit 1
fi

current_branch=$(git rev-parse --abbrev-ref HEAD)

# 2. 全局构建
echo -e "${BLUE}[INFO]${NC} 构建并混淆..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR]${NC} 构建失败"
    exit 1
fi

# 验证混淆包
obfuscated_jar="libra-toolkit-core/target/libra-spring-boot-starter-toolkit-$new_version-obfuscated.jar"
if [ ! -f "$obfuscated_jar" ]; then
    echo -e "${RED}[ERROR]${NC} 未找到混淆包: $obfuscated_jar"
    exit 1
fi

# 3. 提交
echo -e "${BLUE}[INFO]${NC} 提交代码..."
git add .
if ! git diff --staged --quiet; then
    git commit -m "Release v$new_version (Obfuscated)"
    git push origin "$current_branch"
fi

# 4. 发布 Core
echo -e "${BLUE}[INFO]${NC} 发布 Core 模块..."
mvn deploy -f libra-toolkit-core/pom.xml -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR]${NC} 发布失败"
    exit 1
fi

# 5. Tag & Release
tag_name="v$new_version"
if ! git rev-parse "$tag_name" >/dev/null 2>&1; then
    git tag -a "$tag_name" -m "Release $new_version"
    git push origin "$tag_name"
fi

echo -e "${BLUE}[INFO]${NC} 创建 Release 并上传混淆包..."
gh release create "$tag_name" "$obfuscated_jar" --title "$tag_name" --notes "Release v$new_version (Obfuscated)" --target "$current_branch" --latest

echo -e "${GREEN}[SUCCESS]${NC} 混淆发布完成! ($new_version)"
