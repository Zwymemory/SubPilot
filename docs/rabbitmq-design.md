# SubPilot RabbitMQ 设计

## RabbitMQ 在项目中的作用

RabbitMQ 用来处理异步提醒消息。

提醒不是用户操作的主流程。系统定时扫描后，把提醒事件发送到队列，消费者再生成站内通知。

## 为什么使用消息队列

如果定时任务直接生成所有通知，扫描逻辑和通知逻辑会耦合在一起。

使用 RabbitMQ 后，流程变成：

```text
定时任务扫描
  -> 发布提醒消息
  -> RabbitMQ 暂存消息
  -> 消费者处理消息
  -> 写入通知
```

好处：

- 扫描和通知解耦。
- 消息可排队处理。
- 后续可以扩展邮件、短信、WebSocket 通知。

## 交换机和队列

配置代码：

```text
src/main/java/com/subpilot/infrastructure/rabbitmq/RabbitMqConfig.java
```

常量：

```text
src/main/java/com/subpilot/infrastructure/rabbitmq/RabbitMqConstants.java
```

当前设计：

- Exchange: `subpilot.reminder.exchange`
- Queue: `subpilot.reminder.queue`
- Routing keys:
  - `reminder.billing`
  - `reminder.expiring`
  - `reminder.overdue`

使用 Direct Exchange，因为当前按明确的提醒类型路由。

## 消息内容

提醒消息对象：

```text
ReminderEvent
```

包含：

- `userId`
- `subscriptionId`
- `billId`
- `reminderType`
- `reminderDate`
- `targetDate`
- `subscriptionName`
- `amount`
- `currency`

消费者可以根据这些字段生成通知标题和内容。

## 定时任务

定时任务代码：

```text
ReminderScheduler
```

当前任务：

- 每天 8 点扫描即将扣费和即将到期订阅。
- 每小时扫描逾期账单。

## 生产者

生产者代码：

```text
ReminderServiceImpl
```

它负责：

- 查询即将扣费订阅。
- 查询即将到期订阅。
- 查询逾期账单。
- 构造 `ReminderEvent`。
- 使用 `RabbitTemplate` 发布消息。

## 消费者

消费者代码：

```text
ReminderEventConsumer
```

它监听：

```text
subpilot.reminder.queue
```

收到消息后调用：

```text
NotificationService.createFromReminder
```

## 去重设计

定时任务会重复执行，所以必须防止重复通知。

项目使用 `reminder_records` 表做去重。

同一天、同用户、同订阅或账单、同提醒类型，只生成一次通知。

## 失败处理

提醒是增强功能，不应该影响核心业务。

如果发布消息失败：

- 记录 warn 日志。
- 不影响用户创建订阅、支付账单等主流程。

如果消费时格式错误：

- 抛出业务异常。
- RabbitMQ listener 配置不重新入队，避免坏消息无限重试。
