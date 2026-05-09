# BirdHelp 后端设计文档

> 本文档仅覆盖 Java 传统业务后端。AI 生成相关功能放在另一个独立模块中。

---

## 一、模块总览

与 AI 模块的关系：Java 后端通过定义好的内部接口调用 AI 模块（另建工程），自身不实现任何 AI 逻辑。

---

## 二、用户模块 (user)

### 2.1 功能点

- 注册：手机号 + 验证码，邮箱 + 验证码
- 登录：手机号 + 密码，手机号 + 验证码，邮箱 + 密码，微信 openid 登录
- 个人信息：昵称、头像、学校、学院、专业、入学年份
- 身份认证：校园邮箱验证（.edu.cn 后缀），用于解锁学校专属模板(这个后期再考虑)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';****

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

### 3.4 核心表

```sql
user_quota (
    id BIGINT PK,
    user_id BIGINT FK,
    member_level TINYINT DEFAULT 0,  -- 0免费 1月卡 2季卡 3年卡
    member_expire_at DATETIME,       -- 会员到期时间
    daily_used INT DEFAULT 0,        -- 今日已用次数
    daily_date DATE,                 -- 已用次数对应日期，用于跨天判断
    created_at DATETIME,
    updated_at DATETIME
)

quota_config (           -- 额度配置表，后台管理维护
    id BIGINT PK,
    level TINYINT,       -- 0免费 1月卡 2季卡 3年卡
    daily_limit INT,     -- 每日上限
    updated_at DATETIME
)

quota_log (              -- 额度流水，方便对账
    id BIGINT PK,
    user_id BIGINT,
    change_type TINYINT, -- 1扣减 2退还
    project_id BIGINT,   -- 关联的项目ID
    created_at DATETIME
)
```

---

## 四、文件模块 (file)

### 4.1 功能点

- 文件上传（参考资料上传、模板素材上传、生成结果存储）
- 文件下载
- 文件列表：按类型筛选（PPT/Word/PDF）、按课程归档、按时间排序、分页
- 文件搜索：按文件名模糊搜索
- 回收站：软删除 → 30 天自动清理（定时任务）
- 文件恢复：从回收站恢复

### 4.2 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/file/upload | 上传文件 |
| GET | /api/file/{id}/download | 下载文件 |
| GET | /api/file/list | 文件列表（分页、类型筛选、课程筛选、排序） |
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
- 模板预览图：`template/preview/{template_id}.png`

### 4.4 核心表

```sql
file_record (
    id BIGINT PK,
    user_id BIGINT FK,
    project_id BIGINT,           -- 关联项目，可为空
    file_name VARCHAR(255),      -- 原始文件名
    file_type TINYINT,           -- 1PPT 2Word 3PDF 4图片 5其他
    file_size BIGINT,            -- 字节
    file_url VARCHAR(500),       -- 存储路径或 OSS URL
    source TINYINT,              -- 1用户上传 2AI生成
    course_name VARCHAR(100),    -- 归档课程名
    deleted TINYINT DEFAULT 0,   -- 0正常 1回收站
    deleted_at DATETIME,         -- 删除时间，用于30天清理
    created_at DATETIME,
    updated_at DATETIME
)
```

---

## 五、模板模块 (template)

### 5.1 模板分类

| 分类 | 说明 | 可见范围 |
|------|------|----------|
| 官方模板 | 平台通用模板 | 所有人 |
| 学校模板 | 某高校定制模板（含校徽、学院标识） | 该校认证用户 |
| 用户模板 | 用户自建/保存的模板 | 仅自己 |

### 5.2 功能点

- 模板列表：按分类、场景（学术汇报/开题答辩/毕业答辩/个人简历）筛选
- 收藏/取消收藏
- 我的收藏列表
- 用户自定义模板：保存模板配置
- 模板预览：提供预览图 URL
- 模板统计：下载量（后台更新）

### 5.3 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/template/list | 模板列表（分页、分类筛选、场景筛选） |
| GET | /api/template/{id} | 模板详情 |
| POST | /api/template/{id}/favorite | 收藏模板 |
| DELETE | /api/template/{id}/favorite | 取消收藏 |
| GET | /api/template/favorites | 我的收藏 |
| POST | /api/template/user | 创建自定义模板 |
| PUT | /api/template/user/{id} | 修改自定义模板 |
| DELETE | /api/template/user/{id} | 删除自定义模板 |
| GET | /api/template/user/list | 我的自定义模板列表 |

### 5.4 核心表

```sql
template (
    id BIGINT PK,
    name VARCHAR(100),
    category TINYINT,              -- 1官方 2学校 3用户
    scene TINYINT,                 -- 场景：1学术汇报 2开题答辩 3毕业答辩 4个人简历 5其他
    school_id BIGINT,              -- 关联学校，category=2时有效
    preview_url VARCHAR(500),      -- 预览图
    file_url VARCHAR(500),         -- 模板文件路径
    download_count INT DEFAULT 0,  -- 下载量
    status TINYINT DEFAULT 1,      -- 1上架 0下架
    created_at DATETIME,
    updated_at DATETIME
)

user_template (
    id BIGINT PK,
    user_id BIGINT FK,
    name VARCHAR(100),
    config_json TEXT,              -- 模板自定义配置（颜色、字体、布局等JSON）
    preview_url VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME
)

template_favorite (
    id BIGINT PK,
    user_id BIGINT FK,
    template_id BIGINT FK,
    created_at DATETIME,
    UNIQUE(user_id, template_id)
)
```

---

## 六、项目模块 (project) / 课程管理

### 6.1 设计思路

"项目"是用户一次文档生成操作的抽象。一个项目包含：用户输入的上下文信息、AI 生成结果、关联的文件、所属课程。课程是一个标签概念，用户可自定义课程名，用于归档管理。

### 6.2 功能点

- 创建项目（AI 模块发起，传入用户输入和生成参数）
- 更新项目状态（生成中 → 完成 → 失败）
- 项目列表：按课程、类型、时间筛选
- 项目详情：包含生成内容、关联文件
- 删除项目
- 课程列表：用户使用过的所有课程名（去重）

### 6.3 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/project | 创建项目（AI模块调用，传入生成参数） |
| PUT | /api/project/{id} | 更新项目（AI模块回调，更新状态和结果） |
| GET | /api/project/list | 我的项目列表（分页、类型筛选、课程筛选） |
| GET | /api/project/{id} | 项目详情 |
| DELETE | /api/project/{id} | 删除项目 |
| GET | /api/project/courses | 我的课程标签列表（去重后的课程名） |

### 6.4 核心表

```sql
project (
    id BIGINT PK,
    user_id BIGINT FK,
    title VARCHAR(200),           -- 项目标题（如 "近代史纲要期末汇报"）
    doc_type TINYINT,             -- 文档类型：1PPT 2Word 3PDF
    course_name VARCHAR(100),     -- 课程名（用户自定义标签）
    input_content TEXT,           -- 用户输入的原始内容（主题、关键词等JSON）
    input_files VARCHAR(1000),    -- 用户上传的参考文件ID，逗号分隔
    template_id BIGINT,           -- 使用的模板ID
    status TINYINT DEFAULT 0,     -- 0生成中 1完成 2失败
    result_summary VARCHAR(500),  -- 生成结果摘要（AI模块回传）
    created_at DATETIME,
    updated_at DATETIME
)
```

---

## 七、会员模块 (member)

### 7.1 功能点

- 套餐列表：会员套餐配置（后台管理维护）
- 创建订单：选择套餐 → 生成订单
- 支付回调：收到支付成功通知 → 激活会员
- 会员状态查询：当前等级、到期时间
- 过期处理：定时任务检查过期会员 → 回退到免费等级

### 7.2 支付流程

```
用户选套餐 → 创建订单(pending) → 调起支付 → 支付回调 → 更新订单(paid) → 激活会员
                                                     → 支付失败 → 订单过期(expired)
```

初期先做微信支付（JSAPI），后续扩展支付宝。

### 7.3 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/member/plans | 套餐列表 |
| POST | /api/member/order | 创建订单 |
| GET | /api/member/order/{id} | 订单详情 |
| GET | /api/member/orders | 我的订单列表 |
| POST | /api/member/pay-callback | 支付回调（第三方调用） |
| GET | /api/member/status | 当前会员状态 |

### 7.4 核心表

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

## 八、消息通知模块 (notify)

### 8.1 功能点

- 系统通知：生成完成、额度不足提醒、会员到期提醒
- 通知列表：分页、已读/未读状态
- 标记已读：单条 / 全部
- 未读数量

### 8.2 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/notify/list | 通知列表（分页） |
| GET | /api/notify/unread-count | 未读数量 |
| PUT | /api/notify/{id}/read | 标记单条已读 |
| PUT | /api/notify/read-all | 全部已读 |

### 8.3 核心表

```sql
notification (
    id BIGINT PK,
    user_id BIGINT FK,
    title VARCHAR(200),
    content VARCHAR(1000),
    type TINYINT,                 -- 1生成完成 2额度不足 3会员到期 4系统通知
    related_id BIGINT,            -- 关联业务ID（如 project_id）
    read_flag TINYINT DEFAULT 0,
    created_at DATETIME
)
```

---

## 九、与 AI 模块的对接

### 9.1 职责边界

```
Java 后端（本工程）                  AI 模块（另建工程）
─────────────────                    ─────────────────
用户认证/权限校验                     调用大模型 API
额度校验与扣减                        Prompt 组装
文件存储                             文档内容生成
模板管理                             PPT/Word/PDF 文件生成
项目记录维护                         语音转文字
订单支付                             OCR 识别
消息通知
```

### 9.2 内部调用协议

Java 后端暴露给 AI 模块的内部接口（通过 HTTP，可加内部 token 校验）：

| 方法 | 路径 | 用途 |
|------|------|------|
| POST | /internal/quota/consume | AI 生成前扣减额度 |
| POST | /internal/quota/refund | 生成失败退还额度 |
| POST | /internal/project | 创建项目记录 |
| PUT | /internal/project/{id} | 更新项目状态（完成/失败） |
| POST | /internal/file/upload | AI 模块上传生成结果文件 |

AI 模块暴露给 Java 后端的接口（由前端直接调用，或 Java 后端代理转发）：

| 方法 | 路径 | 用途 |
|------|------|------|
| POST | /ai/ppt/generate | 生成 PPT |
| POST | /ai/word/generate | 生成 Word |
| POST | /ai/pdf/generate | 生成 PDF |
| POST | /ai/chat/modify | 对话式修改文档 |

### 9.3 一次生成请求的完整调用链

```
前端 → Java后端(/api/project) → 额度校验扣减 → 创建project记录
     → 调用AI模块(/ai/ppt/generate)
     → AI模块生成内容 → 调用Java后端(/internal/file/upload) 保存结果
     → 调用Java后端(/internal/project/{id}) 更新状态
     → Java后端发送完成通知
```

前端轮询 project 状态或通过 WebSocket 推送状态变更。

---

## 十、技术选型

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

## 十一、工程结构

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
│   ├── template/                  -- 模板模块
│   ├── project/                   -- 项目模块
│   ├── member/                    -- 会员模块
│   └── notify/                    -- 通知模块
└── internal/                      -- 内部接口（给AI模块调用）
    ├── QuotaInternalController.java
    ├── ProjectInternalController.java
    └── FileInternalController.java
```

---

## 十二、开发顺序

### 第一阶段：基础骨架
- [x] 工程初始化
- [ ] 公共组件（统一响应、全局异常、JWT 鉴权）
- [ ] 用户模块（注册、登录、个人信息）

### 第二阶段：核心业务
- [ ] 额度模块（查询、扣减、退还、每日重置）
- [ ] 文件模块（上传、下载、列表、删除）
- [ ] 项目模块（CRUD、课程标签）

### 第三阶段：扩展功能
- [ ] 模板模块（列表、收藏、用户模板）
- [ ] 内部接口（对接 AI 模块）
- [ ] 消息通知

### 第四阶段：商业化
- [ ] 会员模块（套餐、订单、支付回调）
