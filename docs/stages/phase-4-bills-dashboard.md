# 阶段四：账单与看板统计

## 这一阶段解决什么问题

阶段四让系统从“记录订阅”升级为“统计消费”。订阅告诉你有哪些服务，账单告诉你花了多少钱，看板把账单数据汇总成容易理解的数字和图表。

本阶段实现：

- 手动创建账单。
- 分页查询账单。
- 查询账单详情。
- 标记账单已支付。
- 标记账单未支付。
- 查询某个订阅的历史账单。
- 首页看板 summary。
- 最近 6 个月支出趋势。
- 分类支出统计。
- 最贵订阅 Top 10。

## 本阶段代码在哪里

账单：

- `module/bill/controller/BillController.java`
- `module/bill/service/impl/BillServiceImpl.java`
- `module/bill/entity/BillEntity.java`
- `module/bill/enums/BillStatus.java`
- `module/bill/mapper/BillMapper.java`

看板：

- `module/dashboard/controller/DashboardController.java`
- `module/dashboard/service/impl/DashboardServiceImpl.java`
- `module/dashboard/vo/DashboardSummaryVO.java`
- `module/dashboard/vo/MonthlyTrendVO.java`
- `module/dashboard/vo/CategoryExpenseVO.java`
- `module/dashboard/vo/TopSubscriptionVO.java`

通知实体：

- `module/notification/entity/NotificationEntity.java`
- `module/notification/mapper/NotificationMapper.java`

## 为什么账单和订阅分开

订阅是“长期资产”，例如 ChatGPT Plus。

账单是“某一次扣费记录”，例如 2026-06-12 支付 20 USD。

一个订阅可以有很多账单：

```text
ChatGPT Plus
  -> 2026-06 bill
  -> 2026-07 bill
  -> 2026-08 bill
```

这就是一对多关系。

## 账单状态是什么

本项目账单状态包括：

- `UNPAID`：未支付。
- `PAID`：已支付。
- `OVERDUE`：已逾期。
- `CANCELLED`：已取消。

状态不是随便改的。例如已经取消的账单不能标记为已支付；只有已支付账单才能标记回未支付。

## 看板统计从哪里来

看板不是单独存一张表，而是从已有数据计算出来：

- 本月支出：本月 `PAID` 账单金额求和。
- 年度支出：今年 `PAID` 账单金额求和。
- 活跃订阅数：状态为 `ACTIVE` 的订阅数量。
- 即将扣费数：下次扣费日期接近的订阅数量。
- 未读通知数：未读通知数量。

这就是统计查询。

## 为什么 dashboard summary 要缓存

首页看板可能会被频繁打开，而且里面有多个统计查询。如果每次打开都查很多次数据库，会增加压力。

所以本项目把 summary 缓存到 Redis：

1. 第一次查 summary，计算并写入 Redis。
2. 第二次查 summary，直接读 Redis。
3. 账单或订阅变化后，删除 summary 缓存。
4. 下次查询重新计算。

这叫“缓存失效策略”。

## 一个支付账单请求如何流动

用户访问：

```text
PUT /api/bills/{id}/paid
```

流程：

1. JWT 过滤器识别当前用户。
2. Controller 接收账单 ID。
3. Service 查询账单，并校验属于当前用户。
4. Service 检查状态能不能改。
5. 更新 `status = PAID` 和 `paid_time`。
6. 删除 dashboard 缓存。
7. 返回账单 VO。

## 阶段四学习重点

- 一个业务对象可能拆成多张表。
- 状态流转要校验，不能随便改。
- 统计接口本质是查询和聚合。
- 缓存不是写了就完，要知道什么时候删。
- Service 层负责事务、校验和缓存失效。
