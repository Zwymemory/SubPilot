# 阶段五：定时任务与 RabbitMQ 提醒

## 这一阶段解决什么问题

阶段五让系统具备“主动提醒”的能力。用户不需要每天打开系统检查到期时间，系统会定时扫描即将扣费、即将到期、账单逾期的数据，然后生成通知。

本阶段实现：

- RabbitMQ exchange、queue、routing key。
- 提醒事件 DTO。
- 定时扫描任务。
- 扫描即将扣费订阅。
- 扫描即将到期订阅。
- 扫描逾期账单。
- 投递提醒消息到 RabbitMQ。
- 消费提醒消息。
- 写入通知表。
- 写入提醒记录表防重复。
- 通知查询和标记已读接口。

## 初学者先理解：什么是定时任务

定时任务就是“到时间自动执行的方法”。

比如每天早上 8 点扫描订阅：

```java
@Scheduled(cron = "0 0 8 * * *")
```

你可以把它理解成 Java 里的闹钟。时间到了，Spring 自动调用这个方法。

## 什么是 RabbitMQ

RabbitMQ 是消息队列。它像一个中转站：

```text
定时任务 -> RabbitMQ -> 消费者 -> 通知表
```

为什么不让定时任务直接写通知表？

因为扫描和生成通知是两个动作。用消息队列拆开后：

- 扫描任务只负责找数据和发消息。
- 消费者负责真正创建通知。
- 后续如果通知逻辑变复杂，比如发邮件、发短信，也更容易扩展。

## RabbitMQ 的三个概念

本项目用了三个核心概念：

- Exchange：交换机，接收消息。
- Queue：队列，保存消息。
- Routing Key：路由键，决定消息怎么从交换机进入队列。

配置在：

- `infrastructure/rabbitmq/RabbitMqConfig.java`
- `infrastructure/rabbitmq/RabbitMqConstants.java`

## 本阶段代码在哪里

RabbitMQ 配置：

- `infrastructure/rabbitmq/RabbitMqConfig.java`
- `infrastructure/rabbitmq/RabbitMqConstants.java`

提醒：

- `module/reminder/scheduler/ReminderScheduler.java`
- `module/reminder/service/ReminderService.java`
- `module/reminder/service/impl/ReminderServiceImpl.java`
- `module/reminder/service/ReminderEventConsumer.java`
- `module/reminder/dto/ReminderEvent.java`
- `module/reminder/entity/ReminderRecordEntity.java`
- `module/reminder/mapper/ReminderRecordMapper.java`

通知：

- `module/notification/controller/NotificationController.java`
- `module/notification/service/impl/NotificationServiceImpl.java`
- `module/notification/entity/NotificationEntity.java`
- `module/notification/mapper/NotificationMapper.java`

## 提醒流程如何流动

以即将扣费提醒为例：

1. 每天 8 点，`ReminderScheduler` 自动执行。
2. 调用 `ReminderService.scanBillingReminders()`。
3. 查询 `next_billing_date` 接近的订阅。
4. 为每个订阅创建 `ReminderEvent`。
5. 用 `RabbitTemplate` 发到 RabbitMQ。
6. `ReminderEventConsumer` 从队列消费消息。
7. `NotificationService.createFromReminder()` 检查是否重复提醒。
8. 写入 `reminder_records` 表。
9. 写入 `notifications` 表。
10. 删除 dashboard 缓存，因为未读通知数变化了。

## 为什么要防重复提醒

定时任务每天都会跑。如果不去重，同一个订阅今天可能被提醒多次。

本项目用 `reminder_records` 表记录：

- userId
- subscriptionId
- billId
- reminderType
- reminderDate

消费者写通知前先查今天是否已经生成过同类提醒。如果有，就跳过。

## 为什么消息发送失败不影响主业务

提醒是辅助功能。如果 RabbitMQ 暂时不可用，不能影响用户创建订阅、支付账单这些核心操作。

所以发送消息失败时记录日志：

```java
log.warn("Publish reminder event failed", exception);
```

后续可以扩展为重试或补偿任务。

## 通知接口

本阶段提供：

```text
GET /api/notifications
PUT /api/notifications/{id}/read
PUT /api/notifications/read-all
```

用户可以查询自己的通知，也可以标记已读。所有通知查询都按 `userId` 隔离。

## 阶段五学习重点

- 定时任务解决“系统自动做事”的问题。
- RabbitMQ 解决“把两个动作异步拆开”的问题。
- 消费者是处理队列消息的代码。
- 去重记录表能防止重复通知。
- 缓存和通知也有关联，因为看板展示未读通知数。
