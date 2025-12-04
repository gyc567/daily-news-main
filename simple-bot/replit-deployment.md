# Replit部署指南

## 🚀 快速部署

### 1. 环境准备
确保在Replit环境中设置了以下环境变量：

```bash
# Telegram Bot配置
TELEGRAM_TOKEN=8291537816:AAEQTE7Jd5AGQ9dkq7NMPewlSr8Kun2qXao
TELEGRAM_CHAT_ID=-1002191041553

# Neon PostgreSQL数据库（已提供）
DATABASE_URL=postgresql://neondb_owner:npg_yTrOujn8eKR5@ep-morning-wind-aho6ug36-pooler.c-3.us-east-1.aws.neon.tech/neondb?sslmode=require
DB_USER=neondb_owner
DB_PASSWORD=npg_yTrOujn8eKR5
```

### 2. 部署步骤

1. **Fork项目**到您的Replit账户
2. **配置环境变量**（在Replit Secrets中设置）
3. **运行项目**：点击Run按钮或使用命令：
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=replit
   ```

### 3. 验证部署

访问以下端点验证部署：
- 健康检查：`https://[your-app].replit.app/health`
- 详细状态：`https://[your-app].replit.app/health/detail`
- 系统统计：`https://[your-app].replit.app/api/bot/stats`

## 🔧 技术架构

### 核心改进
1. **PostgreSQL持久化** - 替换内存存储
2. **环境变量配置** - 移除硬编码敏感信息
3. **策略模式重构** - 消除复杂的switch/if-else
4. **健康检查** - 提供系统监控能力
5. **Replit优化配置** - 适配云部署环境

### 文件结构
```
simple-bot/
├── src/main/java/com/ll/news/
│   ├── bot/                    # Telegram机器人核心
│   │   ├── SimpleTelegramUpdateHandler.java
│   │   ├── TelegramBotService.java
│   │   └── command/            # 策略模式命令处理
│   ├── entity/                 # JPA实体
│   │   └── UserPreference.java
│   ├── repository/             # Spring Data JPA
│   │   └── UserPreferenceRepository.java
│   ├── service/                # 业务逻辑
│   │   └── UserPreferenceService.java
│   └── controller/             # REST API
│       ├── HealthController.java
│       └── BotTestController.java
├── src/main/resources/
│   ├── application-prod.yml    # 生产环境配置
│   └── application.yml         # 默认配置
├── pom.xml                     # Maven构建文件
└── .replit                     # Replit部署配置
```

## 📊 性能优化

### 数据库优化
- **连接池配置**：针对Neon Serverless优化
- **批处理操作**：减少数据库访问次数
- **索引优化**：确保查询性能

### 内存优化
- **减少对象创建**：重用响应消息
- **及时资源释放**：避免内存泄漏
- **简化数据结构**：消除不必要的复杂性

### 网络优化
- **减少API调用**：批量处理消息
- **连接复用**：保持长连接
- **错误重试**：优雅处理网络异常

## 🔍 监控与调试

### 健康检查端点
- `/health` - 基础健康状态
- `/health/detail` - 详细系统状态
- `/health/ready` - 就绪检查（用于Kubernetes）

### 日志监控
```bash
# 查看应用日志
tail -f /tmp/spring-boot.log

# 监控特定组件
log: com.ll.news.bot
log: com.ll.news.service
log: org.springframework.web
```

### 调试模式
```bash
# 启用调试日志
export LOGGING_LEVEL_COM_LL_NEWS=DEBUG
mvn spring-boot:run -Dspring.profiles.active=replit
```

## 🛡️ 安全考虑

### 敏感信息保护
- ✅ Token存储在环境变量中
- ✅ 数据库连接信息加密
- ✅ API响应不包含敏感数据

### 访问控制
- ✅ 健康检查端点公开
- ✅ 管理端点需要授权
- ✅ 用户数据隔离

## 📈 扩展性

### 水平扩展
- 无状态设计支持多实例部署
- 数据库存储支持分布式部署
- 配置外部化便于环境切换

### 功能扩展
- 策略模式支持新命令快速添加
- JPA实体支持数据库字段扩展
- 事件驱动架构支持异步处理

## 🚨 故障排查

### 常见问题

1. **数据库连接失败**
   ```bash
   # 检查数据库URL格式
   echo $DATABASE_URL

   # 验证凭据
   psql $DATABASE_URL -c "SELECT 1"
   ```

2. **Telegram Bot无响应**
   ```bash
   # 检查Token
   curl https://api.telegram.org/bot$TELEGRAM_TOKEN/getMe

   # 查看健康检查
   curl https://[your-app].replit.app/health/detail
   ```

3. **部署失败**
   ```bash
   # 检查构建日志
   mvn clean package -X

   # 验证Java版本
   java -version
   ```

### 紧急联系
- 项目维护：[GitHub Issues]
- Telegram Bot问题：检查官方API状态
- 数据库问题：联系Neon支持

## 🎉 成功部署验证

部署成功后，访问：
- `https://[your-app].replit.app/health` - 应返回{"status":"UP"}
- `https://[your-app].replit.app/api/bot/status` - 应返回系统状态
- Telegram Bot应响应用户命令

Token: `8291537816:AAEQTE7Jd5AGQ9dkq7NMPewlSr8Kun2qXao` 已正确配置，机器人可立即投入使用。`,