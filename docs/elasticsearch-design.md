# SubPilot Elasticsearch 设计

## Elasticsearch 在项目中的作用

Elasticsearch 用来搜索订阅资产。

用户可以按订阅名称、服务商、分类名称、描述、备注搜索。

## 为什么不用 MySQL LIKE 完成所有搜索

MySQL 可以做简单 `LIKE`，但全文搜索能力有限。

Elasticsearch 更适合：

- 多字段搜索。
- 文本相关性排序。
- 后续扩展分词、高亮、复杂搜索。

本项目阶段六用 ES 建立搜索基础。

## Java 客户端

配置代码：

```text
ElasticsearchConfig
```

创建：

- `RestClient`
- `ElasticsearchTransport`
- `ElasticsearchClient`

业务代码通过 `ElasticsearchClient` 访问 ES。

## 索引设计

索引名：

```text
subpilot_subscription
```

索引文档：

```text
SubscriptionSearchDocument
```

主要字段：

- `id`
- `userId`
- `categoryId`
- `categoryName`
- `name`
- `provider`
- `description`
- `price`
- `currency`
- `billingCycle`
- `nextBillingDate`
- `expireDate`
- `status`
- `website`
- `remark`
- `deleted`

## Mapping 思路

- `name`、`provider`、`categoryName`、`description`、`remark` 使用 `text`，便于全文搜索。
- `userId`、`categoryId` 使用数字类型，便于过滤。
- `status`、`currency` 使用 `keyword`，便于精确匹配。
- `price` 使用 `scaled_float`。
- 日期字段使用 `date`。
- `deleted` 使用 `boolean`。

## 索引初始化

启动时执行：

```text
SearchIndexInitializer
```

它会尝试创建 `subpilot_subscription` 索引。

如果 ES 没启动，项目不会整体启动失败，只会记录日志。因为搜索是增强功能，不应该阻塞后端基础功能启动。

## 索引同步

订阅创建：

```text
保存 MySQL
  -> 写入 ES 文档
```

订阅更新：

```text
更新 MySQL
  -> 查询最新订阅
  -> 覆盖 ES 文档
```

订阅删除：

```text
MySQL 逻辑删除
  -> 删除 ES 文档
```

同步接入点在：

```text
SubscriptionServiceImpl
```

## 用户隔离

搜索时必须过滤：

```text
userId = 当前登录用户
deleted = false
```

这样用户 A 不能搜到用户 B 的订阅。

## 重建索引

接口：

```text
POST /api/search/subscriptions/rebuild
```

它会读取当前登录用户的 MySQL 订阅数据，并重新写入 ES。

适用场景：

- 历史数据还没同步到 ES。
- ES 数据被清空。
- 某次 ES 同步失败。

## 设计原则

- MySQL 是主数据。
- ES 是搜索副本。
- ES 同步失败不能影响核心订阅写入。
- 可通过重建索引修复搜索副本。
