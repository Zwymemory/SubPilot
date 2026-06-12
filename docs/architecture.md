# SubPilot 架构设计

## 项目定位

SubPilot 是一个个人订阅与数字资产管理系统。它的核心目标是帮助用户记录订阅、管理账单、接收到期提醒、查看消费统计，并通过搜索快速找到订阅资产。

## 总体架构

```text
Client / Swagger
  -> Controller
  -> Service
  -> Mapper
  -> MySQL

Service
  -> Redis
  -> RabbitMQ
  -> Elasticsearch
```

对初学者来说，可以先把系统理解为三层：

- Controller：接收 HTTP 请求，校验参数，返回统一响应。
- Service：写业务逻辑，例如创建订阅、生成通知、统计看板。
- Mapper：通过 MyBatis-Plus 操作 MySQL。

中间件负责增强能力：

- Redis：缓存高频读取数据。
- RabbitMQ：异步处理提醒消息。
- Elasticsearch：支持全文搜索。

## 模块划分

- `common`：统一响应、统一异常。
- `config`：MyBatis-Plus、OpenAPI 等通用配置。
- `security`：Spring Security、JWT、当前用户上下文。
- `infrastructure.redis`：Redis 缓存封装。
- `infrastructure.rabbitmq`：RabbitMQ exchange、queue、binding 配置。
- `infrastructure.elasticsearch`：Elasticsearch Java Client 配置。
- `module.auth`：注册登录。
- `module.user`：当前用户信息。
- `module.category`：订阅分类。
- `module.subscription`：订阅资产。
- `module.bill`：账单。
- `module.dashboard`：看板统计。
- `module.reminder`：定时扫描和提醒事件。
- `module.notification`：站内通知。
- `module.search`：订阅搜索。

## 请求处理流程

以创建订阅为例：

```text
POST /api/subscriptions
  -> JwtAuthenticationFilter 解析 token
  -> UserContext 保存当前用户
  -> SubscriptionController 接收 DTO
  -> SubscriptionServiceImpl 校验分类和业务字段
  -> SubscriptionMapper 写入 MySQL
  -> CacheService 删除 dashboard 缓存
  -> SubscriptionSearchService 同步 ES
  -> 返回 SubscriptionVO
```

## 用户隔离原则

所有用户业务数据都必须按 `userId` 隔离。

具体做法：

- JWT 中保存用户 ID。
- 过滤器解析 token 后写入 `UserContext`。
- Service 查询数据时从 `UserContext.getUserId()` 获取当前用户。
- 查询、更新、删除都同时带上业务 ID 和 `userId`。

这样即使用户猜到了其他用户的订阅 ID，也无法访问。

## 数据一致性原则

MySQL 是主数据源。

Redis、RabbitMQ、Elasticsearch 都是辅助系统：

- Redis 可以删除后重新从 MySQL 加载。
- RabbitMQ 消息用于异步通知。
- Elasticsearch 文档用于搜索，可以通过重建索引修复。

这让系统在中间件短暂不可用时仍能保护核心业务。

## 分层约束

- Controller 不写复杂业务逻辑。
- Service 不把 Entity 直接暴露给 Controller。
- 请求参数使用 DTO。
- 返回结果使用 VO。
- 业务错误使用 `BusinessException`。
- 所有接口返回 `ApiResponse`。
- 金额使用 `BigDecimal`。
- 时间使用 `LocalDate` 或 `LocalDateTime`。
