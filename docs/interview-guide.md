# SubPilot 面试讲解指南

## 1. 项目背景

SubPilot 是一个智能订阅与数字资产管理系统。

它解决的问题是：个人用户订阅了很多周期性服务，例如 AI 工具、云服务器、域名、会员、课程等，容易忘记扣费日期、到期时间和实际花费。

项目提供订阅管理、账单管理、到期提醒、消费统计和全文搜索，让用户可以统一管理这些数字资产。

## 2. 技术选型

- Spring Boot：快速构建后端应用。
- Spring Security + JWT：实现无状态登录认证。
- MyBatis-Plus：简化 MySQL CRUD。
- MySQL：保存核心业务数据。
- Redis：缓存订阅详情和首页看板。
- RabbitMQ：异步处理提醒消息。
- Elasticsearch：实现订阅全文搜索。
- Docker Compose：一键启动本地中间件。
- Swagger / OpenAPI：提供接口文档和调试入口。

面试表达：

```text
MySQL 负责真实数据，Redis 负责缓存，RabbitMQ 负责异步提醒，Elasticsearch 负责搜索。每个组件只解决自己擅长的问题。
```

## 3. 项目模块

- 认证模块：注册、登录、JWT 签发。
- 用户模块：查询和更新当前用户。
- 分类模块：默认分类、自定义分类。
- 订阅模块：订阅 CRUD、筛选、缓存、搜索同步。
- 账单模块：账单 CRUD、支付状态切换。
- 看板模块：月度支出、年度支出、分类支出、Top 订阅。
- 提醒模块：定时扫描即将扣费、即将到期、逾期账单。
- 通知模块：消费提醒消息并生成站内通知。
- 搜索模块：ES 索引、搜索、重建索引。

## 4. 数据库设计

核心表：

- `users`
- `categories`
- `subscriptions`
- `bills`
- `notifications`
- `reminder_records`

设计重点：

- 所有用户业务表都有 `user_id`。
- 订阅、账单、通知等表支持逻辑删除或状态字段。
- 金额使用 `DECIMAL`，Java 使用 `BigDecimal`。
- 时间按场景使用 `DATE` 或 `DATETIME`。
- `reminder_records` 使用唯一索引实现提醒去重。

面试表达：

```text
我的设计里 userId 是非常关键的隔离字段。所有查询、更新、删除都会带上 userId，避免用户越权访问其他人的数据。
```

## 5. Spring Security + JWT 认证流程

注册流程：

```text
用户提交邮箱、昵称、密码
  -> 校验邮箱是否已存在
  -> BCrypt 加密密码
  -> 写入用户表
  -> 初始化默认分类
  -> 生成 JWT
```

登录流程：

```text
用户提交邮箱和密码
  -> 查询用户
  -> BCrypt 校验密码
  -> 生成 JWT
```

请求认证流程：

```text
前端携带 Authorization: Bearer token
  -> JwtAuthenticationFilter 解析 token
  -> 构造 LoginUser
  -> 写入 SecurityContext
  -> 写入 UserContext
  -> Service 从 UserContext 获取 userId
```

项目使用无状态认证，不依赖 Session。

## 6. Redis 缓存设计

缓存内容：

- 订阅详情。
- Dashboard 汇总。

缓存策略：

```text
读：先查 Redis，未命中再查 MySQL，然后写回 Redis。
写：先写 MySQL，再删除相关缓存。
```

为什么删除缓存而不是更新缓存？

```text
删除缓存更简单可靠。下次读取时从 MySQL 重新加载，就能得到最新数据。
```

缓存 key 带 `userId`，避免用户之间缓存串读。

## 7. RabbitMQ 异步提醒设计

提醒流程：

```text
ReminderScheduler 定时触发
  -> ReminderService 查询即将扣费、即将到期、逾期账单
  -> RabbitTemplate 发送 ReminderEvent
  -> RabbitMQ 保存消息
  -> ReminderEventConsumer 消费消息
  -> NotificationService 生成站内通知
```

使用 Direct Exchange：

- `reminder.billing`
- `reminder.expiring`
- `reminder.overdue`

去重方式：

```text
reminder_records 唯一索引
```

同一天同用户同订阅或账单同类型提醒只生成一次通知。

## 8. Elasticsearch 搜索设计

索引：

```text
subpilot_subscription
```

文档：

```text
SubscriptionSearchDocument
```

搜索字段：

- `name`
- `provider`
- `categoryName`
- `description`
- `remark`

过滤字段：

- `userId`
- `deleted`

同步策略：

- 创建订阅后写入 ES。
- 更新订阅后覆盖 ES。
- 删除订阅后删除 ES 文档。
- 提供重建索引接口修复搜索副本。

面试表达：

```text
MySQL 是主库，Elasticsearch 是搜索副本。如果 ES 同步失败，不影响核心业务，后续可以通过重建索引修复。
```

## 9. 定时任务设计

使用 Spring `@Scheduled`。

任务：

- 每天 8 点扫描即将扣费和即将到期订阅。
- 每小时扫描逾期账单。

扫描结果不直接写通知，而是发送 RabbitMQ 消息，让通知生成异步执行。

## 10. 事务一致性设计

使用 `@Transactional` 保证数据库写操作一致。

典型场景：

- 注册用户和初始化默认分类。
- 创建提醒记录和通知。
- 更新订阅或账单后删除缓存。

对于 MySQL 和 ES、RabbitMQ 这类跨系统一致性，项目采用最终一致性：

- MySQL 先成功。
- ES 或 MQ 同步失败时记录日志。
- ES 可以通过重建索引修复。
- 提醒消息可以通过下一次定时扫描补偿。

## 11. 项目亮点

- 业务闭环完整：注册、订阅、账单、统计、提醒、通知、搜索。
- 分层清晰：Controller、Service、Mapper、DTO、VO、Entity 职责明确。
- 用户隔离严格：所有业务数据都围绕 `userId` 查询。
- 缓存设计落地：Redis 缓存详情和看板，写操作主动失效。
- 异步能力落地：RabbitMQ 解耦定时扫描和通知生成。
- 搜索能力落地：Elasticsearch 支持多字段搜索和索引重建。
- 本地环境完整：Docker Compose 管理 MySQL、Redis、RabbitMQ、Elasticsearch、Kibana。
- 文档完整：适合学习、复盘和面试讲解。

## 12. 面试常见问题和回答

### 为什么要用 JWT？

JWT 适合前后端分离的无状态认证。服务端不需要保存 Session，请求只要带 token，后端就能解析出当前用户。

### 为什么密码要用 BCrypt？

不能明文保存密码。BCrypt 会加盐并进行多轮哈希，即使数据库泄露，也比普通哈希更难被破解。

### 如何防止用户访问别人的数据？

后端从 JWT 中解析当前用户 ID，保存到 `UserContext`。所有查询、更新、删除都同时限制业务 ID 和 `userId`。

### Redis 缓存不一致怎么办？

项目采用写后删除缓存策略。MySQL 更新成功后删除相关缓存，下次读取时重新从 MySQL 加载。

### 为什么 RabbitMQ 不直接省掉？

提醒扫描和通知生成是两个职责。RabbitMQ 可以解耦它们，也方便后续扩展邮件、短信、WebSocket 等通知方式。

### 如何避免重复提醒？

使用 `reminder_records` 表和唯一索引。消费提醒消息时先判断今天是否已经生成过同类提醒，存在则跳过。

### Elasticsearch 和 MySQL 如何保持一致？

创建、更新、删除订阅时同步 ES。MySQL 是主数据，ES 是搜索副本。如果 ES 同步失败，可以通过重建索引接口修复。

### 为什么金额使用 BigDecimal？

浮点数会有精度问题。账单金额必须精确，所以 Java 使用 `BigDecimal`，MySQL 使用 `DECIMAL`。

### 项目里哪里用了事务？

注册用户并初始化分类、生成提醒记录和通知、账单状态变化等涉及多个数据库写操作的地方使用事务。

### 如果让你继续优化，会做什么？

可以增加邮件通知、WebSocket 实时通知、账单自动生成、ES 高亮搜索、测试覆盖、权限角色、操作审计和部署流水线。
