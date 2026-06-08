# SubPilot V2 开发文档：智能订阅与数字资产管理系统

## 一、项目定位

请从零创建一个新的 Java Spring Boot 项目，项目名称为：

**SubPilot**

项目定位：

**SubPilot 是一个智能订阅与数字资产管理系统，用于帮助用户统一管理周期性订阅、数字服务、云服务器、域名、会员、账单、到期提醒和消费统计。**

该项目用于学习 Java 后端企业开发，并作为 Java 后端实习简历项目。因此要求代码结构规范、技术栈主流、功能可运行、业务能讲清楚。

本项目直接实现 V2 版本，不做过于简化的玩具 CRUD 项目。V2 版本必须包含：

1. Spring Boot 单体后端架构。
2. 用户注册登录。
3. Spring Security + JWT 认证授权。
4. MySQL 数据持久化。
5. MyBatis-Plus 数据访问。
6. Redis 缓存。
7. RabbitMQ 异步提醒。
8. Elasticsearch 全文搜索。
9. Spring Scheduler 定时任务。
10. Swagger / OpenAPI 接口文档。
11. Docker Compose 本地开发环境。
12. README-local.md 本地启动说明。
13. docs/interview-guide.md 面试讲解文档。

请不要生成前端项目，当前只实现后端。

---

## 二、技术栈要求

### 2.1 后端基础

- Java 21
- Maven
- Spring Boot 3.x
- Spring MVC
- Spring Validation
- Spring Security
- JWT
- MyBatis-Plus
- MySQL 8.0
- Redis 7
- RabbitMQ 3-management
- Elasticsearch 8.x
- Kibana 8.x
- Spring Scheduler
- Swagger / OpenAPI
- Lombok
- MapStruct，可选
- JUnit 5，可选

### 2.2 本地开发环境

Mac 本机只安装：

- Java 21
- Maven
- IntelliJ IDEA
- Docker Desktop

以下中间件全部用 Docker Compose 启动：

- MySQL
- Redis
- RabbitMQ
- Elasticsearch
- Kibana

不要要求用户在 Mac 本机直接安装 MySQL、Redis、RabbitMQ、Elasticsearch。

---

## 三、项目核心业务说明

SubPilot 管理的不是普通帖子或商品，而是用户的各种周期性订阅和数字资产，例如：

- ChatGPT Plus
- GitHub Copilot
- iCloud
- Netflix
- Spotify
- 阿里云服务器
- 腾讯云轻量服务器
- 域名
- VPN
- 网盘会员
- 软件许可证
- 在线课程
- 设备保修
- 电子发票

系统核心目标：

1. 帮助用户记录自己有哪些订阅和数字资产。
2. 帮助用户知道每个订阅什么时候再次扣费或到期。
3. 自动生成账单记录。
4. 在即将扣费或即将到期前产生提醒。
5. 统计用户每月、每年订阅支出。
6. 支持按名称、服务商、备注、标签全文搜索。
7. 通过 Redis 缓存用户首页看板和订阅详情。
8. 通过 RabbitMQ 异步处理提醒事件。
9. 通过 Elasticsearch 提供搜索能力。

---

## 四、功能模块设计

### 4.1 用户与认证模块

模块名：

```text
auth
user
```

功能：

1. 用户注册。
2. 用户登录。
3. 获取当前登录用户。
4. 修改当前用户资料。
5. 修改密码，可选。
6. JWT 登录认证。
7. Spring Security 接口保护。
8. BCrypt 密码加密。

接口建议：

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
PUT  /api/users/me
```

要求：

1. 注册时校验邮箱唯一。
2. 密码必须加密存储。
3. 登录成功返回 accessToken。
4. 除注册、登录、Swagger 外，其余业务接口默认需要登录。
5. Controller 中不要手动解析 token，应通过统一用户上下文获取 userId。

推荐类：

```text
JwtTokenProvider
JwtAuthenticationFilter
SecurityConfig
LoginUser
UserContext
PasswordEncoderConfig
```

---

### 4.2 订阅资产模块

模块名：

```text
subscription
```

这是项目核心模块。

功能：

1. 新增订阅资产。
2. 编辑订阅资产。
3. 删除订阅资产，使用逻辑删除。
4. 查询订阅资产详情。
5. 分页查询我的订阅资产。
6. 按状态筛选。
7. 按分类筛选。
8. 按服务商筛选。
9. 按是否即将到期筛选。
10. 设置订阅周期。
11. 设置下次扣费日期。
12. 设置价格。
13. 设置币种。
14. 设置提醒提前天数。
15. 设置备注。

订阅状态建议：

```text
ACTIVE       使用中
PAUSED       已暂停
CANCELLED    已取消
EXPIRED      已过期
```

周期类型建议：

```text
MONTHLY      月付
QUARTERLY    季付
YEARLY       年付
CUSTOM       自定义
ONE_TIME     一次性
```

接口建议：

```text
POST   /api/subscriptions
PUT    /api/subscriptions/{id}
DELETE /api/subscriptions/{id}
GET    /api/subscriptions/{id}
GET    /api/subscriptions
```

核心字段建议：

```text
id
user_id
name
provider
category_id
description
price
currency
billing_cycle
billing_interval
next_billing_date
expire_date
remind_days_before
status
auto_renew
website
remark
created_at
updated_at
deleted
```

要求：

1. 用户只能操作自己的订阅资产。
2. 金额必须使用 BigDecimal，不要使用 double。
3. 删除使用逻辑删除。
4. 新增、更新、删除订阅后，需要删除 Redis 缓存。
5. 新增、更新、删除订阅后，需要同步 Elasticsearch 索引。
6. 重要操作需要记录日志。

---

### 4.3 分类模块

模块名：

```text
category
```

功能：

1. 创建分类。
2. 编辑分类。
3. 删除分类。
4. 查询我的分类。
5. 提供默认分类初始化。

默认分类建议：

```text
AI 工具
云服务
影音娱乐
办公软件
学习课程
域名网站
生活服务
其他
```

接口建议：

```text
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}
GET    /api/categories
```

要求：

1. 分类属于用户。
2. 分类名称在同一用户下不能重复。
3. 删除分类前需要判断是否存在订阅正在使用该分类。
4. 注册用户后可以自动初始化默认分类。

---

### 4.4 账单模块

模块名：

```text
bill
```

功能：

1. 根据订阅生成账单。
2. 手动创建账单。
3. 查询账单列表。
4. 查询账单详情。
5. 标记账单已支付。
6. 标记账单未支付。
7. 查询某个订阅的历史账单。
8. 查询某月账单。
9. 查询年度账单。

账单状态建议：

```text
UNPAID
PAID
OVERDUE
CANCELLED
```

接口建议：

```text
POST /api/bills
GET  /api/bills
GET  /api/bills/{id}
PUT  /api/bills/{id}/paid
PUT  /api/bills/{id}/unpaid
GET  /api/subscriptions/{subscriptionId}/bills
```

核心字段建议：

```text
id
user_id
subscription_id
amount
currency
bill_date
due_date
paid_time
status
remark
created_at
updated_at
deleted
```

要求：

1. 金额使用 BigDecimal。
2. 用户只能访问自己的账单。
3. 标记账单状态时要校验状态流转。
4. 查询账单时支持时间范围过滤。
5. 账单创建后可以发送异步事件，用于通知或统计刷新。

---

### 4.5 看板统计模块

模块名：

```text
dashboard
```

功能：

1. 查询本月总支出。
2. 查询年度总支出。
3. 查询即将扣费订阅数量。
4. 查询即将过期订阅数量。
5. 查询最贵订阅 Top 10。
6. 查询分类支出占比。
7. 查询最近 6 个月支出趋势。
8. 查询首页看板汇总。

接口建议：

```text
GET /api/dashboard/summary
GET /api/dashboard/monthly-trend
GET /api/dashboard/category-expense
GET /api/dashboard/top-subscriptions
```

要求：

1. 首页汇总接口需要使用 Redis 缓存。
2. 缓存 key 按用户隔离。
3. 订阅、账单发生变化后，需要删除该用户看板缓存。
4. 统计 SQL 写在 Service 或 Mapper 中，不要写在 Controller。

Redis key 建议：

```text
subpilot:dashboard:summary:{userId}
subpilot:dashboard:monthly-trend:{userId}
subpilot:dashboard:category-expense:{userId}
```

---

### 4.6 提醒模块

模块名：

```text
reminder
notification
```

功能：

1. 订阅即将扣费提醒。
2. 订阅即将到期提醒。
3. 账单逾期提醒。
4. 定时扫描需要提醒的数据。
5. 使用 RabbitMQ 发送提醒事件。
6. 消费者异步处理提醒事件。
7. 初期可以将提醒写入 notification 表。
8. 查询我的通知列表。
9. 标记通知已读。

接口建议：

```text
GET /api/notifications
PUT /api/notifications/{id}/read
PUT /api/notifications/read-all
```

定时任务建议：

```text
每天早上 8 点扫描即将扣费和即将到期的数据
每小时扫描逾期账单，可选
```

RabbitMQ 建议：

```text
Exchange: subpilot.reminder.exchange
Queue:    subpilot.reminder.queue
Routing Key:
  reminder.billing
  reminder.expiring
  reminder.overdue
```

要求：

1. 定时任务只负责扫描和投递消息。
2. 消费者负责真正生成通知。
3. 消息体需要包含 userId、subscriptionId、billId、reminderType。
4. 同一天同一订阅同一提醒类型不要重复生成通知。
5. notification 表需要唯一约束或业务去重。

---

### 4.7 Redis 缓存模块

模块名：

```text
infrastructure.redis
```

缓存内容：

1. 订阅详情。
2. 用户首页看板。
3. 分类列表，可选。
4. 即将到期列表，可选。

Redis key 建议：

```text
subpilot:subscription:detail:{userId}:{subscriptionId}
subpilot:dashboard:summary:{userId}
subpilot:category:list:{userId}
```

要求：

1. 查询订阅详情时优先查 Redis。
2. 缓存不存在再查 MySQL。
3. 查询到数据后写入 Redis。
4. 更新或删除订阅时删除订阅详情缓存。
5. 账单或订阅变化时删除 dashboard 缓存。
6. 防止缓存穿透：不存在的数据可以写入短 TTL 空值缓存。
7. Redis 操作封装到 CacheService 或 RedisService，不要散落在 Controller。

---

### 4.8 Elasticsearch 搜索模块

模块名：

```text
search
infrastructure.elasticsearch
```

功能：

1. 创建订阅搜索索引。
2. 新增订阅时写入 ES。
3. 更新订阅时更新 ES。
4. 删除订阅时删除 ES 文档或标记 deleted。
5. 搜索订阅名称。
6. 搜索服务商。
7. 搜索备注。
8. 搜索分类名称。
9. 支持分页。
10. 支持只搜索当前用户的数据。
11. 支持重建索引。

接口建议：

```text
GET  /api/search/subscriptions?keyword=xxx&page=1&size=10
POST /api/admin/search/subscriptions/rebuild
```

索引名：

```text
subpilot_subscription
```

ES 文档字段建议：

```text
id
userId
name
provider
categoryId
categoryName
description
remark
status
price
currency
billingCycle
nextBillingDate
expireDate
createdAt
updatedAt
deleted
```

要求：

1. 搜索结果必须按 userId 隔离。
2. 不能搜到其他用户的数据。
3. 订阅新增、更新、删除后要同步索引。
4. 如果 ES 同步失败，不能影响主业务写入 MySQL，可以记录日志，后续可扩展为 MQ 异步同步。
5. 提供重建索引能力，从 MySQL 全量同步到 ES。
6. 使用官方 Elasticsearch Java API Client 或 Spring Data Elasticsearch，优先使用官方 Java API Client。

---

## 五、数据库设计要求

请创建完整 SQL 初始化文件：

```text
src/main/resources/db/init.sql
```

或：

```text
docs/sql/init.sql
```

至少包含以下表：

```text
users
categories
subscriptions
bills
notifications
reminder_records
```

### 5.1 users 表

字段建议：

```text
id
email
password_hash
nickname
avatar_url
status
created_at
updated_at
deleted
```

约束：

```text
email 唯一
```

---

### 5.2 categories 表

字段建议：

```text
id
user_id
name
icon
sort_order
created_at
updated_at
deleted
```

约束：

```text
user_id + name 唯一
```

---

### 5.3 subscriptions 表

字段建议：

```text
id
user_id
category_id
name
provider
description
price
currency
billing_cycle
billing_interval
next_billing_date
expire_date
remind_days_before
auto_renew
status
website
remark
created_at
updated_at
deleted
```

索引建议：

```text
idx_user_status
idx_user_next_billing_date
idx_user_expire_date
idx_user_category
```

---

### 5.4 bills 表

字段建议：

```text
id
user_id
subscription_id
amount
currency
bill_date
due_date
paid_time
status
remark
created_at
updated_at
deleted
```

索引建议：

```text
idx_user_bill_date
idx_user_status
idx_subscription_id
```

---

### 5.5 notifications 表

字段建议：

```text
id
user_id
type
title
content
related_type
related_id
read_status
created_at
updated_at
deleted
```

索引建议：

```text
idx_user_read_status
idx_user_created_at
```

---

### 5.6 reminder_records 表

用于防止重复提醒。

字段建议：

```text
id
user_id
subscription_id
bill_id
reminder_type
reminder_date
created_at
```

约束建议：

```text
user_id + subscription_id + bill_id + reminder_type + reminder_date 唯一
```

---

## 六、后端工程结构要求

推荐项目结构：

```text
SubPilot/
├── README.md
├── README-local.md
├── docker-compose.yml
├── pom.xml
├── docs/
│   ├── architecture.md
│   ├── api-design.md
│   ├── database-design.md
│   ├── redis-design.md
│   ├── rabbitmq-design.md
│   ├── elasticsearch-design.md
│   └── interview-guide.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/subpilot/
    │   │       ├── SubPilotApplication.java
    │   │       ├── common/
    │   │       │   ├── response/
    │   │       │   ├── exception/
    │   │       │   ├── enums/
    │   │       │   ├── constants/
    │   │       │   └── utils/
    │   │       ├── config/
    │   │       ├── security/
    │   │       ├── infrastructure/
    │   │       │   ├── redis/
    │   │       │   ├── rabbitmq/
    │   │       │   └── elasticsearch/
    │   │       └── module/
    │   │           ├── auth/
    │   │           ├── user/
    │   │           ├── category/
    │   │           ├── subscription/
    │   │           ├── bill/
    │   │           ├── dashboard/
    │   │           ├── reminder/
    │   │           ├── notification/
    │   │           └── search/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── mapper/
    │       └── db/
    │           └── init.sql
    └── test/
```

每个业务模块内部结构建议：

```text
controller
service
service.impl
mapper
entity
dto
vo
converter
enums
```

要求：

1. Controller 只负责参数接收和响应返回。
2. Service 负责业务逻辑。
3. Mapper 负责数据库访问。
4. Entity 对应数据库表。
5. DTO 用于请求参数。
6. VO 用于响应前端。
7. 不要直接返回 Entity。
8. 不要把业务逻辑写在 Controller。

---

## 七、统一响应与异常处理

所有接口统一返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败返回：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

要求实现：

```text
ApiResponse<T>
BusinessException
ErrorCode
GlobalExceptionHandler
```

错误码建议：

```text
0       成功
40001   参数错误
40100   未登录
40300   无权限
40400   资源不存在
40900   资源冲突
50000   系统错误
```

---

## 八、配置文件要求

需要：

```text
application.yml
application-dev.yml
```

开发环境端口：

```text
server.port=8080
```

配置内容包括：

1. MySQL 数据源。
2. Redis。
3. RabbitMQ。
4. Elasticsearch。
5. JWT。
6. MyBatis-Plus。
7. Swagger。

示例配置：

```yaml
spring:
  profiles:
    active: dev
```

开发环境示例：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/subpilot?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: subpilot
    password: subpilot123

  data:
    redis:
      host: localhost
      port: 6379

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

  elasticsearch:
    uris: http://localhost:9200
```

JWT 示例：

```yaml
subpilot:
  jwt:
    secret: dev-secret-please-change-in-production
    expire-seconds: 604800
```

---

## 九、Docker Compose 要求

请生成 docker-compose.yml，包含：

```text
mysql
redis
rabbitmq
elasticsearch
kibana
```

要求：

1. MySQL 使用 8.0。
2. Redis 使用 7。
3. RabbitMQ 使用 management 版本。
4. Elasticsearch 和 Kibana 使用 8.x。
5. Elasticsearch 设置单节点。
6. 本地开发关闭 ES 安全认证。
7. ES JVM 内存限制为 512MB 或 1GB。
8. 所有服务配置数据卷。
9. 所有服务放在同一个 network。
10. README-local.md 写清楚启动、停止、查看日志命令。

访问地址：

```text
MySQL: localhost:3306
Redis: localhost:6379
RabbitMQ: http://localhost:15672
Elasticsearch: http://localhost:9200
Kibana: http://localhost:5601
Swagger: http://localhost:8080/swagger-ui/index.html
```

---

## 十、事务要求

以下操作必须使用事务：

1. 用户注册并初始化默认分类。
2. 新增订阅并同步创建首期账单，可选。
3. 删除订阅并处理相关账单。
4. 标记账单已支付。
5. 定时任务生成账单。
6. 消费提醒消息并写入 notification 和 reminder_record。

事务要求：

1. 使用 @Transactional。
2. 注解放在 Service 层。
3. 不要在 Controller 层加事务。
4. 事务内只处理核心数据库一致性。
5. Redis 删除、ES 同步、MQ 投递需要注意失败影响，尽量不要让非核心中间件失败导致主业务完全不可用。

---

## 十一、V2 开发阶段要求

请按阶段开发，不要一次性生成所有代码。

### 阶段 1：项目骨架与本地环境

完成内容：

1. 创建 Spring Boot 项目。
2. 配置 Maven 依赖。
3. 配置 application.yml 和 application-dev.yml。
4. 配置统一响应。
5. 配置统一异常。
6. 配置 Swagger。
7. 配置 MyBatis-Plus。
8. 创建 docker-compose.yml。
9. 创建 README-local.md。
10. 创建数据库初始化 SQL。

验收标准：

```text
docker compose up -d 能启动所有中间件
mvn spring-boot:run 能启动项目
Swagger 页面可以访问
```

---

### 阶段 2：用户注册登录与 JWT

完成内容：

1. users 表。
2. User Entity / Mapper / Service。
3. 注册接口。
4. 登录接口。
5. BCrypt 密码加密。
6. JWT 生成和校验。
7. Spring Security 配置。
8. 获取当前用户接口。
9. 注册后初始化默认分类。

验收标准：

```text
可以注册用户
可以登录获取 token
携带 token 可以访问 /api/users/me
不携带 token 访问受保护接口返回 401
```

---

### 阶段 3：分类与订阅资产模块

完成内容：

1. categories 表。
2. subscriptions 表。
3. 分类 CRUD。
4. 订阅 CRUD。
5. 分页查询订阅。
6. 条件筛选订阅。
7. 订阅详情。
8. 用户数据隔离。
9. 订阅新增、更新、删除后删除相关 Redis 缓存。

验收标准：

```text
可以创建分类
可以创建订阅
可以分页查询我的订阅
无法访问其他用户的订阅
更新订阅后缓存失效
```

---

### 阶段 4：账单与看板统计

完成内容：

1. bills 表。
2. 手动创建账单。
3. 查询账单。
4. 标记账单已支付。
5. 标记账单未支付。
6. 查询月度账单。
7. 首页看板接口。
8. 分类支出统计。
9. 月度支出趋势。
10. Redis 缓存 dashboard summary。

验收标准：

```text
可以查询本月支出
可以查询年度支出
可以查询分类支出
dashboard summary 能被 Redis 缓存
账单变化后 dashboard 缓存失效
```

---

### 阶段 5：定时任务与 RabbitMQ 提醒

完成内容：

1. 配置 RabbitMQ。
2. 创建 exchange、queue、routing key。
3. 实现提醒事件 DTO。
4. 实现定时扫描任务。
5. 扫描即将扣费订阅。
6. 扫描即将过期订阅。
7. 投递提醒消息到 RabbitMQ。
8. 消费者消费消息。
9. 写入 notifications 表。
10. 写入 reminder_records 表防止重复提醒。

验收标准：

```text
RabbitMQ 管理后台能看到 exchange 和 queue
定时任务能扫描到即将到期数据
消息能被消费者消费
通知能写入数据库
同一天同一订阅不会重复提醒
```

---

### 阶段 6：Elasticsearch 搜索

完成内容：

1. 配置 Elasticsearch Java 客户端。
2. 创建 subpilot_subscription 索引。
3. 订阅新增后写入 ES。
4. 订阅更新后更新 ES。
5. 订阅删除后删除或标记 ES 文档。
6. 实现搜索接口。
7. 实现重建索引接口。
8. 支持 userId 数据隔离。

验收标准：

```text
Kibana 能看到 subpilot_subscription 索引
新增订阅后 ES 有对应文档
搜索关键词可以返回订阅
无法搜索到其他用户的数据
更新订阅后搜索结果同步变化
删除订阅后搜索不到
```

---

### 阶段 7：文档与面试讲解

生成以下文档：

```text
README.md
README-local.md
docs/architecture.md
docs/api-design.md
docs/database-design.md
docs/redis-design.md
docs/rabbitmq-design.md
docs/elasticsearch-design.md
docs/interview-guide.md
```

interview-guide.md 必须包含：

1. 项目背景。
2. 技术选型。
3. 项目模块。
4. 数据库设计。
5. Spring Security + JWT 认证流程。
6. Redis 缓存设计。
7. RabbitMQ 异步提醒设计。
8. Elasticsearch 搜索设计。
9. 定时任务设计。
10. 事务一致性设计。
11. 项目亮点。
12. 面试常见问题和回答。

---

## 十二、代码质量要求

1. 代码必须能运行。
2. 不要生成大量无法编译的伪代码。
3. 每完成一个阶段，必须说明新增了哪些文件。
4. 每完成一个阶段，必须说明如何启动和测试。
5. 每个类的职责要清晰。
6. Controller 不写复杂业务逻辑。
7. Service 不直接暴露 Entity 给 Controller。
8. 所有请求参数使用 DTO。
9. 所有响应使用 VO。
10. 所有接口使用统一响应 ApiResponse。
11. 所有业务异常使用 BusinessException。
12. 所有用户数据必须通过 userId 隔离。
13. 所有金额字段使用 BigDecimal。
14. 所有时间字段使用 LocalDateTime 或 LocalDate。
15. 所有枚举值需要定义 enum，不要在代码中散落字符串。
16. 关键业务需要日志记录。

---

## 十三、最终验收清单

最终项目必须支持以下命令：

```bash
docker compose up -d
docker compose ps
mvn clean package
mvn spring-boot:run
```

最终项目必须支持访问：

```text
Swagger: http://localhost:8080/swagger-ui/index.html
RabbitMQ: http://localhost:15672
Elasticsearch: http://localhost:9200
Kibana: http://localhost:5601
```

最终业务闭环：

```text
注册用户
登录获取 token
自动初始化默认分类
创建订阅
查询订阅列表
查询订阅详情
创建账单
标记账单已支付
查看首页看板
Redis 缓存命中
定时任务扫描提醒
RabbitMQ 发送提醒消息
消费者生成通知
Elasticsearch 搜索订阅
重建 ES 索引
```

---

## 十四、开发方式要求

请严格按阶段实现。

每次只实现一个阶段，完成后输出：

```text
1. 本阶段完成内容
2. 新增或修改文件列表
3. 如何启动
4. 如何测试
5. 当前阶段学习重点
6. 下一阶段建议
```

请不要一次性生成全部阶段代码。

本项目的目标不是只让我得到代码，而是让我在开发过程中学习 Spring Boot、Spring Security、MyBatis-Plus、Redis、RabbitMQ、Elasticsearch 和 Docker Compose 的实际使用方式。因此每一步都要兼顾代码实现和学习解释。