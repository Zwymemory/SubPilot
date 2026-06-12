# SubPilot

SubPilot 是一个基于 Java 21 和 Spring Boot 3 的智能订阅与数字资产管理系统后端。

它帮助个人用户统一管理周期性订阅、数字服务、云服务器、域名、会员、账单、到期提醒、消费统计和全文搜索。

当前进度：阶段 7 已完成，后端核心闭环、分阶段学习文档、设计文档和面试讲解文档均已整理。

## 核心能力

- 用户注册、登录、JWT 鉴权。
- 默认分类初始化。
- 分类管理。
- 订阅资产新增、编辑、删除、详情、分页查询。
- 账单创建、支付状态切换、订阅账单历史。
- Dashboard 消费统计。
- Redis 缓存订阅详情和首页看板。
- 定时扫描扣费、到期、逾期提醒。
- RabbitMQ 异步提醒消息和站内通知。
- Elasticsearch 订阅全文搜索和索引重建。
- Swagger 在线接口文档。

## 技术栈

- Java 21
- Spring Boot 3.3
- Spring Security
- MyBatis-Plus
- MySQL 8
- Redis 7
- RabbitMQ 3
- Elasticsearch 8
- Docker Compose
- Maven
- Springdoc OpenAPI

## 快速启动

```bash
docker compose up -d
docker compose ps
mvn spring-boot:run
```

访问地址：

- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Health: `http://localhost:8080/api/health`
- RabbitMQ: `http://localhost:15672`
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`

RabbitMQ 默认账号密码：`guest` / `guest`。

更详细的本地启动、端口覆盖和排错说明见 [README-local.md](README-local.md)。

## 文档入口

- 项目架构：[docs/architecture.md](docs/architecture.md)
- API 设计：[docs/api-design.md](docs/api-design.md)
- 数据库设计：[docs/database-design.md](docs/database-design.md)
- Redis 设计：[docs/redis-design.md](docs/redis-design.md)
- RabbitMQ 设计：[docs/rabbitmq-design.md](docs/rabbitmq-design.md)
- Elasticsearch 设计：[docs/elasticsearch-design.md](docs/elasticsearch-design.md)
- 面试讲解：[docs/interview-guide.md](docs/interview-guide.md)
- API 测试流程：[docs/api-testing-flow.md](docs/api-testing-flow.md)
- 分阶段学习：[docs/stages/README.md](docs/stages/README.md)

## 初学者阅读建议

如果你只学过 Java 语法，没有 JavaSE、Spring、Spring Boot 和项目开发经验，建议先读 [docs/stages/README.md](docs/stages/README.md)，按阶段理解：

1. 项目骨架与本地环境。
2. 用户注册登录与 JWT。
3. 分类与订阅资产。
4. 账单与看板统计。
5. 定时任务与 RabbitMQ 提醒。
6. Elasticsearch 搜索。
7. 文档与面试讲解。

不要急着背注解。先能讲清楚一次请求如何从 Controller 进入 Service，再到 Mapper 和数据库，后面再逐步理解 Redis、RabbitMQ、Elasticsearch 为什么加入项目。
