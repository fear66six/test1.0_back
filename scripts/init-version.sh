#!/bin/bash

# ByrSki 版本初始化脚本
# 用于项目首次设置版本标签

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}ByrSki 版本管理初始化${NC}"
echo ""

# 检查是否在Git仓库中
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo -e "${RED}错误: 当前目录不是Git仓库${NC}"
    exit 1
fi

# 检查是否有现有标签
existing_tags=$(git tag -l)
if [ -n "$existing_tags" ]; then
    echo -e "${YELLOW}发现现有标签:${NC}"
    echo "$existing_tags"
    echo ""
    read -p "是否要创建新标签? (y/N): " continue_create
    if [[ ! "$continue_create" =~ ^[Yy]$ ]]; then
        echo "操作取消"
        exit 0
    fi
else
    echo -e "${YELLOW}未发现现有版本标签，建议创建初始版本${NC}"
fi

# 检查工作目录状态
if ! git diff-index --quiet HEAD --; then
    echo -e "${RED}错误: 工作目录有未提交的更改${NC}"
    echo "请先提交所有更改后再创建版本标签"
    git status --porcelain
    exit 1
fi

echo ""
echo -e "${YELLOW}版本号建议:${NC}"
echo "  初始版本: v1.0.0"
echo "  补丁版本: v1.0.1 (bug修复)"
echo "  小版本: v1.1.0 (新功能)"
echo "  大版本: v2.0.0 (重大更改)"
echo ""

# 输入版本号
while true; do
    read -p "请输入版本号 (格式: v1.0.0): " version
    
    if [ -z "$version" ]; then
        echo -e "${RED}版本号不能为空${NC}"
        continue
    fi
    
    if ! echo "$version" | grep -qE '^v[0-9]+\.[0-9]+\.[0-9]+$'; then
        echo -e "${RED}版本号格式不正确，请使用 v1.2.3 格式${NC}"
        continue
    fi
    
    if git tag -l | grep -q "$version"; then
        echo -e "${RED}标签 $version 已存在${NC}"
        continue
    fi
    
    break
done

# 确认创建
echo ""
echo -e "${YELLOW}准备创建版本标签: $version${NC}"
read -p "确认创建? (y/N): " confirm

if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo "操作取消"
    exit 0
fi

# 创建标签
echo -e "${YELLOW}正在创建标签...${NC}"
git tag -a "$version" -m "Release $version"

echo -e "${GREEN}✅ 版本标签创建成功: $version${NC}"
echo ""
echo -e "${YELLOW}下一步操作:${NC}"
echo "1. 推送标签到远程仓库:"
echo "   git push origin $version"
echo ""
echo "2. 查看当前版本信息:"
echo "   make version"
echo ""
echo "3. 构建发布版本:"
echo "   make build"
echo ""
echo "4. 部署到服务器:"
echo "   make deploy"

echo ""
echo -e "${GREEN}版本管理已初始化完成！${NC}" 