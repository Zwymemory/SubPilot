# SubPilot 数据库设计

## 数据库职责

MySQL 是 SubPilot 的主数据源。用户、分类、订阅、账单、通知和提醒记录都以 MySQL 为准。

Redis、RabbitMQ、Elasticsearch 都不能替代 MySQL：

- Redis 是缓存。
- RabbitMQ 是消息队列。
- Elasticsearch 是搜索副本。

## 表结构

当前初始化 SQL 在 `src/main/resources/db/init.sql`。

主要表：

- `users`：用户。
- `categories`：订阅分类。
- `subscriptions`：订阅资产。
- `bills`：账单。
- `notifications`：站内通知。
- `reminder_records`：提醒去重记录。

## users

保存用户账号信息。

关键字段：

- `email`：登录邮箱，唯一。
- `password_hash`：BCrypt 加密后的密码。
- `nickname`：昵称。
- `status`：用户状态。
- `deleted`：逻辑删除字段。

## categories

保存用户自定义分类和默认分类。

关键字段：

- `user_id`：所属用户。
- `name`：分类名称。
- `icon`：图标名称。
- `sort_order`：排序。

唯一约束：

```text
user_id + name + deleted
```

这样同一用户不能创建同名未删除分类。

## subscriptions

保存订阅资产，是项目核心表。

关键字段：

- `user_id`：所属用户。
- `category_id`：分类 ID。
- `name`：订阅名称。
- `provider`：服务商。
- `price`：价格，使用 `DECIMAL(12, 2)`。
- `currency`：币种。
- `billing_cycle`：计费周期。
- `next_billing_date`：下次扣费日期。
- `expire_date`：到期日期。
- `remind_days_before`：提前几天提醒。
- `auto_renew`：是否自动续费。
- `status`：订阅状态。
- `deleted`：逻辑删除字段。

常用索引：

- `user_id + status`
- `user_id + next_billing_date`
- `user_id + expire_date`
- `user_id + category_id`

这些索引用于列表筛选、提醒扫描和分类查询。

## bills

保存账单记录。

关键字段：

- `user_id`：所属用户。
- `subscription_id`：关联订阅。
- `amount`：金额。
- `bill_date`：账单日期。
- `due_date`：应付日期。
- `paid_time`：支付时间。
- `status`：账单状态。

账单状态：

- `UNPAID`
- `PAID`
- `OVERDUE`
- `CANCELLED`

## notifications

保存站内通知。

关键字段：

- `user_id`：所属用户。
- `type`：通知类型。
- `title`：标题。
- `content`：内容。
- `related_type`：关联类型。
- `related_id`：关联 ID。
- `read_status`：是否已读。

## reminder_records

保存提醒去重记录。

唯一约束：

```text
user_id + subscription_id + bill_id + reminder_type + reminder_date
```

用于保证同一天同一个订阅或账单的同一种提醒不会重复生成通知。

## 逻辑删除

多数业务表都有 `deleted` 字段，并由 MyBatis-Plus `@TableLogic` 支持。

删除订阅时，数据库记录不会物理消失，而是把 `deleted` 标记为 1。这样便于保留历史数据，也降低误删风险。

## 事务

涉及多个写操作的业务使用 `@Transactional`。

例如：

- 注册用户后初始化默认分类。
- 创建通知前写入提醒记录。
- 更新账单状态后刷新缓存。

事务保证同一段数据库操作要么一起成功，要么一起回滚。
