# 阶段六：Elasticsearch 订阅搜索

## 这一阶段解决什么问题

阶段六让系统从“数据库条件查询”升级为“搜索引擎全文搜索”。

数据库适合保存准确数据，比如订阅价格、下次扣费日期、账单状态。Elasticsearch 适合搜索文本，比如用户输入 `open`，系统可以从订阅名称、服务商、分类名称、描述、备注里一起找。

本阶段实现：

- 配置 Elasticsearch Java 客户端。
- 创建 `subpilot_subscription` 索引。
- 新增订阅后同步写入 Elasticsearch。
- 更新订阅后同步更新 Elasticsearch。
- 删除订阅后从 Elasticsearch 删除文档。
- 提供订阅搜索接口。
- 提供当前用户订阅索引重建接口。
- 搜索时按 `userId` 隔离数据。

## 先理解几个词

如果你只学过 Java 语法，可以先把 Elasticsearch 当成“专门用来搜索的数据库”，但它和 MySQL 的叫法不一样。

- index：索引，类似 MySQL 的表。这里叫 `subpilot_subscription`。
- document：文档，类似表里的一行数据。一个订阅就是一个文档。
- mapping：字段定义，类似表结构。它告诉 ES 哪些字段是文本、数字、日期。
- full-text search：全文搜索，不是完全相等才匹配，而是按文本内容查找。
- sync：同步。MySQL 是主数据，ES 是搜索副本；订阅变化后，要把变化同步到 ES。

## 本阶段代码在哪里

Elasticsearch 基础配置：

- `src/main/java/com/subpilot/infrastructure/elasticsearch/ElasticsearchConfig.java`

搜索模块：

- `src/main/java/com/subpilot/module/search/constant/SearchIndexConstants.java`
- `src/main/java/com/subpilot/module/search/document/SubscriptionSearchDocument.java`
- `src/main/java/com/subpilot/module/search/service/SubscriptionSearchService.java`
- `src/main/java/com/subpilot/module/search/service/impl/SubscriptionSearchServiceImpl.java`
- `src/main/java/com/subpilot/module/search/controller/SearchController.java`
- `src/main/java/com/subpilot/module/search/runner/SearchIndexInitializer.java`

被接入索引同步的订阅模块：

- `src/main/java/com/subpilot/module/subscription/service/impl/SubscriptionServiceImpl.java`

## Java 客户端配置在做什么

`ElasticsearchConfig` 创建三个对象：

- `RestClient`：底层 HTTP 客户端，负责连接 `http://localhost:9200`。
- `ElasticsearchTransport`：把 Java 对象转换成 ES 请求和响应。
- `ElasticsearchClient`：业务代码真正调用的客户端。

你可以把它理解成：

```text
Java 代码
  -> ElasticsearchClient
  -> Transport
  -> RestClient
  -> Elasticsearch 服务
```

Spring Boot 启动时会把这些对象放进容器。后面的 `SubscriptionSearchServiceImpl` 只要通过构造方法声明 `ElasticsearchClient`，Spring 就会自动注入。

## 为什么要创建索引

搜索前必须先有索引。索引定义了每个字段如何被搜索。

例如：

- `name` 是 `text`，因为订阅名称需要全文搜索。
- `provider` 是 `text`，因为服务商也要能搜索。
- `userId` 是 `long`，因为它只用来过滤当前用户。
- `deleted` 是 `boolean`，因为删除后的订阅不应该出现在搜索结果。
- `nextBillingDate` 是 `date`，因为搜索结果可以按日期排序。

`SearchIndexInitializer` 会在项目启动时尝试创建索引。如果 ES 没启动，它只记录日志，不让整个后端启动失败。

## 为什么 MySQL 和 Elasticsearch 都要存订阅

MySQL 是主库，负责保存真实业务数据。

Elasticsearch 是搜索库，负责让搜索更快、更灵活。

所以新增订阅时流程是：

```text
Controller 接收请求
  -> SubscriptionService 保存到 MySQL
  -> SubscriptionSearchService 写入 Elasticsearch
  -> 返回订阅结果
```

更新订阅时流程是：

```text
更新 MySQL
  -> 查询更新后的订阅
  -> 用最新数据覆盖 Elasticsearch 文档
```

删除订阅时流程是：

```text
MySQL 逻辑删除订阅
  -> 删除 Elasticsearch 中对应文档
```

阶段六里仍然坚持一个原则：MySQL 是准的，ES 是为了搜索服务的副本。

## 为什么同步失败不打断订阅保存

用户创建订阅是核心功能，搜索是增强功能。

如果 Elasticsearch 临时不可用，用户仍然应该可以保存订阅。代码中 `SubscriptionServiceImpl` 会捕获索引同步异常并写日志。

这样做的结果是：

- 优点：核心业务不被搜索服务拖垮。
- 代价：短时间内搜索结果可能不是最新的。
- 解决办法：调用重建索引接口，把 MySQL 当前数据重新写入 ES。

## 搜索接口如何保护用户数据

搜索接口必须读取当前登录用户：

```text
Long userId = UserContext.getUserId()
```

然后在 ES 查询里加过滤条件：

```text
userId = 当前登录用户 id
deleted = false
```

这和 MySQL 查询订阅时必须带 `user_id` 是同一个思想：任何用户数据接口都不能只靠前端传参数判断身份。

## 搜索请求如何流动

一次搜索请求：

```text
GET /api/search/subscriptions?keyword=open&page=1&size=10
```

后端流程是：

1. JWT 过滤器解析 token。
2. `UserContext` 保存当前用户 ID。
3. `SearchController` 接收关键词和分页参数。
4. `SubscriptionSearchServiceImpl` 确认索引存在。
5. 构造 ES 查询：
   - `userId` 必须等于当前用户。
   - `deleted` 必须是 `false`。
   - `keyword` 去匹配名称、服务商、分类、描述、备注。
6. 把 ES 文档转换成 `SubscriptionVO`。
7. 返回和订阅列表类似的分页结果。

## 重建索引用来解决什么

重建索引接口：

```text
POST /api/search/subscriptions/rebuild
```

它会把当前用户 MySQL 里的订阅重新写入 ES。

常见使用场景：

- 第一次接入 ES，历史订阅还没有索引。
- ES 数据被清空了。
- 之前 ES 暂时不可用，导致部分订阅没有同步成功。

注意：阶段六的重建只重建当前登录用户的数据。这样更安全，也更适合个人系统。

## 阶段六学习重点

- Elasticsearch 是搜索引擎，不是用来替代 MySQL 的。
- MySQL 存真实数据，ES 存搜索副本。
- Java API Client 是 Java 代码访问 ES 的工具。
- index 类似表，document 类似一行数据，mapping 类似表结构。
- 搜索接口也必须做用户隔离。
- 搜索同步失败时可以通过重建索引修复。

## 常见错误

1. 只查 ES，不带 `userId` 过滤。

这样会造成越权搜索，是严重安全问题。

2. 以为 ES 数据就是最终真实数据。

本项目里 MySQL 才是最终真实数据。

3. 删除订阅后忘记删除 ES 文档。

这样用户会在搜索里看到已经删除的订阅。

4. ES 没启动就以为后端坏了。

阶段六代码允许后端先启动，但搜索接口需要 ES 正常运行。
