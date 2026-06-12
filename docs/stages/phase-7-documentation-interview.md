# 阶段七：文档与面试讲解

## 这一阶段解决什么问题

阶段七不再新增业务功能，而是把前六个阶段整理成可以交付、可以学习、可以复盘、可以面试讲解的项目。

很多初学者写完代码后说不清楚：

- 项目为什么这样分层。
- Redis、RabbitMQ、Elasticsearch 分别解决什么问题。
- 一次请求如何走完整链路。
- 面试官问项目亮点时怎么回答。

阶段七就是把这些内容沉淀到文档中。

## 本阶段新增文档

- `README.md`：项目总入口。
- `README-local.md`：本地启动和验证说明。
- `docs/architecture.md`：架构设计。
- `docs/api-design.md`：接口设计。
- `docs/database-design.md`：数据库设计。
- `docs/redis-design.md`：Redis 缓存设计。
- `docs/rabbitmq-design.md`：RabbitMQ 异步提醒设计。
- `docs/elasticsearch-design.md`：Elasticsearch 搜索设计。
- `docs/interview-guide.md`：面试讲解。

## 初学者应该怎样读最终文档

建议顺序：

1. 先读 `README.md`，知道项目能做什么。
2. 再读 `README-local.md`，把项目跑起来。
3. 然后读 `architecture.md`，理解 Controller、Service、Mapper、数据库、中间件如何配合。
4. 再读各专项设计文档。
5. 最后读 `interview-guide.md`，练习把项目讲给别人听。

## 文档和代码如何配合

文档不是替代代码，而是帮你理解代码。

比如你读到 Redis 缓存设计时，可以同时打开：

- `CacheService`
- `SubscriptionServiceImpl`
- `DashboardServiceImpl`

读到 RabbitMQ 设计时，可以同时打开：

- `RabbitMqConfig`
- `ReminderScheduler`
- `ReminderServiceImpl`
- `ReminderEventConsumer`
- `NotificationServiceImpl`

读到 Elasticsearch 设计时，可以同时打开：

- `ElasticsearchConfig`
- `SubscriptionSearchServiceImpl`
- `SearchController`

这样你就能把“文字里的设计”和“代码里的实现”对应起来。

## 阶段七学习重点

- 项目交付不只是写代码，还要让别人能启动、能理解、能维护。
- 架构文档解释整体结构。
- API 文档解释前后端如何约定。
- 数据库文档解释数据如何落表。
- 缓存、消息队列、搜索文档解释中间件为什么存在。
- 面试文档训练你把项目讲清楚。

## 自测问题

1. 为什么 Controller 不直接操作数据库？
2. 为什么用户数据查询都必须带 `userId`？
3. Redis 缓存的数据为什么可以删除后重建？
4. RabbitMQ 为什么适合提醒消息？
5. Elasticsearch 为什么不能替代 MySQL？
6. 如果 ES 同步失败，项目如何补救？
7. 你能用 2 分钟讲清楚 SubPilot 的业务闭环吗？
