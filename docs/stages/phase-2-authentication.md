# 阶段二：用户注册登录与 JWT

## 这一阶段解决什么问题

阶段二让系统认识“当前是谁在使用”。没有登录系统，所有数据都是公共的；有了登录系统，每个用户只能看到自己的分类、订阅和账单。

本阶段实现：

- 注册用户。
- 登录用户。
- 登录图形验证码。
- 密码加密。
- 生成 JWT。
- 校验 JWT。
- 获取当前用户。
- 注册后初始化默认分类。

## 初学者先理解：为什么不能明文存密码

用户注册时输入密码，例如 `123456`。数据库不能直接保存 `123456`，否则数据库泄露时所有用户密码都会暴露。

项目使用 BCrypt：

```java
passwordEncoder.encode(request.password())
```

BCrypt 会把密码变成不可逆的哈希值。登录时不是解密，而是用：

```java
passwordEncoder.matches(rawPassword, passwordHash)
```

判断用户输入是否和数据库里的哈希匹配。

## JWT 是什么

JWT 可以理解成一张“登录凭证”。用户登录成功后，后端生成一个字符串：

```text
eyJhbGciOiJIUz...
```

前端之后访问受保护接口时带上：

```text
Authorization: Bearer <token>
```

后端解析 token，知道当前用户是谁。

## 本阶段代码在哪里

认证接口：

- `module/auth/controller/AuthController.java`

认证业务：

- `module/auth/service/AuthService.java`
- `module/auth/service/AuthServiceImpl.java`
- `module/auth/service/CaptchaService.java`
- `module/auth/service/CaptchaServiceImpl.java`

认证响应：

- `module/auth/vo/CaptchaVO.java`

用户接口：

- `module/user/controller/UserController.java`

用户数据：

- `module/user/entity/UserEntity.java`
- `module/user/mapper/UserMapper.java`
- `module/user/service/UserServiceImpl.java`

安全基础设施：

- `security/JwtTokenProvider.java`
- `security/JwtAuthenticationFilter.java`
- `security/SecurityConfig.java`
- `security/UserContext.java`
- `security/LoginUser.java`

## 注册流程

用户访问：

```text
POST /api/auth/register
```

系统做这些事：

1. Controller 接收邮箱、昵称、密码。
2. Service 检查邮箱是否已注册。
3. BCrypt 加密密码。
4. 插入 `users` 表。
5. 初始化默认分类到 `categories` 表。
6. 生成 JWT。
7. 返回 token 和用户信息。

这里注册用户和初始化分类放在一个事务里。意思是：如果用户创建成功但分类初始化失败，整个注册会回滚，数据库不会留下半成品。

## 登录流程

用户访问：

```text
GET /api/auth/captcha
```

拿到 `captchaId` 和验证码图片后，再访问：

```text
POST /api/auth/login
```

系统做这些事：

1. Controller 接收邮箱、密码、验证码 ID 和验证码内容。
2. Service 先校验 Redis 中的验证码。
3. 根据邮箱查询用户。
4. 用 BCrypt 校验密码。
5. 检查用户状态是否正常。
6. 生成 JWT。
7. 返回登录结果。

验证码是一次性的。校验时无论成功还是失败，都会删除 Redis 中对应的验证码，防止反复猜测。

## JWT 校验流程

用户访问受保护接口，例如：

```text
GET /api/users/me
```

请求头带：

```text
Authorization: Bearer <token>
```

流程：

1. `JwtAuthenticationFilter` 拦截请求。
2. 从请求头取出 token。
3. `JwtTokenProvider` 解析 token。
4. 得到 `LoginUser`。
5. 放入 Spring Security 上下文。
6. 放入 `UserContext`。
7. Controller 和 Service 不需要手动解析 token，只要调用 `UserContext.getUserId()`。

这就是“统一用户上下文”。

## 为什么 Controller 不解析 token

如果每个 Controller 都自己解析 token，会重复、容易出错，也不利于维护。

本项目把解析逻辑放在过滤器中。业务代码只关心当前用户 ID：

```java
Long userId = UserContext.getUserId();
```

这让业务代码更干净。

## 阶段二学习重点

- 密码不能明文存储。
- JWT 是登录凭证，不是用户数据本身。
- 过滤器适合处理所有请求都要经过的逻辑。
- Controller 接参数，Service 写业务。
- 事务可以保证一组数据库操作要么都成功，要么都失败。
