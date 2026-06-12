# SubPilot Local Development

本文说明如何在本地启动 SubPilot 后端和配套中间件。

## 环境要求

- Java 21
- Maven 3.9+
- Docker Desktop

## 启动中间件

```bash
docker compose up -d
docker compose ps
```

本地服务地址：

- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- RabbitMQ AMQP: `localhost:5672`
- RabbitMQ Management: `http://localhost:15672`
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`

RabbitMQ 默认账号密码：`guest` / `guest`。

MySQL 容器第一次创建 `mysql_data` volume 时，会自动执行 `src/main/resources/db/init.sql`。

## 启动后端

```bash
mvn spring-boot:run
```

应用地址：

- Health: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 打包验证

```bash
mvn clean package
```

## 停止服务

```bash
docker compose down
```

如果想清空本地中间件数据并重新执行 MySQL 初始化：

```bash
docker compose down -v
docker compose up -d
```

## 常用日志命令

```bash
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f rabbitmq
docker compose logs -f elasticsearch
```

## 端口冲突处理

如果本机已有 MySQL、Redis、RabbitMQ 或 Elasticsearch 占用了默认端口，可以使用环境变量覆盖端口。

启动中间件示例：

```bash
SUBPILOT_MYSQL_PORT=3308 \
SUBPILOT_REDIS_PORT=6381 \
SUBPILOT_RABBITMQ_PORT=5673 \
SUBPILOT_RABBITMQ_MANAGEMENT_PORT=15673 \
SUBPILOT_ELASTICSEARCH_PORT=9201 \
SUBPILOT_KIBANA_PORT=5602 \
docker compose up -d
```

启动后端示例：

```bash
SUBPILOT_MYSQL_PORT=3308 \
SUBPILOT_REDIS_PORT=6381 \
SUBPILOT_RABBITMQ_PORT=5673 \
SUBPILOT_ELASTICSEARCH_PORT=9201 \
mvn spring-boot:run
```

## 最小业务验证路径

1. 打开 Swagger。
2. 注册用户：`POST /api/auth/register`。
3. 登录用户：`POST /api/auth/login`。
4. 复制返回的 `accessToken`。
5. 在 Swagger 右上角 Authorize 中填入 `Bearer token`。
6. 查询默认分类：`GET /api/categories`。
7. 创建订阅：`POST /api/subscriptions`。
8. 查询订阅列表和详情。
9. 创建账单：`POST /api/bills`。
10. 标记账单已支付：`PUT /api/bills/{id}/paid`。
11. 查看看板：`GET /api/dashboard/summary`。
12. 搜索订阅：`GET /api/search/subscriptions`。
13. 重建搜索索引：`POST /api/search/subscriptions/rebuild`。

## 常见问题

如果 `docker compose up -d` 提示端口被占用，先用上面的环境变量换端口。

如果搜索接口报错，先确认 Elasticsearch 是否健康：

```bash
curl http://localhost:9200
```

如果 RabbitMQ 提醒没有消费，先确认队列是否存在：

```bash
docker compose logs -f rabbitmq
```

如果数据库表没有创建，可能是 `mysql_data` volume 已经存在。可以执行 `docker compose down -v` 后重新启动。
