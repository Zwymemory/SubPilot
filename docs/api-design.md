# SubPilot API 设计

## 统一响应

所有接口返回 `ApiResponse`：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

这样前端只需要按同一套规则处理成功和失败。

## 认证方式

除注册、登录、健康检查、Swagger 外，其余接口都需要 JWT。

请求头格式：

```text
Authorization: Bearer <accessToken>
```

## API 分组

认证：

- `GET /api/auth/captcha`
- `POST /api/auth/register`
- `POST /api/auth/login`

用户：

- `GET /api/users/me`
- `PUT /api/users/me`

分类：

- `GET /api/categories`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

订阅：

- `POST /api/subscriptions`
- `GET /api/subscriptions`
- `GET /api/subscriptions/{id}`
- `PUT /api/subscriptions/{id}`
- `DELETE /api/subscriptions/{id}`

账单：

- `POST /api/bills`
- `GET /api/bills`
- `GET /api/bills/{id}`
- `PUT /api/bills/{id}/paid`
- `PUT /api/bills/{id}/unpaid`
- `GET /api/subscriptions/{subscriptionId}/bills`

看板：

- `GET /api/dashboard/summary`
- `GET /api/dashboard/monthly-trend`
- `GET /api/dashboard/category-expense`
- `GET /api/dashboard/top-subscriptions`

通知：

- `GET /api/notifications`
- `PUT /api/notifications/{id}/read`
- `PUT /api/notifications/read-all`

搜索：

- `GET /api/search/subscriptions`
- `POST /api/search/subscriptions/rebuild`

健康检查：

- `GET /api/health`

## 登录验证码

登录前先调用：

```text
GET /api/auth/captcha
```

返回：

- `captchaId`：验证码 ID。
- `imageBase64`：验证码图片，前端可直接作为图片地址展示。
- `expireSeconds`：过期秒数。

登录时提交：

```json
{
  "email": "test@example.com",
  "password": "123456",
  "captchaId": "验证码ID",
  "captchaCode": "用户输入的验证码"
}
```

验证码存储在 Redis 中，5 分钟过期，并且登录校验时会一次性消费。

## DTO、VO、Entity 的区别

- DTO：前端传给后端的请求对象，例如 `SubscriptionCreateRequest`。
- Entity：数据库表对应的对象，例如 `SubscriptionEntity`。
- VO：后端返回给前端的对象，例如 `SubscriptionVO`。

不要直接把 Entity 返回给前端。Entity 是数据库结构，VO 是接口结构，两者职责不同。

## 分页约定

分页接口使用：

- `page`：页码，从 1 开始。
- `size`：每页条数，最大 100。

返回结构通常包含：

- `current`
- `size`
- `total`
- `pages`
- `records`

## 参数校验

请求 DTO 使用 Jakarta Validation 注解：

- `@NotBlank`
- `@NotNull`
- `@Size`
- `@Min`
- `@Max`
- `@DecimalMin`

校验失败会被全局异常处理器转换成统一错误响应。

## 错误处理

业务错误抛出 `BusinessException`，错误码来自 `ErrorCode`。

常见错误：

- `PARAM_ERROR`：参数错误。
- `UNAUTHORIZED`：未登录。
- `FORBIDDEN`：无权限。
- `NOT_FOUND`：资源不存在。
- `CONFLICT`：资源冲突。
- `SYSTEM_ERROR`：系统错误。
