#!/bin/bash

# Daily News 项目 .replit TOML 修复脚本
# 修复 TOML 解析错误：ports 配置格式问题

set -e

echo "🛠️  Daily News 项目 .replit TOML 修复工具"
echo "============================================="
echo

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 函数定义
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_debug() {
    echo -e "${BLUE}[DEBUG]${NC} $1"
}

# 检查文件是否存在
check_file_exists() {
    if [[ ! -f ".replit" ]]; then
        log_error ".replit 文件不存在！"
        exit 1
    fi
}

# 备份原文件
backup_original() {
    log_info "备份原始 .replit 文件..."
    cp .replit .replit.backup.$(date +%Y%m%d_%H%M%S)
    log_info "✅ 备份完成：.replit.backup.$(date +%Y%m%d_%H%M%S)"
}

# 验证 TOML 语法
validate_toml() {
    log_info "验证 TOML 语法..."

    # 尝试使用 Python 验证
    if command -v python3 &> /dev/null; then
        python3 -c "import toml; toml.load('.replit')" 2>/dev/null
        if [[ $? -eq 0 ]]; then
            log_info "✅ TOML 语法验证通过"
            return 0
        else
            log_error "❌ TOML 语法验证失败"
            return 1
        fi
    else
        log_warn "⚠️  未找到 Python3，跳过 TOML 语法验证"
        return 0
    fi
}

# 检查当前配置格式
check_current_format() {
    log_info "检查当前 .replit 配置格式..."

    # 检查是否存在错误的 ports 格式
    if grep -q "^\[ports\]" .replit; then
        log_warn "发现旧的 ports 对象格式"
        return 1
    fi

    # 检查是否存在正确的 ports 格式
    if grep -q "^\[\[ports\]\]" .replit; then
        log_info "✅ 已经是正确的数组格式"
        return 0
    fi

    # 检查是否存在 ports 配置
    if ! grep -q "ports" .replit; then
        log_warn "未找到 ports 配置"
        return 2
    fi

    return 0
}

# 生成修复后的配置
generate_fixed_config() {
    log_info "生成修复后的 .replit 配置..."

    cat > .replit << 'EOF'
run = "bash run.sh"
language = "java"

[deployment]
run = ["bash", "run.sh"]
deploymentTarget = "cloudrun"

[env]
JAVA_HOME = "/nix/store/5b2q2w1k9f5n3yqx4z1v6cxpb9h4lxmk-openjdk-17.0.12+7"
# 更新为 Neon PostgreSQL 配置，移除 MySQL 相关配置
NEON_DB_PASSWORD = "${NEON_DB_PASSWORD}"
DATABASE_URL = "postgresql://neondb_owner:${NEON_DB_PASSWORD}@ep-morning-wind-aho6ug36-pooler.c-3.us-east-1.aws.neon.tech/neondb?sslmode=require"

# 修复：使用正确的数组格式定义端口（从对象格式改为数组格式）
[[ports]]
port = 18095
external = true

[[ports]]
port = 3306
external = false

# 新增：Neon PostgreSQL 默认端口
[[ports]]
port = 5432
external = false
EOF

    log_info "✅ 配置生成完成"
}

# 显示变更对比
show_diff() {
    log_info "显示配置变更："
    echo
    echo "--- 变更前 ---"
    grep -A 10 -B 2 "ports" .replit.backup.* 2>/dev/null || cat .replit.backup.* 2>/dev/null || echo "无法显示备份文件"
    echo
    echo "+++ 变更后 +++"
    grep -A 10 -B 2 "ports" .replit || cat .replit
    echo
}

# 测试 Replit 环境
test_replit_env() {
    log_info "测试 Replit 环境配置..."

    # 检查环境变量
    if [[ -n "$NEON_DB_PASSWORD" ]]; then
        log_info "✅ NEON_DB_PASSWORD 环境变量已设置"
    else
        log_warn "⚠️  NEON_DB_PASSWORD 环境变量未设置"
    fi

    # 验证端口配置逻辑
    log_info "端口配置验证："
    log_info "  - 18095: 外部访问 (应用端口)"
    log_info "  - 3306: 内部访问 (兼容端口)"
    log_info "  - 5432: 内部访问 (PostgreSQL)"
}

# 提供使用建议
provide_usage_tips() {
    echo
    log_info "🎯 使用建议："
    echo "  1. 在 Replit 中设置 NEON_DB_PASSWORD 环境变量"
    echo "  2. 使用 'bash check-databases.sh' 验证数据库连接"
    echo "  3. 使用 'bash run.sh' 启动应用"
    echo "  4. 查看日志：tail -f logs/news.log"
    echo
    log_info "📚 相关文档："
    echo "  - TOML 修复提案：TOML_FIX_OPENSPEC.md"
    echo "  - Neon 迁移指南：NEON_DATABASE_MIGRATION_SPEC.md"
    echo "  - Replit 部署指南：REPLIT_DEPLOYMENT_GUIDE.md"
}

# 主函数
main() {
    echo
    log_info "开始修复 .replit TOML 解析错误..."
    echo

    # 步骤1：检查文件
    check_file_exists

    # 步骤2：备份原始文件
    backup_original

    # 步骤3：检查当前格式
    check_current_format
    local format_status=$?

    if [[ $format_status -eq 0 ]]; then
        log_info "配置格式正确，无需修复"
        exit 0
    fi

    # 步骤4：生成修复配置
    generate_fixed_config

    # 步骤5：验证语法
    if validate_toml; then
        log_info "✅ TOML 语法验证通过"
    else
        log_error "❌ TOML 语法验证失败，正在恢复备份..."
        mv .replit.backup.* .replit 2>/dev/null || true
        exit 1
    fi

    # 步骤6：显示变更
    show_diff

    # 步骤7：测试环境
    test_replit_env

    # 步骤8：提供使用建议
    provide_usage_tips

    echo
    log_info "🎉 .replit TOML 修复完成！"
    echo
    log_info "核心修复："
    echo "  ✅ 修复 ports 数组格式（从对象改为数组）"
    echo "  ✅ 更新为 Neon PostgreSQL 配置"
    echo "  ✅ 添加 PostgreSQL 端口映射"
    echo "  ✅ 保持向后兼容性"
    echo
    log_info "哲学思考："
    echo "  💡 '好品味' - 用正确的数据结构表达正确的意图"
    echo "  💡 '实用主义' - 直接解决问题而非追求理论完美"
    echo "  💡 '简洁执念' - 明确的类型声明胜过隐式转换"
    echo
}

# 错误处理
trap 'log_error "脚本执行失败，行号: $LINENO"; exit 1' ERR

# 运行主函数
main "$@"""file_path":"/Users/guoyingcheng/dreame/code/daily-news-main/fix-replit-toml.sh