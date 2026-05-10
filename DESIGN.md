# BirdHelp 后端设计文档

> 本文档仅覆盖 Java 传统业务后端。AI 生成相关功能放在另一个独立模块中。

---

## 一、模块总览

与 AI 模块的关系：Java 后端通过定义好的内部接口调用 AI 模块（另建工程），自身不实现任何 AI 逻辑。

当前模块：用户、额度、文件、会员、AI 对接。

---

## 二、用户模块 (user)

### 2.1 功能点

- 注册：手机号 + 验证码，邮箱 + 验证码
- 登录：手机号 + 密码，手机号 + 验证码，邮箱 + 密码，微信 openid 登录
- 个人信息：昵称、头像、学校、学院、专业、入学年份
- 密码管理：修改密码、重置密码（验证码）

### 2.2 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/register/phone | 手机号注册 |
| POST | /api/user/register/email | 邮箱注册 |
| POST | /api/user/login/password | 密码登录 |
| POST | /api/user/login/sms | 短信验证码登录 |
| POST | /api/user/login/wechat | 微信登录 |
| POST | /api/user/send-code | 发送验证码（注册/登录/重置密码复用） |
| GET | /api/user/info | 获取个人信息 |
| PUT | /api/user/info | 修改个人信息 |
| PUT | /api/user/password | 修改密码 |
| POST | /api/user/reset-password | 重置密码 |
| POST | /api/user/avatar | 上传头像 |

### 2.3 核心表

```sql
CREATE TABLE `sys_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键用户ID',
    `tenant_id` bigint DEFAULT 0 COMMENT '租户ID，多租户预留',
    `username` varchar(50) NOT NULL COMMENT '登录账号',
    `nickname` varchar(50) NOT NULL COMMENT '用户昵称',
    `password` varchar(100) NOT NULL COMMENT '加密密码',
    `avatar` varchar(255) DEFAULT '' COMMENT '头像地址',
    `phone` varchar(20) DEFAULT '' COMMENT '手机号',
    `email` varchar(100) DEFAULT '' COMMENT '邮箱',
    `sex` tinyint DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    `birthday` date DEFAULT NULL COMMENT '出生日期',
    `user_type` tinyint DEFAULT 1 COMMENT '用户类型 1-普通学生 2-管理员',
    `status` tinyint DEFAULT 1 COMMENT '账号状态 0-禁用 1-正常',
    `wx_openid` varchar(100) DEFAULT '' COMMENT '微信openid',
    `wx_unionid` varchar(100) DEFAULT '' COMMENT '微信unionid',

    -- 标准审计字段
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `version` int DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
```

---

## 三、额度模块 (quota)

### 3.1 额度体系

每个用户有两种额度来源（取最大值生效，不叠加）：
- **免费额度**：所有用户默认拥有，每日重置
- **会员额度**：购买会员后生效，每日重置，会员过期后回退到免费额度

生成一次文档扣减 1 次额度，每次扣减时先查会员额度，会员额度用尽或过期则消耗免费额度。

不存储"剩余次数"字段，而是存储"已用次数"，额度查询时实时计算 `当日额度上限 - 当日已用次数`。

### 3.2 功能点

- 每日额度上限配置（免费 / 各等级会员，可后台动态调整）
- 额度扣减（生成任务提交时调用）
- 额度退还（生成失败时调用）
- 额度查询
- 每日 0 点重置已用次数（定时任务）

### 3.3 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/quota/my | 查询当前用户额度（今日剩余、总额度上限、会员等级） |
| POST | /api/quota/consume | 扣减额度（内部调用，由 AI 模块发起） |
| POST | /api/quota/refund | 退还额度（内部调用，生成失败时） |

### 3.4 额度配置预设

| 等级 | 名称 | 每日上限 |
|------|------|----------|
| 0 | 免费用户 | 10 次 |
| 1 | 月卡 | 30 次 |
| 2 | 季卡 | 60 次 |
| 3 | 年卡 | 100 次 |

### 3.5 核心表

```sql
-- 用户额度表
CREATE TABLE `user_quota` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `member_level` tinyint DEFAULT 0 COMMENT '会员等级 0-免费 1-月卡 2-季卡 3-年卡',
    `member_expire_at` datetime DEFAULT NULL COMMENT '会员到期时间',
    `daily_used` int DEFAULT 0 COMMENT '今日已用次数',
    `daily_date` date DEFAULT NULL COMMENT '已用次数对应日期，用于跨天判断',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户额度表';

-- 额度配置表，后台管理维护
CREATE TABLE `quota_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `level` tinyint NOT NULL COMMENT '会员等级 0-免费 1-月卡 2-季卡 3-年卡',
    `daily_limit` int NOT NULL COMMENT '每日生成次数上限',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='额度配置表';

-- 额度流水表，用于对账追溯
CREATE TABLE `quota_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `change_type` tinyint NOT NULL COMMENT '变更类型 1-扣减 2-退还',
    `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
    `del_flag` tinyint DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='额度流水表';
```

---

## 四、文件模块 (file)

### 4.1 功能点

- 文件上传（参考资料上传、生成结果存储）
- 文件下载
- 文件列表：按类型筛选（PPT/Word/PDF）、按时间排序、分页
- 文件搜索：按文件名模糊搜索
- 回收站：软删除 → 30 天自动清理（定时任务）
- 文件恢复：从回收站恢复

### 4.2 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/file/upload | 上传文件 |
| GET | /api/file/{id}/download | 下载文件 |
| GET | /api/file/list | 文件列表（分页、类型筛选、排序） |
| GET | /api/file/search | 文件搜索 |
| DELETE | /api/file/{id} | 删除文件（移入回收站） |
| PUT | /api/file/{id}/restore | 从回收站恢复 |
| DELETE | /api/file/{id}/permanent | 永久删除 |
| GET | /api/file/recycle | 回收站列表 |

### 4.3 文件存储策略

- 本地开发：存储到本地磁盘（配置目录 `app.upload-dir`）
- 生产环境：阿里云 OSS（`app.oss.enabled=true` 时启用）
- 路径规则：`{file_type}/{yyyy-MM}/{uuid}.{ext}`
  - 示例：`ppt/2026-05/a1b2c3d4.pptx`

### 4.4 核心表

```sql
file_record (
    id BIGINT PK,
    user_id BIGINT FK,
    file_name VARCHAR(255),      -- 原始文件名
    file_type TINYINT,           -- 1PPT 2Word 3PDF 4图片 5其他
    file_size BIGINT,            -- 字节
    file_url VARCHAR(500),       -- 存储路径或 OSS URL
    source TINYINT,              -- 1用户上传 2AI生成
    deleted TINYINT DEFAULT 0,   -- 0正常 1回收站
    deleted_at DATETIME,         -- 删除时间，用于30天清理
    created_at DATETIME,
    updated_at DATETIME
)
```

---

## 五、会员模块 (member)

### 5.1 功能点

- 套餐列表：会员套餐配置（后台管理维护）
- 创建订单：选择套餐 → 生成订单
- 支付回调：收到支付成功通知 → 激活会员
- 会员状态查询：当前等级、到期时间
- 过期处理：定时任务检查过期会员 → 回退到免费等级

### 5.2 支付流程

```
用户选套餐 → 创建订单(pending) → 调起支付 → 支付回调 → 更新订单(paid) → 激活会员
                                                    → 支付失败 → 订单过期(expired)
```

初期先做微信支付（JSAPI），后续扩展支付宝。

### 5.3 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/member/plans | 套餐列表 |
| POST | /api/member/order | 创建订单 |
| GET | /api/member/order/{id} | 订单详情 |
| GET | /api/member/orders | 我的订单列表 |
| POST | /api/member/pay-callback | 支付回调（第三方调用） |
| GET | /api/member/status | 当前会员状态 |

### 5.4 核心表

```sql
member_plan (
    id BIGINT PK,
    name VARCHAR(50),             -- "月卡"
    level TINYINT,                -- 1月卡 2季卡 3年卡
    price DECIMAL(10,2),          -- 原价
    actual_price DECIMAL(10,2),   -- 实际售价
    duration_days INT,            -- 有效天数
    daily_limit INT,              -- 每日生成次数上限
    status TINYINT DEFAULT 1,     -- 1上架 0下架
    created_at DATETIME
)

member_order (
    id BIGINT PK,
    order_no VARCHAR(32) UNIQUE,  -- 订单号
    user_id BIGINT FK,
    plan_id BIGINT FK,
    amount DECIMAL(10,2),
    pay_type TINYINT,             -- 1微信 2支付宝
    status TINYINT DEFAULT 0,     -- 0待支付 1已支付 2已过期(超时未付)
    paid_at DATETIME,
    expire_at DATETIME,           -- 订单过期时间（创建后15分钟未付则过期）
    created_at DATETIME
)
```

---

## 六、与 AI 模块的对接

### 6.1 职责边界

```
Java 后端（本工程）                  AI 模块（另建工程）
─────────────────                    ─────────────────
用户认证/权限校验                     调用大模型 API
额度校验与扣减                        Prompt 组装
文件存储                             文档内容生成
会员管理                             PPT/Word/PDF 文件生成
                                     语音转文字
                                     OCR 识别
```

### 6.2 内部调用协议

Java 后端暴露给 AI 模块的内部接口（通过 HTTP，可加内部 token 校验）：

| 方法 | 路径 | 用途 |
|------|------|------|
| POST | /internal/quota/consume | AI 生成前扣减额度 |
| POST | /internal/quota/refund | 生成失败退还额度 |
| POST | /internal/file/upload | AI 模块上传生成结果文件 |

AI 模块暴露给 Java 后端的接口（由前端直接调用，或 Java 后端代理转发）：

| 方法 | 路径 | 用途 |
|------|------|------|
| POST | /ai/ppt/generate | 生成 PPT |
| POST | /ai/word/generate | 生成 Word |
| POST | /ai/pdf/generate | 生成 PDF |
| POST | /ai/chat/modify | 对话式修改文档 |

### 6.3 一次生成请求的完整调用链

```
前端 → Java后端(/api/quota/consume) → 额度校验扣减
     → 调用AI模块(/ai/ppt/generate)
     → AI模块生成内容 → 调用Java后端(/internal/file/upload) 保存结果
     → Java后端返回文件信息
```

---

## 七、技术选型

| 组件 | 选型 | 说明 |
|------|------|------|
| 框架 | Spring Boot 2.6.13 | 已搭建 |
| ORM | MyBatis-Plus | 简化 CRUD |
| 数据库 | MySQL 8.0 | 主存储 |
| 缓存 | Redis | 验证码、会话、额度每日计数 |
| 认证 | Spring Security + JWT | 无状态鉴权 |
| 文件存储 | MinIO（本地）/ 阿里云 OSS（生产） | 可切换 |
| 短信 | 阿里云短信 / 腾讯云短信 | 发送验证码 |
| 支付 | 微信支付 API V3 | 会员支付 |
| 定时任务 | Spring Task / XXL-Job | 额度重置、回收站清理、会员过期 |
| API 文档 | Knife4j (Swagger) | 接口文档生成 |

---

## 八、工程结构

```
src/main/java/com/greendam/birdhelp/
├── BirdHelpApplication.java
├── common/                        -- 公共组件
│   ├── config/                    -- 配置类（Security, Redis, OSS, MyBatis等）
│   ├── exception/                 -- 全局异常处理
│   ├── result/                    -- 统一响应体 R<T>
│   └── util/                      -- 工具类
├── module/
│   ├── user/                      -- 用户模块
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   ├── quota/                     -- 额度模块
│   ├── file/                      -- 文件模块
│   └── member/                    -- 会员模块
└── internal/                      -- 内部接口（给AI模块调用）
    ├── QuotaInternalController.java
    └── FileInternalController.java
```

---

## 九、开发顺序

### 第一阶段：基础骨架 ✅
- [x] 工程初始化
- [x] 公共组件（统一响应、全局异常、JWT 鉴权）
- [x] 用户模块（注册、登录、个人信息）

### 第二阶段：核心业务
- [x] 额度模块（查询、扣减、退还、每日重置）
- [ ] 文件模块（上传、下载、列表、删除）
- [ ] 内部接口（对接 AI 模块）

### 第三阶段：商业化
- [ ] 会员模块（套餐、订单、支付回调）
