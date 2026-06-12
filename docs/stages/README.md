# SubPilot 分阶段学习文档

这组文档面向只学过 Java 基础语法、没有 JavaSE 系统学习经验、不了解 Spring 和 Spring Boot、也没有项目开发经历的初学者。

阅读顺序：

1. [阶段一：项目骨架与本地环境](phase-1-foundation.md)
2. [阶段二：用户注册登录与 JWT](phase-2-authentication.md)
3. [阶段三：分类与订阅资产](phase-3-categories-subscriptions.md)
4. [阶段四：账单与看板统计](phase-4-bills-dashboard.md)
5. [阶段五：定时任务与 RabbitMQ 提醒](phase-5-reminders-rabbitmq.md)

每篇文档都按同一种方式组织：

- 这一阶段解决什么真实问题。
- 初学者需要先理解哪些概念。
- 这些概念在代码中落在哪里。
- 一次请求或一次任务在系统里如何流动。
- 常见错误和学习重点。

学习建议：不要一开始追求背会所有注解。先能说清楚“请求进来后经过 Controller、Service、Mapper，最后到数据库”，再逐步理解安全、缓存、消息队列、定时任务这些组件为什么要加进来。
