# 阶段一：项目骨架与本地环境

## 这一阶段解决什么问题

阶段一不急着写业务。它先把一个后端项目能运行起来所需的基础设施搭好：Spring Boot 应用、Maven 依赖、配置文件、统一响应、统一异常、Swagger 接口文档、MyBatis-Plus 配置、Docker Compose 中间件和数据库初始化 SQL。

对初学者来说，这一步像盖楼前打地基。没有地基，后面注册、登录、订阅、账单都没有稳定位置。

## 你需要先理解的概念

Java 源码只是项目的一部分。一个后端项目还需要：

- Maven：管理第三方库，例如 Spring Boot、MySQL 驱动、Redis 客户端。
- Spring Boot：帮我们启动 Web 服务器，并自动装配常用组件。
- Controller：接收浏览器或前端发来的 HTTP 请求。
- Service：写业务逻辑。
- Mapper：访问数据库。
- 配置文件：告诉程序数据库地址、端口、Redis 地址等。
- Docker Compose：用一个文件启动 MySQL、Redis、RabbitMQ、Elasticsearch 等中间件。

如果你只学过 Java 语法，可以把 Spring Boot 理解成“帮你把很多对象创建好并连接起来的框架”。你不再总是自己 `new` 对象，而是把类交给 Spring 管理。

## 本阶段代码在哪里

核心入口：

- `src/main/java/com/subpilot/SubPilotApplication.java`

统一响应：

- `common/response/ApiResponse.java`

统一异常：

- `common/exception/ErrorCode.java`
- `common/exception/BusinessException.java`
- `common/exception/GlobalExceptionHandler.java`

配置：

- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`

数据库初始化：

- `src/main/resources/db/init.sql`

本地中间件：

- `docker-compose.yml`

## 请求是如何流动的

以健康检查接口为例：

1. 浏览器访问 `GET /api/health`。
2. Spring Boot 内置的 Tomcat 收到 HTTP 请求。
3. Spring 根据路径找到 `HealthController`。
4. Controller 返回 `ApiResponse.success(...)`。
5. Spring 把 Java 对象转换为 JSON。
6. 浏览器看到统一格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

这就是后端接口的最小闭环。

## 为什么要统一响应

如果每个接口随便返回，有的返回字符串，有的返回对象，有的返回错误码，前端会很难处理。统一响应让所有接口都像这样：

- 成功：`code = 0`
- 参数错误：`code = 40001`
- 未登录：`code = 40100`
- 系统错误：`code = 50000`

这样前端只需要判断 `code`，就知道请求是否成功。

## 为什么要统一异常

业务代码里经常会遇到错误，比如邮箱已注册、订阅不存在、没有权限。如果每个 Controller 自己 `try-catch`，代码会很乱。

现在做法是：

1. Service 中发现业务错误，抛出 `BusinessException`。
2. `GlobalExceptionHandler` 统一捕获。
3. 返回标准 JSON 错误响应。

这就是“异常集中处理”。

## Docker Compose 的作用

本项目不要求你在电脑上手动安装 MySQL、Redis、RabbitMQ、Elasticsearch。`docker-compose.yml` 会描述这些服务：

- 用哪个镜像。
- 暴露哪个端口。
- 环境变量是什么。
- 数据保存在哪里。

你只需要执行：

```bash
docker compose up -d
```

就能启动本地开发环境。

## 阶段一学习重点

- 理解一个后端项目不是只有 Java 文件。
- 理解 Maven 管依赖，Spring Boot 管启动和对象。
- 理解 Controller、Service、Mapper 的分层思想。
- 理解统一响应和统一异常能让项目更整齐。
- 理解 Docker Compose 是本地开发环境的“启动说明书”。
