#!/bin/bash

# Daily News 项目启动脚本
# 适用于 Replit 环境

echo "🚀 正在启动 Daily News 项目..."

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 函数：打印带颜色的信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 检查是否已经编译过
if [ ! -d "news/target" ] || [ ! -f "news/target/news-0.0.1.jar" ]; then
    print_info "检测到项目尚未编译，开始编译..."

    # 编译项目
    mvn clean package -DskipTests

    if [ $? -ne 0 ]; then
        print_error "项目编译失败，请检查错误信息"
        exit 1
    fi

    print_info "项目编译成功！"
else
    print_info "检测到已编译的JAR文件，跳过编译步骤"
fi

# 检查MySQL是否运行
print_info "检查MySQL服务状态..."
if ! pgrep mysqld > /dev/null; then
    print_warning "MySQL服务未运行，正在启动..."

    # 初始化MySQL数据目录（如果不存在）
    if [ ! -d "/home/runner/${REPL_SLUG}/mysql-data" ]; then
        mkdir -p /home/runner/${REPL_SLUG}/mysql-data
        mysqld --initialize-insecure --datadir=/home/runner/${REPL_SLUG}/mysql-data --user=runner
    fi

    # 启动MySQL服务
    mysqld --datadir=/home/runner/${REPL_SLUG}/mysql-data --user=runner --socket=/home/runner/${REPL_SLUG}/mysql.sock --pid-file=/home/runner/${REPL_SLUG}/mysqld.pid --skip-networking=0 --port=3306 &

    # 等待MySQL启动
    sleep 5

    # 设置root密码并创建数据库
    mysql -u root --socket=/home/runner/${REPL_SLUG}/mysql.sock << EOF
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';
CREATE DATABASE IF NOT EXISTS \`daily-news\`;
CREATE DATABASE IF NOT EXISTS \`financial_analytics\`;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

    print_info "MySQL服务启动成功！"
else
    print_info "MySQL服务已在运行"
fi

# 创建日志目录
mkdir -p logs

# 启动应用
print_info "正在启动Spring Boot应用..."
print_info "应用将在端口 18095 运行"
print_info "访问地址: https://$(echo $REPL_SLUG | tr '_' '-').$(echo $REPL_ID).repl.co"

# 运行应用
java -jar news/target/news-0.0.1.jar --spring.profiles.active=local > logs/news.log 2>>1 &

APP_PID=$!

# 等待应用启动
sleep 10

# 检查应用是否成功启动
if ps -p $APP_PID > /dev/null; then
    print_info "应用启动成功！PID: $APP_PID"
    print_info "查看日志: tail -f logs/news.log"
    print_info "停止应用: kill $APP_PID"
else
    print_error "应用启动失败，请查看日志: logs/news.log"
    exit 1
fi

# 保持脚本运行
echo "按 Ctrl+C 停止应用"
tail -f logs/news.log