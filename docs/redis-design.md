# SubPilot Redis 设计

## Redis 在项目中的作用

Redis 用来缓存高频读取、可从 MySQL 重建的数据。

本项目缓存两类数据：

- 订阅详情。
- Dashboard 看板汇总。
- 登录验证码。

## 为什么要缓存

订阅详情和首页看板都可能被频繁访问。

如果每次访问都查 MySQL，数据库压力会变大。Redis 读写速度快，适合保存这些热点结果。

## 缓存封装

缓存代码集中在：

```text
src/main/java/com/subpilot/infrastructure/redis/CacheService.java
```

Service 层不直接拼 Redis key，而是调用 `CacheService`。

这样做的好处：

- key 规则集中。
- 过期时间集中。
- 以后换缓存策略更容易。

## 缓存内容

订阅详情缓存：

```text
subpilot:subscription:detail:{userId}:{subscriptionId}
```

Dashboard 缓存：

```text
subpilot:dashboard:summary:{userId}
```

登录验证码：

```text
subpilot:auth:captcha:{captchaId}
```

不同用户的 key 必须带 `userId`，否则可能发生用户数据串读。

验证码还没有登录用户，所以使用随机 `captchaId` 做隔离。

## 缓存更新策略

项目使用“删除缓存”策略，而不是手动更新缓存。

流程：

```text
读数据
  -> 先查 Redis
  -> 没有缓存就查 MySQL
  -> 把结果写回 Redis

写数据
  -> 更新 MySQL
  -> 删除相关 Redis 缓存
```

为什么写操作后删除缓存？

因为缓存只是副本。删除后，下次读取会从 MySQL 重新加载最新数据。

## 哪些操作会删除缓存

订阅变化：

- 创建订阅：删除 dashboard 缓存。
- 更新订阅：删除订阅详情缓存和 dashboard 缓存。
- 删除订阅：删除订阅详情缓存和 dashboard 缓存。

账单变化：

- 创建账单：删除 dashboard 缓存。
- 标记已支付或未支付：删除 dashboard 缓存。

通知变化：

- 创建通知：删除 dashboard 缓存。
- 标记通知已读：删除 dashboard 缓存。

## 空值缓存

订阅不存在时，项目可以缓存一个短时间空值，减少恶意或重复请求打到数据库。

初学者需要理解：空值缓存不是业务数据，只是保护数据库的一种手段。

## Redis 设计原则

- Redis 不保存最终真实数据。
- 缓存 key 必须包含用户维度。
- 登录验证码 key 必须足够随机，并设置较短过期时间。
- 写操作后优先删除缓存。
- 缓存丢失不影响业务正确性。
- 缓存命中只是性能优化，不是业务依赖。
