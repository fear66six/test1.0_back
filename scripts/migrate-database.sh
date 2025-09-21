#!/bin/bash

# ByrSki 数据库迁移脚本 v2.0
# 功能：从后端服务器迁移 MySQL 和 MongoDB 数据到数据库服务器
# 作者：ByrSki Team
# 更新日期：2025-07-23
# 修复：MongoDB容器内外路径问题，增强错误处理和验证

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# 获取环境变量
if [ -f "../.env" ]; then
    source ../.env
elif [ -f ".env" ]; then
    source .env
else
    echo -e "${RED}错误: 未找到 .env 文件${NC}"
    exit 1
fi

# 配置变量
SOURCE_SERVER=${BACKEND_SERVER_IP}  # 源服务器（后端服务器）
TARGET_SERVER=${DATABASE_SERVER_IP}  # 目标服务器（数据库服务器）

# SSH选项
SSH_OPTS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o ConnectTimeout=30"

# 数据库配置
MYSQL_USER="wsb"
MYSQL_PASSWORD="wang13261910095"
MYSQL_ROOT_PASSWORD="root"
MYSQL_DATABASE="byrski_schema"

MONGO_USER="byrski_admin"
MONGO_PASSWORD="byrski20241228"
MONGO_DATABASE="byrski"

# 备份目录
BACKUP_DIR="/tmp/byrski_migration_$(date +%Y%m%d_%H%M%S)"
MYSQL_BACKUP_FILE="byrski_mysql_backup.sql"
MONGO_BACKUP_DIR="byrski_mongo_backup"

# 辅助函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查服务器连接
check_server_connection() {
    local server=$1
    local server_name=$2
    
    log_info "检查 ${server_name} 服务器连接: ${server}"
    
    if ssh $SSH_OPTS root@${server} "echo 'Connection successful'" >/dev/null 2>&1; then
        log_success "${server_name} 服务器连接正常"
    else
        log_error "${server_name} 服务器连接失败: ${server}"
        return 1
    fi
}

# 检查Docker服务状态
check_docker_services() {
    local server=$1
    local server_name=$2
    
    log_info "检查 ${server_name} 的 Docker 服务状态"
    
    # 检查MySQL容器
    if ssh $SSH_OPTS root@${server} "docker ps --format 'table {{.Names}}\t{{.Status}}' | grep mysql" >/dev/null 2>&1; then
        local mysql_status=$(ssh $SSH_OPTS root@${server} "docker ps --format 'table {{.Names}}\t{{.Status}}' | grep mysql")
        log_success "MySQL 容器状态: ${mysql_status}"
    else
        log_warning "${server_name} 上未发现运行中的 MySQL 容器"
    fi
    
    # 检查MongoDB容器
    if ssh $SSH_OPTS root@${server} "docker ps --format 'table {{.Names}}\t{{.Status}}' | grep mongo" >/dev/null 2>&1; then
        local mongo_status=$(ssh $SSH_OPTS root@${server} "docker ps --format 'table {{.Names}}\t{{.Status}}' | grep mongo")
        log_success "MongoDB 容器状态: ${mongo_status}"
    else
        log_warning "${server_name} 上未发现运行中的 MongoDB 容器"
    fi
}

# 创建备份目录
create_backup_directory() {
    log_info "在源服务器创建备份目录: ${BACKUP_DIR}"
    ssh $SSH_OPTS root@${SOURCE_SERVER} "mkdir -p ${BACKUP_DIR}"
    log_success "备份目录创建完成"
}

# 导出MySQL数据
export_mysql_data() {
    log_info "开始导出 MySQL 数据库..."
    
    # 检查MySQL容器
    local mysql_container=$(ssh $SSH_OPTS root@${SOURCE_SERVER} "docker ps --format '{{.Names}}' | grep mysql | head -1")
    if [ -z "$mysql_container" ]; then
        log_error "未找到运行中的 MySQL 容器"
        return 1
    fi
    
    log_info "使用 MySQL 容器: ${mysql_container}"
    
    # 导出数据库（添加 --single-transaction 确保数据一致性）
    ssh $SSH_OPTS root@${SOURCE_SERVER} "docker exec ${mysql_container} mysqldump -u${MYSQL_USER} -p${MYSQL_PASSWORD} --single-transaction --routines --triggers ${MYSQL_DATABASE} > ${BACKUP_DIR}/${MYSQL_BACKUP_FILE}"
    
    # 检查导出文件大小
    local file_size=$(ssh $SSH_OPTS root@${SOURCE_SERVER} "ls -lh ${BACKUP_DIR}/${MYSQL_BACKUP_FILE} | awk '{print \$5}'")
    log_success "MySQL 数据导出完成，文件大小: ${file_size}"
    
    # 验证SQL文件完整性
    local line_count=$(ssh $SSH_OPTS root@${SOURCE_SERVER} "wc -l < ${BACKUP_DIR}/${MYSQL_BACKUP_FILE}")
    if [ "$line_count" -gt 0 ]; then
        log_success "MySQL 备份文件验证通过，共 ${line_count} 行"
    else
        log_error "MySQL 备份文件可能损坏"
        return 1
    fi
}

# 导出MongoDB数据
export_mongodb_data() {
    log_info "开始导出 MongoDB 数据库..."
    
    # 检查MongoDB容器
    local mongo_container=$(ssh $SSH_OPTS root@${SOURCE_SERVER} "docker ps --format '{{.Names}}' | grep mongo | head -1")
    if [ -z "$mongo_container" ]; then
        log_error "未找到运行中的 MongoDB 容器"
        return 1
    fi
    
    log_info "使用 MongoDB 容器: ${mongo_container}"
    
    # 在容器内导出数据库
    log_info "在容器内导出 MongoDB 数据..."
    ssh $SSH_OPTS root@${SOURCE_SERVER} "docker exec ${mongo_container} mongodump --username=${MONGO_USER} --password=${MONGO_PASSWORD} --authenticationDatabase=admin --db=${MONGO_DATABASE} --out=/tmp/"
    
    # 从容器复制到主机
    log_info "从容器复制 MongoDB 备份到主机..."
    ssh $SSH_OPTS root@${SOURCE_SERVER} "docker cp ${mongo_container}:/tmp/${MONGO_DATABASE} ${BACKUP_DIR}/"
    
    # 检查导出目录大小
    local dir_size=$(ssh $SSH_OPTS root@${SOURCE_SERVER} "du -sh ${BACKUP_DIR}/${MONGO_DATABASE} | awk '{print \$1}'")
    log_success "MongoDB 数据导出完成，目录大小: ${dir_size}"
    
    # 验证MongoDB备份文件
    local bson_files=$(ssh $SSH_OPTS root@${SOURCE_SERVER} "find ${BACKUP_DIR}/${MONGO_DATABASE} -name '*.bson' | wc -l")
    if [ "$bson_files" -gt 0 ]; then
        log_success "MongoDB 备份验证通过，发现 ${bson_files} 个 BSON 文件"
    else
        log_error "MongoDB 备份文件可能丢失"
        return 1
    fi
}

# 传输数据到目标服务器
transfer_data() {
    log_info "开始传输数据到目标服务器..."
    
    # 在目标服务器创建目录
    ssh $SSH_OPTS root@${TARGET_SERVER} "mkdir -p ${BACKUP_DIR}"
    
    # 传输MySQL备份文件
    log_info "传输 MySQL 备份文件..."
    ssh $SSH_OPTS root@${SOURCE_SERVER} "scp $SSH_OPTS ${BACKUP_DIR}/${MYSQL_BACKUP_FILE} root@${TARGET_SERVER}:${BACKUP_DIR}/"
    
    # 传输MongoDB备份目录
    log_info "传输 MongoDB 备份目录..."
    ssh $SSH_OPTS root@${SOURCE_SERVER} "scp $SSH_OPTS -r ${BACKUP_DIR}/${MONGO_DATABASE} root@${TARGET_SERVER}:${BACKUP_DIR}/"
    
    # 验证传输完成
    local mysql_transferred=$(ssh $SSH_OPTS root@${TARGET_SERVER} "ls -la ${BACKUP_DIR}/${MYSQL_BACKUP_FILE} 2>/dev/null && echo 'exists' || echo 'missing'")
    local mongo_transferred=$(ssh $SSH_OPTS root@${TARGET_SERVER} "ls -la ${BACKUP_DIR}/${MONGO_DATABASE} 2>/dev/null && echo 'exists' || echo 'missing'")
    
    if [[ "$mysql_transferred" == "exists" && "$mongo_transferred" == "exists" ]]; then
        log_success "数据传输完成并验证成功"
    else
        log_error "数据传输验证失败"
        return 1
    fi
}

# 导入MySQL数据
import_mysql_data() {
    log_info "开始导入 MySQL 数据..."
    
    # 检查目标服务器MySQL容器
    local mysql_container=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker ps --format '{{.Names}}' | grep mysql | head -1")
    if [ -z "$mysql_container" ]; then
        log_error "目标服务器未找到运行中的 MySQL 容器"
        return 1
    fi
    
    log_info "使用目标 MySQL 容器: ${mysql_container}"
    
    # 先确保数据库存在
    ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mysql_container} mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e 'CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};'"
    
    # 导入数据
    log_info "正在导入 MySQL 数据，请耐心等待..."
    ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec -i ${mysql_container} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} < ${BACKUP_DIR}/${MYSQL_BACKUP_FILE}"
    
    log_success "MySQL 数据导入完成"
}

# 导入MongoDB数据
import_mongodb_data() {
    log_info "开始导入 MongoDB 数据..."
    
    # 检查目标服务器MongoDB容器
    local mongo_container=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker ps --format '{{.Names}}' | grep mongo | head -1")
    if [ -z "$mongo_container" ]; then
        log_error "目标服务器未找到运行中的 MongoDB 容器"
        return 1
    fi
    
    log_info "使用目标 MongoDB 容器: ${mongo_container}"
    
    # 将备份复制到容器内
    log_info "复制 MongoDB 备份到容器内..."
    ssh $SSH_OPTS root@${TARGET_SERVER} "docker cp ${BACKUP_DIR}/${MONGO_DATABASE} ${mongo_container}:/tmp/"
    
    # 导入数据
    log_info "正在导入 MongoDB 数据..."
    ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mongo_container} mongorestore --username=${MONGO_USER} --password=${MONGO_PASSWORD} --authenticationDatabase=admin --db=${MONGO_DATABASE} /tmp/${MONGO_DATABASE}"
    
    log_success "MongoDB 数据导入完成"
}

# 验证数据迁移
verify_migration() {
    log_info "验证数据迁移结果..."
    
    # 验证MySQL
    log_info "验证 MySQL 数据..."
    local mysql_container=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker ps --format '{{.Names}}' | grep mysql | head -1")
    if [ ! -z "$mysql_container" ]; then
        local table_count=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mysql_container} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e 'SHOW TABLES;' 2>/dev/null | wc -l")
        table_count=$((table_count - 1))
        log_success "MySQL 表数量: ${table_count}"
        
        # 检查关键表的数据量
        local account_count=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mysql_container} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e 'SELECT COUNT(*) FROM account;' 2>/dev/null | tail -1")
        local activity_count=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mysql_container} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e 'SELECT COUNT(*) FROM activity;' 2>/dev/null | tail -1")
        local tickets_count=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mysql_container} mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} -e 'SELECT COUNT(*) FROM tickets;' 2>/dev/null | tail -1")
        
        log_success "关键数据验证 - 账户: ${account_count}, 活动: ${activity_count}, 票据: ${tickets_count}"
    fi
    
    # 验证MongoDB
    log_info "验证 MongoDB 数据..."
    local mongo_container=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker ps --format '{{.Names}}' | grep mongo | head -1")
    if [ ! -z "$mongo_container" ]; then
        local collection_count=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mongo_container} mongosh --username=${MONGO_USER} --password=${MONGO_PASSWORD} --authenticationDatabase=admin ${MONGO_DATABASE} --eval 'db.runCommand(\"listCollections\").cursor.firstBatch.length' --quiet")
        log_success "MongoDB 集合数量: ${collection_count}"
        
        # 检查具体集合的文档数量
        local products_count=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mongo_container} mongosh --username=${MONGO_USER} --password=${MONGO_PASSWORD} --authenticationDatabase=admin ${MONGO_DATABASE} --eval 'db.products.countDocuments()' --quiet")
        local docs_count=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${mongo_container} mongosh --username=${MONGO_USER} --password=${MONGO_PASSWORD} --authenticationDatabase=admin ${MONGO_DATABASE} --eval 'db.docs.countDocuments()' --quiet")
        
        log_success "MongoDB 数据验证 - 产品: ${products_count}, 文档: ${docs_count}"
    fi
}

# 清理临时文件
cleanup() {
    log_info "清理临时文件..."
    
    # 清理源服务器
    ssh $SSH_OPTS root@${SOURCE_SERVER} "rm -rf ${BACKUP_DIR}" 2>/dev/null || true
    
    # 清理目标服务器
    ssh $SSH_OPTS root@${TARGET_SERVER} "rm -rf ${BACKUP_DIR}" 2>/dev/null || true
    
    # 清理容器内临时文件
    local source_mongo_container=$(ssh $SSH_OPTS root@${SOURCE_SERVER} "docker ps --format '{{.Names}}' | grep mongo | head -1" 2>/dev/null || true)
    local target_mongo_container=$(ssh $SSH_OPTS root@${TARGET_SERVER} "docker ps --format '{{.Names}}' | grep mongo | head -1" 2>/dev/null || true)
    
    if [ ! -z "$source_mongo_container" ]; then
        ssh $SSH_OPTS root@${SOURCE_SERVER} "docker exec ${source_mongo_container} rm -rf /tmp/${MONGO_DATABASE}" 2>/dev/null || true
    fi
    
    if [ ! -z "$target_mongo_container" ]; then
        ssh $SSH_OPTS root@${TARGET_SERVER} "docker exec ${target_mongo_container} rm -rf /tmp/${MONGO_DATABASE}" 2>/dev/null || true
    fi
    
    log_success "临时文件清理完成"
}

# 主执行函数
main() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}    ByrSki 数据库迁移脚本${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo
    echo -e "${BLUE}源服务器:${NC} ${SOURCE_SERVER}"
    echo -e "${BLUE}目标服务器:${NC} ${TARGET_SERVER}"
    echo -e "${BLUE}备份目录:${NC} ${BACKUP_DIR}"
    echo
    
    # 检查连接
    check_server_connection ${SOURCE_SERVER} "源服务器"
    check_server_connection ${TARGET_SERVER} "目标服务器"
    
    # 检查Docker服务
    check_docker_services ${SOURCE_SERVER} "源服务器"
    check_docker_services ${TARGET_SERVER} "目标服务器"
    
    # 确认迁移
    echo
    echo -e "${YELLOW}注意：此操作将会覆盖目标服务器上的现有数据！${NC}"
    read -p "确认开始数据库迁移? (y/N): " confirm
    if [[ ! $confirm =~ ^[Yy]$ ]]; then
        log_warning "用户取消迁移"
        exit 0
    fi
    
    echo
    log_info "开始执行数据库迁移流程..."
    
    # 执行迁移步骤
    create_backup_directory
    
    export_mysql_data
    export_mongodb_data
    
    transfer_data
    
    import_mysql_data
    import_mongodb_data
    
    verify_migration
    
    cleanup
    
    echo
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}    数据库迁移完成！${NC}"
    echo -e "${GREEN}========================================${NC}"
    
    log_success "所有数据已成功从 ${SOURCE_SERVER} 迁移到 ${TARGET_SERVER}"
    log_info "请更新应用配置以连接到新的数据库服务器"
}

# 错误处理
trap 'log_error "脚本执行失败，正在清理..."; cleanup; exit 1' ERR

# 脚本使用帮助
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "ByrSki 数据库迁移脚本 v2.0"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  --dry-run      仅检查连接，不执行迁移"
    echo "  --verify-only  仅验证现有数据，不执行迁移"
    echo ""
    echo "环境变量:"
    echo "  BACKEND_SERVER_IP   源服务器IP (从.env文件读取)"
    echo "  DATABASE_SERVER_IP  目标服务器IP (从.env文件读取)"
    echo ""
    echo "更新说明:"
    echo "  v2.0 - 修复MongoDB容器内外路径问题"
    echo "       - 增强数据验证和错误处理"
    echo "       - 改进备份文件完整性检查"
    echo ""
    exit 0
fi

# Dry run 模式
if [[ "$1" == "--dry-run" ]]; then
    log_info "运行在检查模式，不会执行实际迁移"
    check_server_connection ${SOURCE_SERVER} "源服务器"
    check_server_connection ${TARGET_SERVER} "目标服务器"
    check_docker_services ${SOURCE_SERVER} "源服务器"
    check_docker_services ${TARGET_SERVER} "目标服务器"
    log_success "检查完成，所有服务正常"
    exit 0
fi

# 仅验证模式
if [[ "$1" == "--verify-only" ]]; then
    log_info "运行在验证模式，仅检查目标服务器数据"
    check_server_connection ${TARGET_SERVER} "目标服务器"
    verify_migration
    exit 0
fi

# 执行主函数
main 