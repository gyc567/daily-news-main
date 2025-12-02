#!/bin/bash

# 数据库连接检查脚本
# 检查MySQL和Neon数据库的连接状态

set -e

echo "🔍 Daily News 数据库连接检查"
echo "================================"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# MySQL连接信息
MYSQL_HOST="localhost"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASSWORD="root123"

# Neon连接信息
NEON_HOST="ep-morning-wind-aho6ug36-pooler.c-3.us-east-1.aws.neon.tech"
NEON_PORT="5432"
NEON_USER="neondb_owner"
NEON_DATABASE="neondb"

echo -e "${GREEN}[INFO]${NC} 检查MySQL连接..."
if mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -e "SELECT 1;" >/dev/null 2>&1; then
    echo -e "${GREEN}✅${NC} MySQL连接正常"

    # 显示MySQL版本
    VERSION=$(mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -B -N -e "SELECT VERSION();" 2>/dev/null)
    echo -e "${GREEN}📋${NC} MySQL版本: $VERSION"

    # 检查数据库
    echo -e "${GREEN}[INFO]${NC} 检查MySQL数据库..."
    DATABASES=$(mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -B -N -e "SHOW DATABASES LIKE 'daily-news' OR SHOW DATABASES LIKE 'financial_analytics';" 2>/dev/null)

    for db in $DATABASES; do
        if [ "$db" = "daily-news" ] || [ "$db" = "financial_analytics" ]; then
            echo -e "${GREEN}✅${NC} 发现数据库: $db"

            # 检查表
            TABLES=$(mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD $db -B -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$db' AND table_type='BASE TABLE';" 2>/dev/null)
            echo -e "${GREEN}📊${NC}   表数量: $TABLES"

            # 检查数据行数
            TOTAL_ROWS=$(mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD $db -B -N -e "SELECT SUM(table_rows) FROM information_schema.tables WHERE table_schema='$db' AND table_type='BASE TABLE';" 2>/dev/null)
            echo -e "${GREEN}📊${NC}   总行数: ${TOTAL_ROWS:-0}"
        fi
    done
else
    echo -e "${RED}❌${NC} MySQL连接失败"
    echo -e "${YELLOW}⚠️${NC}  请确保MySQL服务正在运行，且用户名密码正确"
fi

echo

# 检查Neon连接（如果密码已设置）
if [ -n "$NEON_DB_PASSWORD" ]; then
    echo -e "${GREEN}[INFO]${NC} 检查Neon PostgreSQL连接..."

    if command -v psql >/dev/null 2>&1; then
        if PGPASSWORD=$NEON_DB_PASSWORD psql -h $NEON_HOST -p $NEON_PORT -U $NEON_USER -d $NEON_DATABASE -c "SELECT 1;" >/dev/null 2>&1; then
            echo -e "${GREEN}✅${NC} Neon PostgreSQL连接正常"

            # 显示PostgreSQL版本
            VERSION=$(PGPASSWORD=$NEON_DB_PASSWORD psql -h $NEON_HOST -p $NEON_PORT -U $NEON_USER -d $NEON_DATABASE -t -c "SELECT version();" 2>/dev/null | head -1)
            echo -e "${GREEN}📋${NC} PostgreSQL版本: $VERSION"

            # 检查schemas
            echo -e "${GREEN}[INFO]${NC} 检查Neon schemas..."
            SCHEMAS=$(PGPASSWORD=$NEON_DB_PASSWORD psql -h $NEON_HOST -p $NEON_PORT -U $NEON_USER -d $NEON_DATABASE -t -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name IN ('news', 'analytics', 'shared');" 2>/dev/null)

            if [ -n "$SCHEMAS" ]; then
                echo -e "${GREEN}✅${NC} 发现schemas:"
                echo "$SCHEMAS" | while read schema; do
                    if [ -n "$schema" ]; then
                        echo -e "${GREEN}  📁${NC} $schema"
                    fi
                done
            else
                echo -e "${YELLOW}⚠️${NC}  未找到news/analytics/shared schemas"
            fi

            # 检查表
            TABLES=$(PGPASSWORD=$NEON_DB_PASSWORD psql -h $NEON_HOST -p $NEON_PORT -U $NEON_USER -d $NEON_DATABASE -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema IN ('news', 'analytics') AND table_type='BASE TABLE';" 2>/dev/null | tr -d ' ')
            if [ "$TABLES" -gt 0 ]; then
                echo -e "${GREEN}📊${NC}  表数量: $TABLES"

                # 检查数据
                for schema in news analytics; do
                    SCHEMA_TABLES=$(PGPASSWORD=$NEON_DB_PASSWORD psql -h $NEON_HOST -p $NEON_PORT -U $NEON_USER -d $NEON_DATABASE -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$schema' AND table_type='BASE TABLE';" 2>/dev/null | tr -d ' ')
                    if [ "$SCHEMA_TABLES" -gt 0 ]; then
                        echo -e "${GREEN}📊${NC}   $schema schema: $SCHEMA_TABLES 表"

                        # 检查总行数
                        TOTAL_ROWS=$(PGPASSWORD=$NEON_DB_PASSWORD psql -h $NEON_HOST -p $NEON_PORT -U $NEON_USER -d $NEON_DATABASE -t -c "SELECT SUM(n_live_tup) FROM pg_stat_user_tables WHERE schemaname='$schema';" 2>/dev/null | tr -d ' ')
                        echo -e "${GREEN}📊${NC}   总行数: ${TOTAL_ROWS:-0}"
                    fi
                done
            fi
        else
            echo -e "${RED}❌${NC} Neon PostgreSQL连接失败"
            echo -e "${YELLOW}⚠️${NC}  请检查连接信息和密码"
        fi
    else
        echo -e "${YELLOW}⚠️${NC}  psql 客户端未安装，无法测试Neon连接"
        echo -e "${YELLOW}⚠️${NC}  建议安装: brew install postgresql (macOS) 或 apt-get install postgresql-client (Linux)"
    fi
else
    echo -e "${YELLOW}⚠️${NC}  NEON_DB_PASSWORD 环境变量未设置"
    echo -e "${YELLOW}⚠️${NC}  请设置: export NEON_DB_PASSWORD='您的Neon密码'"
fi

echo
echo "================================"
echo "📋 数据库连接检查完成"

# 提供建议
if [ -n "$NEON_DB_PASSWORD" ] && command -v psql >/dev/null 2>&1; then
    echo
echo "🚀 下一步: 运行完整的数据库迁移"
    echo "   ./migrate-to-neon.sh"
else
    echo
    echo "🔧 需要准备的:"
    if ! command -v psql >/dev/null 2>&1; then
        echo "   1. 安装PostgreSQL客户端: brew install postgresql"
    fi
    if [ -z "$NEON_DB_PASSWORD" ]; then
        echo "   2. 设置Neon密码: export NEON_DB_PASSWORD='您的密码'"
    fi
fi

echo
echo "✨ 准备好后开始迁移吧！"""file_path