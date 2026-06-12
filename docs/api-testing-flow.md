# SubPilot API 测试流程

这份文档给初学者一个从 0 到完整闭环的手动测试顺序。

## 1. 启动项目

```bash
docker compose up -d
mvn spring-boot:run
```

打开 Swagger：

```text
http://localhost:8080/swagger-ui/index.html
```

## 2. 注册用户

接口：

```text
POST /api/auth/register
```

示例请求：

```json
{
  "email": "demo@example.com",
  "nickname": "Demo",
  "password": "123456"
}
```

注册成功后会自动初始化默认分类，并返回 `accessToken`。

## 3. 获取登录验证码

接口：

```text
GET /api/auth/captcha
```

复制返回的 `captchaId`，并查看 `imageBase64` 图片中的验证码。

## 4. 登录用户

接口：

```text
POST /api/auth/login
```

示例请求：

```json
{
  "email": "demo@example.com",
  "password": "123456",
  "captchaId": "上一步返回的captchaId",
  "captchaCode": "图片中的验证码"
}
```

复制返回的 token。

## 5. 设置 Swagger 鉴权

点击 Swagger 右上角 Authorize，输入：

```text
Bearer 你的token
```

后续接口就会自动带上登录信息。

## 6. 查询默认分类

接口：

```text
GET /api/categories
```

注册后应该能看到默认分类，例如 AI 工具、云服务、影音娱乐等。

## 7. 创建订阅

接口：

```text
POST /api/subscriptions
```

示例请求：

```json
{
  "name": "OpenAI ChatGPT Plus",
  "provider": "OpenAI",
  "description": "AI assistant subscription",
  "price": 20.00,
  "currency": "USD",
  "billingCycle": "MONTHLY",
  "billingInterval": 1,
  "nextBillingDate": "2026-06-20",
  "expireDate": "2026-12-31",
  "remindDaysBefore": 3,
  "autoRenew": true,
  "status": "ACTIVE",
  "website": "https://openai.com",
  "remark": "demo subscription"
}
```

## 8. 查询订阅

接口：

```text
GET /api/subscriptions
GET /api/subscriptions/{id}
```

这里可以验证分页、详情和 Redis 订阅详情缓存。

## 9. 创建账单

接口：

```text
POST /api/bills
```

示例请求：

```json
{
  "subscriptionId": 你的订阅ID,
  "amount": 20.00,
  "currency": "USD",
  "billDate": "2026-06-12",
  "dueDate": "2026-06-20",
  "status": "UNPAID",
  "remark": "June bill"
}
```

## 10. 标记账单已支付

接口：

```text
PUT /api/bills/{id}/paid
```

## 11. 查看 Dashboard

接口：

```text
GET /api/dashboard/summary
GET /api/dashboard/monthly-trend
GET /api/dashboard/category-expense
GET /api/dashboard/top-subscriptions
```

这里可以验证账单统计和 Redis dashboard 缓存。

## 12. 验证搜索

接口：

```text
GET /api/search/subscriptions?keyword=OpenAI
```

如果搜索不到历史订阅，可以调用：

```text
POST /api/search/subscriptions/rebuild
```

## 13. 验证通知

接口：

```text
GET /api/notifications
PUT /api/notifications/{id}/read
PUT /api/notifications/read-all
```

定时任务会扫描提醒，也可以在 RabbitMQ 管理后台观察 exchange 和 queue。
