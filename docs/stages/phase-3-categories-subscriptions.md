# 阶段三：分类与订阅资产

## 这一阶段解决什么问题

阶段三开始进入核心业务：用户管理自己的分类和订阅资产。

本阶段实现：

- 分类新增、修改、删除、查询。
- 订阅新增、修改、删除、详情、分页查询。
- 按状态、分类、服务商、关键词筛选。
- 用户数据隔离。
- 订阅详情 Redis 缓存。
- 更新和删除后清理缓存。

## 初学者先理解：什么是 CRUD

CRUD 是四种最基础的数据操作：

- Create：新增。
- Read：查询。
- Update：修改。
- Delete：删除。

大多数后台管理系统，都是围绕 CRUD 建起来的。但企业项目不是简单增删改查，还要加校验、权限、事务、缓存、日志。

## 本阶段代码在哪里

分类：

- `module/category/controller/CategoryController.java`
- `module/category/service/CategoryServiceImpl.java`
- `module/category/entity/CategoryEntity.java`
- `module/category/mapper/CategoryMapper.java`

订阅：

- `module/subscription/controller/SubscriptionController.java`
- `module/subscription/service/impl/SubscriptionServiceImpl.java`
- `module/subscription/entity/SubscriptionEntity.java`
- `module/subscription/mapper/SubscriptionMapper.java`
- `module/subscription/enums/BillingCycle.java`
- `module/subscription/enums/SubscriptionStatus.java`

缓存：

- `infrastructure/redis/CacheService.java`

## 为什么要用户数据隔离

如果用户 A 能访问用户 B 的订阅，就是严重漏洞。

所以查询时不能只按 `id` 查：

```java
eq(SubscriptionEntity::getId, subscriptionId)
```

还必须带上当前用户：

```java
eq(SubscriptionEntity::getUserId, userId)
```

也就是说，一个资源必须同时满足：

- ID 正确。
- 属于当前登录用户。

## 为什么删除分类前要检查订阅

如果一个分类下还有订阅，直接删除分类，订阅就会变成“指向不存在的分类”。这叫数据不一致。

本项目删除分类前会查：

```java
subscriptionMapper.selectCount(...)
```

如果还有订阅使用该分类，就返回业务错误。

## 为什么使用枚举

订阅状态只有固定几种：

- `ACTIVE`
- `PAUSED`
- `CANCELLED`
- `EXPIRED`

计费周期也只有固定几种：

- `MONTHLY`
- `QUARTERLY`
- `YEARLY`
- `CUSTOM`
- `ONE_TIME`

用 enum 可以避免代码里到处写字符串。字符串容易拼错，枚举更安全。

## Redis 缓存如何工作

订阅详情是常见查询。流程是：

1. 先查 Redis。
2. Redis 有数据，直接返回。
3. Redis 没数据，查 MySQL。
4. 查到后写回 Redis。
5. 更新或删除订阅时，删除 Redis 缓存。

这样热点数据可以少查 MySQL。

## 一个订阅详情请求如何流动

用户访问：

```text
GET /api/subscriptions/{id}
```

流程：

1. JWT 过滤器识别当前用户。
2. Controller 接收订阅 ID。
3. Service 先查 Redis。
4. Redis 没命中，Service 查询 MySQL。
5. SQL 查询带上 `user_id`，保证只能查自己的。
6. Entity 转成 VO。
7. 写入 Redis。
8. 返回统一响应。

## Entity、DTO、VO 的区别

初学者很容易混淆这三个：

- Entity：数据库表对应的 Java 类。
- DTO：前端请求传进来的参数。
- VO：后端返回给前端的数据。

不要直接把 Entity 返回给前端，因为 Entity 可能包含内部字段，例如 `deleted`、`userId`、数据库时间字段等。

## 阶段三学习重点

- CRUD 不只是增删改查，还要做权限和校验。
- 所有用户数据都要带 `userId` 隔离。
- enum 适合表达固定取值。
- Redis 缓存要记住“查、写、删”三件事。
- DTO、Entity、VO 要分清职责。
