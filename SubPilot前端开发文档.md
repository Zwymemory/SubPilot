# SubPilot V2 前端开发文档

## 一、项目名称

**SubPilot Web**

## 二、项目定位

请创建一个现代化的前端管理系统，用于对接后端项目 **SubPilot：智能订阅与数字资产管理系统**。

SubPilot 用于帮助个人用户管理自己的周期性订阅、数字资产、账单、到期提醒和消费统计，例如：

- ChatGPT Plus
- GitHub Copilot
- iCloud
- Netflix
- Spotify
- 阿里云服务器
- 腾讯云服务器
- 域名
- VPN
- 网盘会员
- 软件许可证
- 在线课程
- 设备保修
- 电子发票

前端目标是：提供一个简洁、现代、科技感、适合个人财务与数字资产管理的 Web 控制台。

---

## 三、前端技术栈要求

优先使用以下技术栈：

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
```

如果 Google AI Studio 更适合生成 React，也可以使用：

```text
React
Vite
TypeScript
Ant Design
React Router
Zustand 或 Redux Toolkit
Axios
ECharts / Recharts
dayjs
```

但我更希望使用 **Vue 3 + Element Plus**，因为它更适合后台管理系统，也方便我学习。

---

## 四、整体 UI 风格要求

请不要做成传统老旧后台管理系统。

希望风格是：

```text
现代
干净
轻量科技感
偏 SaaS 控制台
适合个人订阅资产管理
卡片化布局
圆角
柔和阴影
浅色主题优先
支持后续扩展深色主题
```

视觉参考方向：

```text
Linear
Notion
Stripe Dashboard
Vercel Dashboard
Apple 设置页
现代 SaaS 管理后台
```

主色建议：

```text
蓝紫色 / 靛蓝色 / 青绿色
```

不要使用过重的传统后台蓝色，也不要做成电商风格。

---

## 五、页面结构

前端需要包含以下页面：

```text
登录页
注册页
首页看板 Dashboard
订阅资产列表页
订阅资产详情页
新增/编辑订阅页
分类管理页
账单列表页
通知提醒页
搜索页
个人设置页
404 页面
```

---

## 六、路由设计

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
```

默认访问 `/` 时，如果已登录跳转 `/dashboard`，未登录跳转 `/login`。

需要登录保护的页面：

```text
/dashboard
/subscriptions
/categories
/bills
/notifications
/search
/settings
```

登录状态判断：

1. 登录成功后保存 token。
2. Axios 请求自动携带 token。
3. 如果后端返回 401，清除 token 并跳转登录页。

---

## 七、后端接口约定

后端基础地址：

```text
http://localhost:8080
```

所有接口统一返回格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败格式：

```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

前端需要封装 Axios：

1. 请求拦截器自动添加 Authorization。
2. 响应拦截器统一处理 `code !== 0`。
3. 401 自动跳转登录。
4. 错误提示使用 Element Plus Message。

Authorization 格式：

```text
Authorization: Bearer <token>
```

---

## 八、接口清单

### 8.1 认证接口

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
PUT  /api/users/me
```

登录请求：

```json
{
  "email": "test@example.com",
  "password": "123456"
}
```

登录响应：

```json
{
  "accessToken": "xxx",
  "user": {
    "id": 1,
    "email": "test@example.com",
    "nickname": "Zhu"
  }
}
```

---

### 8.2 分类接口

```text
GET    /api/categories
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

分类字段：

```json
{
  "id": 1,
  "name": "AI 工具",
  "icon": "robot",
  "sortOrder": 1
}
```

---

### 8.3 订阅资产接口

```text
GET    /api/subscriptions
POST   /api/subscriptions
GET    /api/subscriptions/{id}
PUT    /api/subscriptions/{id}
DELETE /api/subscriptions/{id}
```

订阅字段：

```json
{
  "id": 1,
  "name": "ChatGPT Plus",
  "provider": "OpenAI",
  "categoryId": 1,
  "categoryName": "AI 工具",
  "description": "AI 助手订阅",
  "price": 20.00,
  "currency": "USD",
  "billingCycle": "MONTHLY",
  "billingInterval": 1,
  "nextBillingDate": "2026-07-01",
  "expireDate": null,
  "remindDaysBefore": 3,
  "autoRenew": true,
  "status": "ACTIVE",
  "website": "https://chatgpt.com",
  "remark": "学习和开发使用"
}
```

列表查询参数：

```text
page
size
keyword
status
categoryId
provider
upcomingOnly
```

---

### 8.4 账单接口

```text
GET  /api/bills
POST /api/bills
GET  /api/bills/{id}
PUT  /api/bills/{id}/paid
PUT  /api/bills/{id}/unpaid
GET  /api/subscriptions/{subscriptionId}/bills
```

账单字段：

```json
{
  "id": 1,
  "subscriptionId": 1,
  "subscriptionName": "ChatGPT Plus",
  "amount": 20.00,
  "currency": "USD",
  "billDate": "2026-07-01",
  "dueDate": "2026-07-01",
  "paidTime": null,
  "status": "UNPAID",
  "remark": ""
}
```

---

### 8.5 看板接口

```text
GET /api/dashboard/summary
GET /api/dashboard/monthly-trend
GET /api/dashboard/category-expense
GET /api/dashboard/top-subscriptions
```

首页汇总数据示例：

```json
{
  "monthlyExpense": 238.50,
  "yearlyExpense": 2862.00,
  "activeSubscriptionCount": 18,
  "upcomingBillingCount": 4,
  "expiringSoonCount": 2,
  "unreadNotificationCount": 3
}
```

月度趋势示例：

```json
[
  {
    "month": "2026-01",
    "amount": 180.00
  },
  {
    "month": "2026-02",
    "amount": 210.00
  }
]
```

分类支出示例：

```json
[
  {
    "categoryName": "AI 工具",
    "amount": 90.00
  },
  {
    "categoryName": "云服务",
    "amount": 120.00
  }
]
```

---

### 8.6 通知接口

```text
GET /api/notifications
PUT /api/notifications/{id}/read
PUT /api/notifications/read-all
```

通知字段：

```json
{
  "id": 1,
  "type": "BILLING_REMINDER",
  "title": "ChatGPT Plus 即将扣费",
  "content": "你的 ChatGPT Plus 将在 3 天后扣费 20 USD",
  "readStatus": false,
  "createdAt": "2026-06-09T10:00:00"
}
```

---

### 8.7 搜索接口

```text
GET /api/search/subscriptions?keyword=chatgpt&page=1&size=10
```

搜索结果和订阅列表结果保持一致。

---

## 九、核心页面设计要求

### 9.1 登录页

要求：

1. 页面简洁现代。
2. 左侧可以展示 SubPilot 品牌介绍。
3. 右侧为登录表单。
4. 表单字段：邮箱、密码。
5. 登录成功后跳转 `/dashboard`。
6. 支持跳转注册页。

页面文案建议：

```text
SubPilot
Manage your subscriptions before they manage you.
```

中文可用：

```text
统一管理你的订阅、账单与数字资产。
```

---

### 9.2 注册页

要求：

1. 字段：邮箱、昵称、密码、确认密码。
2. 注册成功后自动跳转登录页，或直接登录进入 Dashboard。
3. 表单校验：
    - 邮箱格式
    - 密码至少 6 位
    - 两次密码一致

---

### 9.3 首页看板 Dashboard

这是最重要的页面之一。

需要包含：

1. 顶部欢迎语。
2. 本月支出卡片。
3. 年度支出卡片。
4. 活跃订阅数量卡片。
5. 即将扣费数量卡片。
6. 分类支出饼图。
7. 最近 6 个月支出趋势折线图。
8. 最贵订阅 Top 10。
9. 即将扣费列表。
10. 未读通知入口。

布局建议：

```text
顶部：欢迎语 + 新增订阅按钮
第一行：4 个数据卡片
第二行：趋势图 + 分类饼图
第三行：即将扣费列表 + 最贵订阅列表
```

---

### 9.4 订阅资产列表页

功能：

1. 表格展示订阅资产。
2. 支持分页。
3. 支持关键词搜索。
4. 支持状态筛选。
5. 支持分类筛选。
6. 支持服务商筛选。
7. 支持新增订阅。
8. 支持编辑、删除、查看详情。

表格列建议：

```text
名称
服务商
分类
价格
币种
周期
下次扣费日期
状态
是否自动续费
操作
```

状态需要用 Tag 显示：

```text
ACTIVE 使用中
PAUSED 已暂停
CANCELLED 已取消
EXPIRED 已过期
```

---

### 9.5 新增/编辑订阅页

表单字段：

```text
订阅名称
服务商
分类
描述
价格
币种
计费周期
计费间隔
下次扣费日期
到期日期
提前提醒天数
是否自动续费
状态
官网地址
备注
```

要求：

1. 表单分区，不要全部堆在一起。
2. 金额使用数字输入框。
3. 日期使用日期选择器。
4. 状态、周期、币种使用下拉框。
5. 提交成功后返回列表页或详情页。

---

### 9.6 订阅详情页

展示：

1. 订阅基本信息。
2. 价格与周期。
3. 下次扣费时间。
4. 到期时间。
5. 自动续费状态。
6. 备注。
7. 历史账单列表。
8. 操作按钮：编辑、删除、标记暂停、返回列表。

---

### 9.7 分类管理页

功能：

1. 展示分类列表。
2. 新增分类。
3. 编辑分类。
4. 删除分类。
5. 显示每个分类下的订阅数量，如果后端有该字段。

---

### 9.8 账单列表页

功能：

1. 展示账单列表。
2. 按月份筛选。
3. 按状态筛选。
4. 按订阅名称搜索。
5. 标记已支付。
6. 标记未支付。

表格列：

```text
订阅名称
金额
币种
账单日期
到期日期
支付时间
状态
操作
```

---

### 9.9 通知提醒页

功能：

1. 展示通知列表。
2. 未读通知高亮。
3. 标记单条已读。
4. 一键全部已读。
5. 按通知类型筛选。

---

### 9.10 搜索页

功能：

1. 顶部大搜索框。
2. 输入关键词搜索订阅。
3. 结果以卡片形式展示。
4. 支持跳转订阅详情页。

---

### 9.11 个人设置页

功能：

1. 展示当前用户邮箱。
2. 修改昵称。
3. 预留修改密码入口。
4. 退出登录。

---

## 十、前端工程结构要求

推荐结构：

```text
SubPilot-Web/
├── package.json
├── vite.config.ts
├── index.html
├── README.md
└── src/
    ├── main.ts
    ├── App.vue
    ├── router/
    │   └── index.ts
    ├── stores/
    │   ├── auth.ts
    │   └── user.ts
    ├── api/
    │   ├── request.ts
    │   ├── auth.ts
    │   ├── user.ts
    │   ├── category.ts
    │   ├── subscription.ts
    │   ├── bill.ts
    │   ├── dashboard.ts
    │   ├── notification.ts
    │   └── search.ts
    ├── layouts/
    │   └── BasicLayout.vue
    ├── views/
    │   ├── Login.vue
    │   ├── Register.vue
    │   ├── Dashboard.vue
    │   ├── SubscriptionList.vue
    │   ├── SubscriptionDetail.vue
    │   ├── SubscriptionForm.vue
    │   ├── CategoryManage.vue
    │   ├── BillList.vue
    │   ├── NotificationList.vue
    │   ├── Search.vue
    │   ├── Settings.vue
    │   └── NotFound.vue
    ├── components/
    │   ├── AppHeader.vue
    │   ├── AppSidebar.vue
    │   ├── StatCard.vue
    │   ├── SubscriptionCard.vue
    │   ├── ExpenseTrendChart.vue
    │   └── CategoryPieChart.vue
    ├── types/
    │   ├── auth.ts
    │   ├── subscription.ts
    │   ├── bill.ts
    │   └── common.ts
    ├── utils/
    │   ├── date.ts
    │   ├── money.ts
    │   └── storage.ts
    └── styles/
        ├── variables.scss
        └── global.scss
```

---

## 十一、布局要求

登录和注册页使用独立布局。

登录后页面使用 BasicLayout：

```text
左侧侧边栏
顶部导航栏
右侧主内容区
```

侧边栏菜单：

```text
Dashboard
订阅资产
分类管理
账单管理
通知提醒
搜索
个人设置
```

顶部导航栏：

```text
项目名称
全局搜索入口
通知图标
用户头像 / 昵称
退出登录
```

---

## 十二、数据状态管理

使用 Pinia 管理：

```text
authStore
userStore
```

authStore 管理：

```text
token
isLoggedIn
login()
logout()
setToken()
clearToken()
```

userStore 管理：

```text
currentUser
fetchCurrentUser()
updateCurrentUser()
```

业务数据可以先在页面内管理，不必全部放 Pinia。

---

## 十三、开发阶段要求

请分阶段生成，不要一次性生成全部页面。

### 阶段 1：项目骨架与基础布局

完成：

1. 创建 Vue 3 + Vite + TypeScript 项目。
2. 安装 Element Plus、Vue Router、Pinia、Axios、ECharts、dayjs。
3. 配置路由。
4. 配置 Axios 请求封装。
5. 配置 Pinia。
6. 实现登录页。
7. 实现注册页。
8. 实现 BasicLayout。
9. 实现侧边栏和顶部栏。
10. 实现 Dashboard 静态页面。

验收：

```text
npm install
npm run dev
可以访问登录页
可以访问注册页
登录后可以进入 Dashboard 静态页
布局正常
```

---

### 阶段 2：认证联调

完成：

1. 对接登录接口。
2. 对接注册接口。
3. 保存 token。
4. Axios 自动携带 token。
5. 路由守卫。
6. 对接获取当前用户接口。
7. 退出登录。

验收：

```text
可以注册
可以登录
刷新页面后仍保持登录状态
未登录访问受保护页面会跳转登录页
token 失效后自动退出
```

---

### 阶段 3：分类和订阅模块

完成：

1. 分类管理页。
2. 订阅列表页。
3. 新增订阅页。
4. 编辑订阅页。
5. 订阅详情页。
6. 对接分类接口。
7. 对接订阅接口。
8. 实现搜索、筛选、分页。
9. 实现删除确认弹窗。

验收：

```text
可以创建分类
可以创建订阅
可以查询订阅列表
可以编辑订阅
可以删除订阅
可以查看订阅详情
```

---

### 阶段 4：账单与看板

完成：

1. 账单列表页。
2. 标记账单已支付。
3. 标记账单未支付。
4. Dashboard 对接真实接口。
5. ECharts 展示月度趋势。
6. ECharts 展示分类支出。

验收：

```text
Dashboard 能显示真实统计
账单列表能查询
账单状态可以修改
图表显示正常
```

---

### 阶段 5：通知与搜索

完成：

1. 通知列表页。
2. 标记通知已读。
3. 一键全部已读。
4. 搜索页。
5. 对接 Elasticsearch 搜索接口。

验收：

```text
可以查看通知
可以标记已读
可以搜索订阅
搜索结果可以跳转详情页
```

---

### 阶段 6：优化与文档

完成：

1. 全局 loading。
2. 空状态组件。
3. 错误状态处理。
4. 页面响应式优化。
5. README.md。
6. 前后端联调说明。
7. 常见问题说明。

---

## 十四、Mock 数据要求

如果后端接口暂时没有完成，请先使用 mock 数据实现页面效果。

但代码结构要方便切换到真实接口。

可以采用：

```text
在 api 文件中暂时返回 Promise.resolve(mockData)
后端完成后再替换为 axios 请求
```

不要把 mock 数据直接写死在页面组件里。

---

## 十五、代码质量要求

1. 使用 TypeScript。
2. 组件命名清晰。
3. 页面不要写成一个巨大文件。
4. API 请求统一放在 `src/api`。
5. 类型定义统一放在 `src/types`。
6. 公共工具函数放在 `src/utils`。
7. 不要在多个页面重复写相同逻辑。
8. 表单需要基本校验。
9. 删除操作需要二次确认。
10. 金额展示需要格式化。
11. 日期展示需要格式化。
12. 接口错误需要友好提示。
13. 页面需要空状态。
14. 页面需要 loading 状态。
15. 代码需要能直接运行。

---

## 十六、前端最终验收

最终需要支持：

```bash
npm install
npm run dev
npm run build
```

最终页面包括：

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
```

最终业务闭环：

```text
注册
登录
进入 Dashboard
创建分类
创建订阅
查询订阅
编辑订阅
查看订阅详情
创建/查看账单
查看统计图表
查看通知
搜索订阅
退出登录
```

---

## 十七、请优先生成阶段 1

请先只实现阶段 1，不要一次性生成全部页面和接口联调代码。

阶段 1 完成后，请输出：

```text
1. 项目结构
2. 安装命令
3. 启动命令
4. 已完成页面
5. 下一阶段如何对接后端
```