# SubPilot 前端开发文档

本文档用于交给 Gemini、Google AI Studio 或其他前端代码生成工具，生成一个对接当前 SubPilot 后端的完整前端项目。

当前后端已经完成核心闭环：认证、验证码、用户、分类、订阅、账单、看板、通知、RabbitMQ 提醒、Elasticsearch 搜索、Redis 缓存和 Swagger 文档。

## 一、先回答项目流程问题

### 1. 当前后端是否已经成熟

可以认为：当前后端已经是一个功能完整、结构清晰、适合学习、演示和继续接前端的后端项目。

它已经覆盖一个正规后端项目的关键节点：

- 项目骨架和本地 Docker 环境。
- 统一响应 `ApiResponse`。
- 统一异常 `BusinessException`。
- Spring Security + JWT 登录鉴权。
- 登录图形验证码。
- MyBatis-Plus 数据访问。
- MySQL 表设计。
- Redis 缓存。
- RabbitMQ 异步提醒。
- Elasticsearch 搜索。
- Swagger 接口文档。
- 分阶段学习文档和设计文档。

但如果按“真实生产环境上线”标准，还可以继续增强：

- 更完整的单元测试和集成测试。
- CI/CD 自动构建和部署。
- 数据库迁移工具，例如 Flyway 或 Liquibase。
- 日志链路追踪、指标监控和告警。
- 接口限流、防暴力登录、操作审计。
- 更细粒度权限，例如管理员、普通用户。
- HTTPS、生产环境密钥管理、容器化部署脚本。

所以它现在不是“商业生产级最终形态”，但已经完全可以接前端，形成一个完整全栈项目。

### 2. 正规项目是先写后端还是前端

真实团队通常不是绝对先后，而是并行推进：

1. 产品先确定需求和页面原型。
2. 前后端一起约定 API 合同。
3. 后端实现接口。
4. 前端用 Mock 数据或接口文档先开发页面。
5. 后端接口稳定后，前端切换真实接口联调。
6. 联调、测试、修复、上线。

常见模式：

- 后端先行：适合你现在这种学习项目，先把业务和接口写完整，再接前端。
- 前端先行：适合展示型项目或已有接口的项目。
- 前后端并行：企业里最常见，依赖接口文档和 Mock 数据。

你现在后端已经写完，可以接入前端。

## 二、项目名称和定位

项目名称：

```text
SubPilot Web
```

项目定位：

```text
智能订阅与数字资产管理系统的前端控制台
```

SubPilot 用于帮助个人用户管理周期性订阅、数字资产、会员、云服务、域名、账单、到期提醒和消费统计。

典型订阅：

- ChatGPT Plus
- GitHub Copilot
- iCloud
- Netflix
- Spotify
- 云服务器
- 域名
- VPN
- 网盘会员
- 软件许可证
- 在线课程

前端目标：

```text
做一个现代、干净、好看、易用的个人订阅资产管理后台。
```

## 三、推荐前端技术栈

优先使用：

```text
Vue 3
Vite
TypeScript
Element Plus
Vue Router
Pinia
Axios
ECharts
dayjs
lucide-vue-next
```

如果生成工具更擅长 React，也可以使用：

```text
React
Vite
TypeScript
Ant Design
React Router
Zustand
Axios
ECharts 或 Recharts
dayjs
lucide-react
```

如果没有特别说明，请优先生成 Vue 3 + Element Plus 版本。

## 四、UI 风格要求

请不要做成传统老旧后台管理系统。

视觉关键词：

- 现代 SaaS 控制台。
- 干净、留白适中。
- 信息密度适中，适合长期使用。
- 浅色主题优先。
- 支持后续扩展深色主题。
- 不要夸张大渐变，不要电商风，不要营销落地页风格。

参考风格：

- Linear
- Notion
- Stripe Dashboard
- Vercel Dashboard
- Apple 设置页

颜色建议：

- 主色：靛蓝、蓝、青绿任选一种。
- 背景：浅灰白。
- 卡片：白色，轻微边框或阴影。
- 风险状态：红色。
- 成功状态：绿色。
- 提醒状态：橙色。

布局要求：

- 登录页可以更精致。
- 登录后是标准管理控制台布局。
- 左侧导航 + 顶部栏 + 主内容区域。
- 移动端需要可用，至少保证布局不崩。

## 五、后端基础信息

后端本地地址：

```text
http://localhost:8080
```

Swagger：

```text
http://localhost:8080/swagger-ui/index.html
```

统一响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败响应示例：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

前端判断：

- `code === 0`：成功。
- `code !== 0`：失败，展示 `message`。

鉴权请求头：

```text
Authorization: Bearer <accessToken>
```

除以下接口外，其他接口都需要登录：

- `GET /api/auth/captcha`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/health`
- Swagger 相关接口

## 六、前端必须实现的页面

必须实现：

- 登录页
- 注册页
- Dashboard 首页看板
- 订阅资产列表页
- 订阅资产详情页
- 新增订阅页
- 编辑订阅页
- 分类管理页
- 账单列表页
- 通知提醒页
- 搜索页
- 个人设置页
- 404 页面

可选增强：

- 欢迎页或空状态页
- 数据加载骨架屏
- 错误重试页
- 深色主题切换

## 七、路由设计

建议路由：

```text
/login
/register
/dashboard
/subscriptions
/subscriptions/create
/subscriptions/:id
/subscriptions/:id/edit
/categories
/bills
/notifications
/search
/settings
/404
```

默认跳转：

- 访问 `/`，如果已登录跳转 `/dashboard`。
- 未登录跳转 `/login`。

需要登录保护：

```text
/dashboard
/subscriptions
/subscriptions/create
/subscriptions/:id
/subscriptions/:id/edit
/categories
/bills
/notifications
/search
/settings
```

## 八、Axios 封装要求

必须封装统一请求实例。

请求拦截器：

- 从 Pinia 或 localStorage 读取 `accessToken`。
- 如果 token 存在，加请求头 `Authorization: Bearer ${token}`。

响应拦截器：

- 读取统一响应 `code`。
- `code === 0` 返回 `data`。
- `code !== 0` 弹出错误消息。
- 如果 HTTP 状态为 401 或业务 code 为 40100，清除 token 并跳转 `/login`。

建议文件：

```text
src/api/request.ts
src/api/auth.ts
src/api/user.ts
src/api/category.ts
src/api/subscription.ts
src/api/bill.ts
src/api/dashboard.ts
src/api/notification.ts
src/api/search.ts
```

## 九、核心 TypeScript 类型

### 1. 通用响应

```ts
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  page: number
  size: number
  total: number
  pages: number
  records: T[]
}
```

### 2. 用户

```ts
export interface UserVO {
  id: number
  email: string
  nickname: string
  avatarUrl?: string
  status: string
  createdAt: string
}
```

### 3. 认证

```ts
export interface CaptchaVO {
  captchaId: string
  imageBase64: string
  expireSeconds: number
}

export interface LoginVO {
  accessToken: string
  user: UserVO
}

export interface RegisterRequest {
  email: string
  nickname: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
  captchaId: string
  captchaCode: string
}
```

### 4. 分类

```ts
export interface CategoryVO {
  id: number
  name: string
  icon?: string
  sortOrder: number
  subscriptionCount: number
  createdAt: string
  updatedAt: string
}

export interface CategoryRequest {
  name: string
  icon?: string
  sortOrder?: number
}
```

### 5. 订阅

```ts
export type BillingCycle = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY' | 'CUSTOM'
export type SubscriptionStatus = 'ACTIVE' | 'PAUSED' | 'CANCELLED' | 'EXPIRED'

export interface SubscriptionVO {
  id: number
  name: string
  provider?: string
  categoryId?: number
  categoryName?: string
  description?: string
  price: number
  currency: string
  billingCycle: BillingCycle | string
  billingInterval: number
  nextBillingDate?: string
  expireDate?: string
  remindDaysBefore: number
  autoRenew: boolean
  status: SubscriptionStatus | string
  website?: string
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface SubscriptionRequest {
  name: string
  provider?: string
  categoryId?: number
  description?: string
  price: number
  currency: string
  billingCycle: BillingCycle
  billingInterval: number
  nextBillingDate?: string
  expireDate?: string
  remindDaysBefore: number
  autoRenew: boolean
  status: SubscriptionStatus
  website?: string
  remark?: string
}
```

### 6. 账单

```ts
export type BillStatus = 'UNPAID' | 'PAID' | 'OVERDUE' | 'CANCELLED'

export interface BillVO {
  id: number
  subscriptionId?: number
  subscriptionName?: string
  amount: number
  currency: string
  billDate: string
  dueDate?: string
  paidTime?: string
  status: BillStatus | string
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface BillCreateRequest {
  subscriptionId?: number
  amount: number
  currency?: string
  billDate: string
  dueDate?: string
  status?: BillStatus
  remark?: string
}
```

### 7. Dashboard

```ts
export interface DashboardSummaryVO {
  monthlyExpense: number
  yearlyExpense: number
  activeSubscriptionCount: number
  upcomingBillingCount: number
  expiringSoonCount: number
  unreadNotificationCount: number
}

export interface MonthlyTrendVO {
  month: string
  amount: number
}

export interface CategoryExpenseVO {
  categoryId: number
  categoryName: string
  amount: number
}

export interface TopSubscriptionVO {
  id: number
  name: string
  provider?: string
  price: number
  currency: string
  billingCycle: string
}
```

### 8. 通知

```ts
export interface NotificationVO {
  id: number
  type: string
  title: string
  content: string
  relatedType?: string
  relatedId?: number
  readStatus: boolean
  createdAt: string
}
```

## 十、接口清单

### 1. 认证接口

#### 获取登录验证码

```text
GET /api/auth/captcha
```

返回：

```json
{
  "captchaId": "uuid",
  "imageBase64": "data:image/png;base64,...",
  "expireSeconds": 300
}
```

前端要求：

- 登录页加载时自动获取验证码。
- 图片点击可刷新验证码。
- 登录失败后刷新验证码。
- 登录请求必须提交 `captchaId` 和用户输入的 `captchaCode`。

#### 注册

```text
POST /api/auth/register
```

请求：

```json
{
  "email": "demo@example.com",
  "nickname": "Demo",
  "password": "123456"
}
```

返回：`LoginVO`。

注册成功后后端会自动初始化默认分类，并返回 token。前端可以直接保存 token 并进入 Dashboard。

#### 登录

```text
POST /api/auth/login
```

请求：

```json
{
  "email": "demo@example.com",
  "password": "123456",
  "captchaId": "验证码ID",
  "captchaCode": "用户输入验证码"
}
```

返回：`LoginVO`。

### 2. 用户接口

#### 当前用户

```text
GET /api/users/me
```

返回：`UserVO`。

#### 更新当前用户

```text
PUT /api/users/me
```

请求：

```json
{
  "nickname": "New Name",
  "avatarUrl": "https://example.com/avatar.png"
}
```

### 3. 分类接口

#### 分类列表

```text
GET /api/categories
```

返回：`CategoryVO[]`。

#### 创建分类

```text
POST /api/categories
```

请求：

```json
{
  "name": "AI 工具",
  "icon": "bot",
  "sortOrder": 1
}
```

#### 更新分类

```text
PUT /api/categories/{id}
```

#### 删除分类

```text
DELETE /api/categories/{id}
```

注意：如果分类下还有订阅，后端会拒绝删除。

### 4. 订阅接口

#### 创建订阅

```text
POST /api/subscriptions
```

请求示例：

```json
{
  "name": "OpenAI ChatGPT Plus",
  "provider": "OpenAI",
  "categoryId": 1,
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
  "remark": "个人 AI 工具"
}
```

#### 更新订阅

```text
PUT /api/subscriptions/{id}
```

请求字段和创建订阅一致。

#### 删除订阅

```text
DELETE /api/subscriptions/{id}
```

#### 订阅详情

```text
GET /api/subscriptions/{id}
```

#### 订阅列表

```text
GET /api/subscriptions?page=1&size=10&keyword=&status=&categoryId=&provider=&upcomingOnly=false
```

查询参数：

- `page`：默认 1。
- `size`：默认 10，最大 100。
- `keyword`：搜索名称、服务商、描述、备注。
- `status`：`ACTIVE`、`PAUSED`、`CANCELLED`、`EXPIRED`。
- `categoryId`：分类 ID。
- `provider`：服务商。
- `upcomingOnly`：是否只看即将扣费。

返回：`PageResult<SubscriptionVO>`。

### 5. 账单接口

#### 创建账单

```text
POST /api/bills
```

请求：

```json
{
  "subscriptionId": 1,
  "amount": 20.00,
  "currency": "USD",
  "billDate": "2026-06-12",
  "dueDate": "2026-06-20",
  "status": "UNPAID",
  "remark": "June bill"
}
```

#### 账单列表

```text
GET /api/bills?page=1&size=10&status=&subscriptionId=&startDate=&endDate=
```

#### 账单详情

```text
GET /api/bills/{id}
```

#### 标记已支付

```text
PUT /api/bills/{id}/paid
```

#### 标记未支付

```text
PUT /api/bills/{id}/unpaid
```

#### 某订阅的账单历史

```text
GET /api/subscriptions/{subscriptionId}/bills?page=1&size=10
```

### 6. Dashboard 接口

#### 汇总

```text
GET /api/dashboard/summary
```

返回：

```json
{
  "monthlyExpense": 20.00,
  "yearlyExpense": 240.00,
  "activeSubscriptionCount": 3,
  "upcomingBillingCount": 1,
  "expiringSoonCount": 0,
  "unreadNotificationCount": 2
}
```

#### 月度趋势

```text
GET /api/dashboard/monthly-trend?months=12
```

返回：`MonthlyTrendVO[]`。

#### 分类支出

```text
GET /api/dashboard/category-expense?year=2026&month=6
```

返回：`CategoryExpenseVO[]`。

#### 最贵订阅 Top

```text
GET /api/dashboard/top-subscriptions?limit=10
```

返回：`TopSubscriptionVO[]`。

### 7. 通知接口

#### 通知列表

```text
GET /api/notifications?page=1&size=10&readStatus=false
```

查询参数：

- `readStatus` 可为空。为空表示全部，`false` 表示未读，`true` 表示已读。

返回：`PageResult<NotificationVO>`。

#### 标记单条已读

```text
PUT /api/notifications/{id}/read
```

#### 全部标记已读

```text
PUT /api/notifications/read-all
```

### 8. 搜索接口

#### 搜索订阅

```text
GET /api/search/subscriptions?keyword=OpenAI&page=1&size=10
```

返回：`PageResult<SubscriptionVO>`。

#### 重建当前用户订阅索引

```text
POST /api/search/subscriptions/rebuild
```

返回：当前用户重建的订阅数量。

### 9. 健康检查

```text
GET /api/health
```

## 十一、页面详细要求

### 1. 登录页

必须包含：

- 邮箱输入框。
- 密码输入框。
- 验证码输入框。
- 验证码图片。
- 点击验证码图片刷新。
- 登录按钮。
- 跳转注册页。

交互要求：

- 页面进入时调用 `GET /api/auth/captcha`。
- 登录失败时刷新验证码。
- 登录成功保存 token 和用户信息，跳转 `/dashboard`。

### 2. 注册页

必须包含：

- 邮箱。
- 昵称。
- 密码。
- 确认密码。
- 注册按钮。
- 跳转登录页。

注册成功后：

- 保存 token。
- 跳转 Dashboard。

### 3. Dashboard

展示：

- 本月支出。
- 年度支出。
- 活跃订阅数。
- 即将扣费数。
- 即将过期数。
- 未读通知数。
- 月度趋势折线图。
- 分类支出饼图或柱状图。
- 最贵订阅 Top 10。

使用接口：

- `/api/dashboard/summary`
- `/api/dashboard/monthly-trend`
- `/api/dashboard/category-expense`
- `/api/dashboard/top-subscriptions`

### 4. 订阅列表页

功能：

- 分页表格或卡片列表。
- 关键词搜索。
- 状态筛选。
- 分类筛选。
- 服务商筛选。
- 只看即将扣费开关。
- 新增订阅按钮。
- 编辑、删除、查看详情操作。

字段展示：

- 名称。
- 服务商。
- 分类。
- 价格和币种。
- 计费周期。
- 下次扣费日期。
- 状态。
- 自动续费。

### 5. 新增和编辑订阅页

表单字段：

- 订阅名称，必填。
- 服务商。
- 分类。
- 描述。
- 价格，必填。
- 币种，默认 CNY，可选 USD。
- 计费周期，必填。
- 计费间隔，必填。
- 下次扣费日期。
- 到期日期。
- 提前提醒天数。
- 是否自动续费。
- 状态。
- 官网地址。
- 备注。

提交后：

- 新增成功跳转详情或列表。
- 编辑成功回到详情页。

### 6. 订阅详情页

展示：

- 订阅基本信息。
- 价格、周期、下次扣费、到期时间。
- 官网链接。
- 备注。
- 关联账单历史。

操作：

- 编辑订阅。
- 删除订阅。
- 创建账单。
- 查看账单历史。

### 7. 分类管理页

功能：

- 分类列表。
- 新增分类。
- 编辑分类。
- 删除分类。
- 显示每个分类下订阅数量。

注意：

- 删除分类失败时展示后端 message，例如“该分类下存在订阅，不能删除”。

### 8. 账单列表页

功能：

- 分页列表。
- 状态筛选。
- 订阅筛选。
- 日期范围筛选。
- 新增账单。
- 标记已支付。
- 标记未支付。

字段：

- 订阅名称。
- 金额。
- 币种。
- 账单日期。
- 应付日期。
- 支付时间。
- 状态。
- 备注。

### 9. 通知提醒页

功能：

- 通知分页列表。
- 未读筛选。
- 标记单条已读。
- 全部标记已读。

展示：

- 标题。
- 内容。
- 类型。
- 是否已读。
- 创建时间。

通知类型可能包括：

- `BILLING_REMINDER`
- `EXPIRING_REMINDER`
- `OVERDUE_REMINDER`

### 10. 搜索页

功能：

- 顶部大搜索框。
- 输入关键词搜索订阅。
- 搜索结果列表。
- 点击结果进入订阅详情。
- 提供“重建索引”按钮。

使用接口：

- `GET /api/search/subscriptions`
- `POST /api/search/subscriptions/rebuild`

### 11. 个人设置页

功能：

- 展示当前用户。
- 修改昵称。
- 修改头像地址。

使用接口：

- `GET /api/users/me`
- `PUT /api/users/me`

## 十二、状态和枚举展示

订阅状态：

- `ACTIVE`：活跃。
- `PAUSED`：暂停。
- `CANCELLED`：已取消。
- `EXPIRED`：已过期。

账单状态：

- `UNPAID`：未支付。
- `PAID`：已支付。
- `OVERDUE`：已逾期。
- `CANCELLED`：已取消。

计费周期：

- `DAILY`：每日。
- `WEEKLY`：每周。
- `MONTHLY`：每月。
- `QUARTERLY`：每季度。
- `YEARLY`：每年。
- `CUSTOM`：自定义。

前端需要用标签颜色展示状态：

- 活跃、已支付：绿色。
- 未支付、即将扣费：橙色。
- 逾期、错误：红色。
- 暂停、取消：灰色。

## 十三、数据格式约定

金额：

- 后端返回数字。
- 前端展示保留两位小数。
- 格式：`20.00 USD` 或 `¥20.00`。

日期：

- `LocalDate` 格式：`YYYY-MM-DD`。
- `LocalDateTime` 格式：ISO 字符串。
- 前端用 dayjs 格式化。

分页：

- 页码从 1 开始。
- Element Plus 分页组件需要和后端 `page` 对齐。

## 十四、建议项目结构

```text
src/
  api/
    request.ts
    auth.ts
    user.ts
    category.ts
    subscription.ts
    bill.ts
    dashboard.ts
    notification.ts
    search.ts
  assets/
  components/
    AppLayout.vue
    PageHeader.vue
    StatusTag.vue
    MoneyText.vue
    EmptyState.vue
  router/
    index.ts
  stores/
    auth.ts
  types/
    api.ts
  utils/
    format.ts
  views/
    auth/
      LoginView.vue
      RegisterView.vue
    dashboard/
      DashboardView.vue
    subscriptions/
      SubscriptionListView.vue
      SubscriptionDetailView.vue
      SubscriptionFormView.vue
    categories/
      CategoryView.vue
    bills/
      BillView.vue
    notifications/
      NotificationView.vue
    search/
      SearchView.vue
    settings/
      SettingsView.vue
    NotFoundView.vue
```

## 十五、开发顺序建议

请按以下顺序生成和实现：

1. 项目骨架、路由、布局、Axios、Pinia。
2. 登录、注册、验证码。
3. Dashboard 页面。
4. 分类管理。
5. 订阅列表、详情、新增、编辑。
6. 账单列表和支付状态切换。
7. 通知提醒。
8. 搜索页和重建索引。
9. 个人设置。
10. 全局 polish：空状态、loading、错误处理、移动端适配。

不要先做营销首页。打开应用后应该直接进入登录页或 Dashboard。

## 十六、联调验收流程

后端启动：

```bash
docker compose up -d
mvn spring-boot:run
```

前端验收：

1. 打开登录页。
2. 验证码正常显示。
3. 注册新用户。
4. 登录成功进入 Dashboard。
5. 默认分类能显示。
6. 创建订阅。
7. 查询订阅列表。
8. 查看订阅详情。
9. 编辑订阅。
10. 创建账单。
11. 标记账单已支付。
12. Dashboard 统计变化。
13. 搜索订阅能返回结果。
14. 通知页面能正常显示。
15. 个人设置能更新昵称。
16. token 失效或 401 时能跳回登录页。

## 十七、给代码生成工具的最终指令

请根据本文档生成一个完整可运行的 SubPilot 前端项目。

要求：

- 使用 Vue 3 + Vite + TypeScript + Element Plus。
- 使用 Pinia 管理登录状态。
- 使用 Axios 封装后端请求。
- 使用 Vue Router 管理页面路由和登录守卫。
- 使用 ECharts 展示 Dashboard 图表。
- 使用 dayjs 格式化日期。
- 页面要美观、现代、完整，不要只生成静态假页面。
- 所有接口都要按本文档真实对接。
- 登录页必须实现验证码获取、显示、刷新、提交。
- 所有需要登录的接口必须自动携带 JWT。
- 后端返回错误时要展示 `message`。
- 列表页要有分页、筛选、loading、空状态。
- 表单要有校验。
- 删除操作需要二次确认。
- 请生成清晰的项目目录和可运行代码。
