# BirdHelp 前端 API 调用文档

> 基础地址：`http://{host}:7890/api`
>
> 内容类型：`application/json`（文件上传类接口使用 `multipart/form-data`）
>
> 统一响应格式：`{"code": 0, "data": ..., "message": "success"}`

---

## 一、鉴权说明

登录成功后，客户端需将返回的 `token` 存入本地，后续需鉴权的接口在请求头中携带：

```
token: {JWT Token}
```

| 模块                                  | 是否需要 Token  |
|-------------------------------------|:-----------:|
| 注册、登录、发送验证码、重置密码                    |      否      |
| 用户信息、项目管理、文件管理、PPT/Word/PDF 生成、额度查询 |      是      |
| 管理员后台所有接口                           | Admin Token |
| 用户端公告查询                             |      否      |

管理员登录成功后，客户端需使用返回的 token，在后续请求头中携带：

```
admin-token: {Admin JWT Token}
```

---

## 二、用户模块 — `/user`

### 2.1 发送验证码

```
POST /user/send-code
```

| 参数       | 类型     | 必填 | 说明                             |
|----------|--------|:--:|--------------------------------|
| `target` | string | 是  | 手机号或邮箱                         |
| `type`   | string | 是  | `register` / `login` / `reset` |

```json
{
  "target": "13800138000",
  "type": "register"
}
```

响应：`BaseResponse<Void>`

### 2.2 手机号注册

```
POST /user/register/phone
```

| 参数         | 类型     | 必填 | 说明         |
|------------|--------|:--:|------------|
| `phone`    | string | 是  | 11 位手机号    |
| `code`     | string | 是  | 短信验证码      |
| `username` | string | 是  | 用户名，2-50 位 |
| `password` | string | 是  | 密码，6-100 位 |
| `nickname` | string | 是  | 昵称，最长 50 位 |

```json
{
  "phone": "13800138000",
  "code": "123456",
  "username": "zhangsan",
  "password": "abc123",
  "nickname": "张三"
}
```

响应：`BaseResponse<Void>`

### 2.3 邮箱注册

```
POST /user/register/email
```

| 参数         | 类型     | 必填 | 说明         |
|------------|--------|:--:|------------|
| `email`    | string | 是  | 邮箱地址       |
| `code`     | string | 是  | 邮箱验证码      |
| `username` | string | 是  | 用户名，2-50 位 |
| `password` | string | 是  | 密码，6-100 位 |
| `nickname` | string | 是  | 昵称，最长 50 位 |

### 2.4 密码登录

```
POST /user/login/password
```

| 参数         | 类型     | 必填 | 说明             |
|------------|--------|:--:|----------------|
| `account`  | string | 是  | 手机号 / 邮箱 / 用户名 |
| `password` | string | 是  | 密码             |

```json
{
  "account": "zhangsan",
  "password": "abc123"
}
```

响应 `BaseResponse<LoginVO>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": "1",
      "username": "zhangsan",
      "nickname": "张三",
      "avatar": null,
      "phone": "13800138000",
      "email": null,
      "sex": 0,
      "birthday": null,
      "userType": 1,
      "status": 1,
      "createTime": "2026-05-16T10:00:00"
    }
  }
}
```

### 2.5 获取个人信息

> 需 Token

```
GET /user/info
```

响应 `BaseResponse<UserInfoVO>`

### 2.6 修改个人信息

> 需 Token，所有字段可选

```
PUT /user/info
```

| 参数         | 类型     | 必填 | 说明           |
|------------|--------|:--:|--------------|
| `nickname` | string | 否  | 昵称，最长 50 位   |
| `avatar`   | string | 否  | 头像 URL       |
| `sex`      | int    | 否  | 0-未知 1-男 2-女 |
| `birthday` | string | 否  | `yyyy-MM-dd` |

### 2.7 修改密码

> 需 Token

```
PUT /user/password
```

| 参数            | 类型     | 必填 | 说明          |
|---------------|--------|:--:|-------------|
| `oldPassword` | string | 是  | 原密码         |
| `newPassword` | string | 是  | 新密码，6-100 位 |

### 2.8 重置密码（忘记密码）

```
POST /user/reset-password
```

| 参数            | 类型     | 必填 | 说明          |
|---------------|--------|:--:|-------------|
| `account`     | string | 是  | 手机号或邮箱      |
| `code`        | string | 是  | 验证码         |
| `newPassword` | string | 是  | 新密码，6-100 位 |

### 2.9 上传头像

> 需 Token，multipart/form-data

```
POST /user/avatar
```

| 参数     | 类型   | 必填 | 说明   |
|--------|------|:--:|------|
| `file` | file | 是  | 图片文件 |

响应 `BaseResponse<String>`，data 为头像的 OSS URL。

---

## 三、项目模块 — `/project`

> 所有接口需 Token

### 3.1 创建项目

```
POST /project
```

| 参数            | 类型     | 必填 | 说明             |
|---------------|--------|:--:|----------------|
| `name`        | string | 是  | 项目名称，最长 100 字符 |
| `description` | string | 否  | 项目描述，最长 500 字符 |

响应 `BaseResponse<ProjectVO>`：

```json
{
  "code": 0,
  "data": {
    "id": "1",
    "name": "Java课程设计",
    "description": "大二Java课件制作",
    "status": 1,
    "fileCount": 0,
    "createTime": "2026-05-16T10:30:00",
    "updateTime": "2026-05-16T10:30:00"
  }
}
```

### 3.2 项目列表

```
GET /project/list?page=1&size=10
```

| 参数     | 类型  | 必填 | 说明         |
|--------|-----|:--:|------------|
| `page` | int | 否  | 页码，默认 1    |
| `size` | int | 否  | 每页条数，默认 10 |

响应 `BaseResponse<Page<ProjectVO>>`：

```json
{
  "code": 0,
  "data": {
    "current": 1,
    "size": 10,
    "total": 5,
    "records": [
      ...
    ]
  }
}
```

### 3.3 项目详情

```
GET /project/{id}
```

### 3.4 编辑项目

```
PUT /project/{id}
```

| 参数            | 类型     | 必填 | 说明   |
|---------------|--------|:--:|------|
| `name`        | string | 否  | 项目名称 |
| `description` | string | 否  | 项目描述 |

### 3.5 删除项目

> 级联将项目下文件移入回收站

```
DELETE /project/{id}
```

### 3.6 归档项目

```
PUT /project/{id}/archive
```

### 3.7 激活项目

```
PUT /project/{id}/activate
```

---

## 四、文件模块 — `/file`

> 所有接口需 Token

### 4.1 上传文件

> multipart/form-data

```
POST /file/upload
```

| 参数          | 类型   | 必填 | 说明    |
|-------------|------|:--:|-------|
| `file`      | file | 是  | 文件    |
| `projectId` | long | 是  | 项目 ID |

响应 `BaseResponse<FileRecordVO>`：

```json
{
  "code": 0,
  "data": {
    "id": "42",
    "projectId": "1",
    "fileName": "课件素材.pdf",
    "fileType": 3,
    "fileSize": 1024000,
    "source": 1,
    "deleted": 0,
    "deletedAt": null,
    "createTime": "2026-05-16T11:00:00"
  }
}
```

| fileType | 含义   |
|:--------:|------|
|    1     | PPT  |
|    2     | Word |
|    3     | PDF  |
|    4     | 图片   |
|    5     | 其他   |

### 4.2 文件列表

```
GET /file/list?projectId=1&page=1&size=10&fileType=3
```

| 参数          | 类型   | 必填 | 说明         |
|-------------|------|:--:|------------|
| `projectId` | long | 是  | 项目 ID      |
| `page`      | int  | 否  | 页码，默认 1    |
| `size`      | int  | 否  | 每页条数，默认 10 |
| `fileType`  | int  | 否  | 筛选文件类型     |

### 4.3 搜索文件

```
GET /file/search?projectId=1&keyword=课件&page=1&size=10
```

| 参数          | 类型     | 必填 | 说明    |
|-------------|--------|:--:|-------|
| `projectId` | long   | 是  | 项目 ID |
| `keyword`   | string | 是  | 搜索关键词 |
| `page`      | int    | 否  | 页码    |
| `size`      | int    | 否  | 每页条数  |

### 4.4 下载文件

```
GET /file/{id}/download
```

响应为文件流（`Content-Type: application/octet-stream`）。

### 4.5 删除文件（移入回收站）

```
DELETE /file/{id}
```

### 4.6 从回收站恢复

```
PUT /file/{id}/restore
```

### 4.7 永久删除

> 删除物理文件 + 数据库记录

```
DELETE /file/{id}/permanent
```

### 4.8 回收站列表

```
GET /file/recycle?projectId=1&page=1&size=10
```

| 参数          | 类型   | 必填 | 说明         |
|-------------|------|:--:|------------|
| `projectId` | long | 是  | 项目 ID      |
| `page`      | int  | 否  | 页码，默认 1    |
| `size`      | int  | 否  | 每页条数，默认 10 |

---

## 五、模型查询 — `/model`

> 需 Token

### 5.1 获取可用模型列表

```
GET /model/list
```

返回已启用的大语言模型列表，不包含 `apiKey` 和 `baseUrl` 等敏感信息，供前端渲染模型选择下拉框。

响应 `BaseResponse<List<Map<String, String>>>`：

```json
{
  "code": 0,
  "data": [
    {
      "modelName": "gpt-4o",
      "providerName": "openai",
      "description": "OpenAI GPT-4o 生产密钥"
    },
    {
      "modelName": "deepseek-chat",
      "providerName": "deepseek",
      "description": ""
    }
  ]
}
```

---

## 六、PPT 生成模块 — `/ppt`

> 需 Token。**异步接口**，提交任务后立即返回 `taskId`，生成完成后通过回调更新状态。
>
> 前端可通过 `taskId` 轮询任务结果（详见 5.2 节）。

### 5.1 提交 PPT 生成任务

```
POST /ppt/generate
```

| 参数             | 类型     | 必填 | 默认值        | 说明                                                                                   |
|----------------|--------|:--:|------------|--------------------------------------------------------------------------------------|
| `projectId`    | string | 是  | —          | 项目 ID                                                                                |
| `topic`        | string | 是  | —          | PPT 主题，最长 200 字符                                                                     |
| `modelName`    | string | 否  | `gpt-4o`   | 使用的 LLM 模型名称，默认由后端根据可用密钥自动选择                                                         |
| `language`     | string | 否  | `zh`       | `zh` 中文 / `en` 英文                                                                    |
| `style`        | string | 否  | `academic` | `academic` 学术 / `business` 商务 / `creative` 创意 / `minimal` 极简 / `tech` 科技 / `warm` 暖色 |
| `slideCount`   | int    | 否  | `10`       | 页数（含封面和结束页），范围 1–50                                                                  |
| `extraPrompt`  | string | 否  | —          | 补充指令，最长 500 字符                                                                       |
| `enableImages` | bool   | 否  | `true`     | 是否自动搜索配图（Unsplash → Pexels → 纯色占位图降级）                                                |
| `materialIds`  | list   | 否  | `[]`       | 参考素材的 `javaFileId` 列表                                                                |
| `ragEnabled`   | bool   | 否  | `false`    | 是否启用 RAG 检索增强                                                                        |

```json
{
  "projectId": "1",
  "topic": "Java基础语法教学",
  "modelName": "gpt-4o",
  "language": "zh",
  "style": "academic",
  "slideCount": 10,
  "extraPrompt": "重点讲解面向对象三大特性",
  "enableImages": true,
  "materialIds": [
    "42",
    "43"
  ],
  "ragEnabled": true
}
```

响应 `BaseResponse<DocGenerateTaskVO>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "pending",
    "callbackId": "req_20260523_001"
  }
}
```

| 字段           | 类型     | 说明                         |
|--------------|--------|----------------------------|
| `taskId`     | string | 任务唯一 ID（UUID v4），用于追踪和查询结果 |
| `status`     | string | 固定 `"pending"`，表示任务已提交     |
| `callbackId` | string | 业务流水 ID，用于关联请求             |

可能的错误码：

| 场景   | message      |
|------|--------------|
| 系统繁忙 | `系统繁忙，请稍后重试` |

### 5.2 查询任务结果（通用）

> 需 Token。以下接口适用于 PPT / Word / PDF 三种文档类型。

```
GET /task/{taskId}
```

**响应 `BaseResponse<TaskStatusVO>`：**

**pending（任务已提交，尚未开始处理）：**

```json
{
  "code": 0,
  "data": {
    "taskId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "pending"
  }
}
```

**processing（AI 模块正在生成，Python 周期性推送进度）：**

```json
{
  "code": 0,
  "data": {
    "taskId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "processing",
    "stage": "running_qa",
    "progress": 65,
    "message": "正在质量评审：第 10/15 页"
  }
}
```

**completed（生成完成，文件已上传至存储）：**

```json
{
  "code": 0,
  "data": {
    "taskId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "completed",
    "fileId": "128",
    "fileUrl": "https://storage.example.com/files/128.pptx",
    "fileName": "Java基础语法教学.pptx",
    "qaLowestScore": 72,
    "qaPassedCount": 14,
    "qaTotalCount": 15
  }
}
```

**failed（生成失败）：**

```json
{
  "code": 0,
  "data": {
    "taskId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "failed",
    "errorCode": 5002,
    "errorMessage": "大纲验证失败：缺少主标题字段"
  }
}
```

**进度阶段枚举（`stage`）：**

| stage                | 说明             |
|----------------------|----------------|
| `retrieving_context` | 检索知识库上下文       |
| `generating_outline` | 生成大纲           |
| `validating_outline` | 验证大纲           |
| `rendering_charts`   | 渲染图表（Word/PDF） |
| `fetching_images`    | 搜索配图           |
| `running_qa`         | 质量评审           |
| `building_document`  | 构建文档           |
| `uploading_file`     | 上传文件           |

> 前端轮询建议：提交任务后每 1–2 秒查询一次，直到 `status` 变为 `completed` 或 `failed`。结果缓存 24 小时。

---

## 七、Word 生成模块 — `/word`

> 需 Token。**异步接口**，提交任务后立即返回 `taskId`，生成完成后通过回调更新状态。

### 6.1 提交 Word 生成任务

```
POST /word/generate
```

| 参数             | 类型     | 必填 | 默认值        | 说明                                                                                   |
|----------------|--------|:--:|------------|--------------------------------------------------------------------------------------|
| `projectId`    | string | 是  | —          | 项目 ID                                                                                |
| `topic`        | string | 是  | —          | 文档主题，最长 200 字符                                                                       |
| `modelName`    | string | 否  | `gpt-4o`   | 使用的 LLM 模型名称，默认由后端根据可用密钥自动选择                                                         |
| `language`     | string | 否  | `zh`       | `zh` 中文 / `en` 英文                                                                    |
| `docType`      | string | 否  | `essay`    | `essay` 论文 / `report` 报告 / `letter` 信函 / `paper` 学术论文                                |
| `wordCount`    | int    | 否  | `2000`     | 目标字数，范围 500–10000                                                                    |
| `style`        | string | 否  | `academic` | `academic` 学术 / `business` 商务 / `creative` 创意 / `minimal` 极简 / `tech` 科技 / `warm` 暖色 |
| `extraPrompt`  | string | 否  | —          | 补充指令，最长 500 字符                                                                       |
| `enableImages` | bool   | 否  | `true`     | 是否自动搜索配图（Unsplash → Pexels → 纯色占位图降级）                                                |
| `materialIds`  | list   | 否  | `[]`       | 参考素材的 `javaFileId` 列表                                                                |
| `ragEnabled`   | bool   | 否  | `false`    | 是否启用 RAG 检索增强                                                                        |

```json
{
  "projectId": "1",
  "topic": "人工智能发展报告",
  "modelName": "gpt-4o",
  "language": "zh",
  "docType": "report",
  "wordCount": 3000,
  "style": "tech",
  "extraPrompt": "重点阐述深度学习部分",
  "enableImages": true,
  "materialIds": [
    "42",
    "43"
  ],
  "ragEnabled": true
}
```

响应 `BaseResponse<DocGenerateTaskVO>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "status": "pending",
    "callbackId": "req_20260523_002"
  }
}
```

可能的错误码：

| 场景   | message      |
|------|--------------|
| 系统繁忙 | `系统繁忙，请稍后重试` |

---

## 八、PDF 生成模块 — `/pdf`

> 需 Token。**异步接口**，提交任务后立即返回 `taskId`，生成完成后通过回调更新状态。

### 7.1 提交 PDF 生成任务

```
POST /pdf/generate
```

| 参数             | 类型     | 必填 | 默认值        | 说明                                                                                   |
|----------------|--------|:--:|------------|--------------------------------------------------------------------------------------|
| `projectId`    | string | 是  | —          | 项目 ID                                                                                |
| `topic`        | string | 是  | —          | 文档主题，最长 200 字符                                                                       |
| `modelName`    | string | 否  | `gpt-4o`   | 使用的 LLM 模型名称，默认由后端根据可用密钥自动选择                                                         |
| `language`     | string | 否  | `zh`       | `zh` 中文 / `en` 英文                                                                    |
| `docType`      | string | 否  | `report`   | `report` 报告 / `resume` 简历 / `form` 表单                                                |
| `style`        | string | 否  | `academic` | `academic` 学术 / `business` 商务 / `creative` 创意 / `minimal` 极简 / `tech` 科技 / `warm` 暖色 |
| `extraPrompt`  | string | 否  | —          | 补充指令，最长 500 字符                                                                       |
| `enableImages` | bool   | 否  | `true`     | 是否自动搜索配图（Unsplash → Pexels → 纯色占位图降级）                                                |
| `materialIds`  | list   | 否  | `[]`       | 参考素材的 `javaFileId` 列表                                                                |
| `ragEnabled`   | bool   | 否  | `false`    | 是否启用 RAG 检索增强                                                                        |

```json
{
  "projectId": "1",
  "topic": "年度工作总结",
  "modelName": "gpt-4o",
  "language": "zh",
  "docType": "report",
  "style": "business",
  "extraPrompt": "突出Q3业绩数据",
  "enableImages": true,
  "materialIds": [
    "42",
    "43"
  ],
  "ragEnabled": true
}
```

响应 `BaseResponse<DocGenerateTaskVO>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "taskId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "status": "pending",
    "callbackId": "req_20260523_003"
  }
}
```

可能的错误码：

| 场景   | message      |
|------|--------------|
| 系统繁忙 | `系统繁忙，请稍后重试` |

---

## 九、额度模块 — `/quota`

> 需 Token

### 8.1 查询我的额度

```
GET /quota/my
```

响应 `BaseResponse<QuotaInfoVO>`：

```json
{
  "code": 0,
  "data": {
    "todayRemaining": 15,
    "dailyLimit": 20,
    "dailyUsed": 5,
    "memberLevel": 1,
    "memberExpireAt": "2026-06-16T10:00:00"
  }
}
```

---

## 十、管理员后台 — 认证

### 9.1 管理员登录

```
POST /admin/login
```

| 参数       | 类型     | 必填 | 说明         |
|----------|--------|:--:|------------|
| account  | string | 是  | 手机号/邮箱/用户名 |
| password | string | 是  | 密码         |

```json
{
  "account": "admin",
  "password": "admin123"
}
```

响应 `BaseResponse<LoginVO>`（与用户登录结构相同，但 token 由 admin 密钥签发）。仅 `userType=2` 的用户可以登录。

---

## 十一、管理员后台 — 用户管理

> 所有接口需 Admin Token

### 10.1 用户列表

```
GET /admin/user/list?page=1&size=10&username=zhang&status=1&startDate=2026-01-01&endDate=2026-12-31
```

| 参数        | 类型     | 必填 | 说明                 |
|-----------|--------|:--:|--------------------|
| page      | int    | 否  | 页码，默认 1            |
| size      | int    | 否  | 每页条数，默认 10         |
| username  | string | 否  | 用户名模糊搜索            |
| phone     | string | 否  | 手机号模糊搜索            |
| email     | string | 否  | 邮箱模糊搜索             |
| status    | int    | 否  | 状态 0-禁用 1-正常       |
| startDate | string | 否  | 注册时间起 `yyyy-MM-dd` |
| endDate   | string | 否  | 注册时间止 `yyyy-MM-dd` |

响应 `BaseResponse<Page<AdminUserVO>>`：

```json
{
  "code": 0,
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "records": [
      {
        "id": "1",
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": "...",
        "phone": "138***",
        "email": "***@qq.com",
        "sex": 1,
        "birthday": "2000-01-01",
        "userType": 1,
        "status": 1,
        "createTime": "2026-05-16T10:00:00",
        "updateTime": "2026-05-20T15:30:00"
      }
    ]
  }
}
```

### 10.2 用户详情

```
GET /admin/user/{id}
```

### 10.3 封禁/启用

```
PUT /admin/user/{id}/status?status=0
```

| 参数     | 类型  | 必填 | 说明        |
|--------|-----|:--:|-----------|
| status | int | 是  | 0-禁用，1-正常 |

### 10.4 修改用户信息

```
PUT /admin/user/{id}
```

| 参数       | 类型     | 必填 | 说明           |
|----------|--------|:--:|--------------|
| nickname | string | 否  | 昵称           |
| phone    | string | 否  | 手机号          |
| email    | string | 否  | 邮箱           |
| sex      | int    | 否  | 0-未知 1-男 2-女 |
| birthday | string | 否  | `yyyy-MM-dd` |

```json
{
  "nickname": "张三丰",
  "phone": "13900139000",
  "email": "newemail@example.com",
  "sex": 1,
  "birthday": "2000-06-15"
}
```

### 10.5 重置密码

```
PUT /admin/user/{id}/password?newPassword=abc123
```

| 参数          | 类型     | 必填 | 说明          |
|-------------|--------|:--:|-------------|
| newPassword | string | 是  | 新密码，6-100 位 |

### 10.6 设置角色

```
PUT /admin/user/{id}/role?userType=2
```

| 参数       | 类型  | 必填 | 说明           |
|----------|-----|:--:|--------------|
| userType | int | 是  | 1-普通用户，2-管理员 |

---

## 十二、管理员后台 — 额度管理

> 所有接口需 Admin Token

### 11.1 额度配置列表

```
GET /admin/quota/config/list
```

响应 `BaseResponse<List<QuotaConfig>>`

### 11.2 修改额度配置

```
PUT /admin/quota/config
```

```json
{
  "id": 1,
  "dailyLimit": 50
}
```

### 11.3 用户额度列表

```
GET /admin/quota/user/list?page=1&size=10&userId=1&memberLevel=1
```

| 参数          | 类型   | 必填 | 说明          |
|-------------|------|:--:|-------------|
| page        | int  | 否  | 页码，默认 1     |
| size        | int  | 否  | 每页条数，默认 10  |
| userId      | long | 否  | 按用户 ID 精确筛选 |
| memberLevel | int  | 否  | 按会员等级筛选     |

响应 `BaseResponse<Page<AdminUserQuotaVO>>`：

```json
{
  "code": 0,
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "records": [
      {
        "userId": "1",
        "username": "zhangsan",
        "nickname": "张三",
        "memberLevel": 1,
        "memberExpireAt": "2026-06-16T10:00:00",
        "dailyUsed": 5,
        "dailyLimit": 30,
        "dailyDate": "2026-05-27"
      }
    ]
  }
}
```

### 11.4 修改会员等级

```
PUT /admin/quota/user/member
```

| 参数             | 类型     | 必填 | 说明                           |
|----------------|--------|:--:|------------------------------|
| userId         | long   | 是  | 用户 ID                        |
| memberLevel    | int    | 是  | 会员等级 0-免费 1-月卡 2-季卡 3-年卡     |
| memberExpireAt | string | 是  | 会员到期时间 `yyyy-MM-ddTHH:mm:ss` |

```json
{
  "userId": 1,
  "memberLevel": 2,
  "memberExpireAt": "2027-05-27T10:00:00"
}
```

### 11.5 额度流水查询

```
GET /admin/quota/log/list?page=1&size=10&userId=1&changeType=1&startTime=2026-01-01T00:00:00&endTime=2026-12-31T23:59:59
```

| 参数         | 类型     | 必填 | 说明                         |
|------------|--------|:--:|----------------------------|
| userId     | long   | 否  | 用户 ID                      |
| changeType | int    | 否  | 1-扣减 2-退还                  |
| startTime  | string | 否  | 开始时间 `yyyy-MM-ddTHH:mm:ss` |
| endTime    | string | 否  | 结束时间                       |

---

## 十三、管理员后台 — API Key 管理

> 所有接口需 Admin Token

### 12.1 Key 列表

```
GET /admin/api-key/list?page=1&size=10
```

响应中 `apiKeyMasked` 为脱敏值（`sk-xx****xxxx`），不返回完整密钥。

### 12.2 Key 详情

```
GET /admin/api-key/{id}
```

返回完整 `apiKeyMasked`、`baseUrl`、`modelName`。

### 12.3 新增 Key

```
POST /admin/api-key
```

```json
{
  "providerName": "openai",
  "apiKey": "sk-proj-xxxxxxxxxxxx",
  "baseUrl": "https://api.openai.com/v1",
  "modelName": "gpt-4o",
  "description": "OpenAI GPT-4o 生产密钥"
}
```

`modelType`: 已移除。所有密钥均为聊天模型（嵌入向量模型在 Python 端硬编码）。

### 12.4 修改 Key

```
PUT /admin/api-key
```

```json
{
  "id": 1,
  "apiKey": "sk-new-key",
  "baseUrl": "https://new-api.example.com/v1"
}
```

### 12.5 删除 Key

```
DELETE /admin/api-key/{id}
```

### 12.6 启用/禁用

```
PUT /admin/api-key/{id}/enabled?enabled=false
```

---

## 十四、管理员后台 — 其他

> 所有接口需 Admin Token

### 13.1 数据看板

```
GET /admin/dashboard/stats
```

响应 `BaseResponse<DashboardVO>`：

```json
{
  "code": 0,
  "data": {
    "totalUsers": 1024,
    "todayNewUsers": 15,
    "totalProjects": 350,
    "totalFiles": 1200,
    "todayGenerationTasks": 48,
    "userCountByLevel": {
      "0": 800,
      "1": 150,
      "2": 50,
      "3": 24
    }
  }
}
```

### 13.2 操作日志

```
GET /admin/operation-log/list?page=1&size=10&adminId=1&action=UPDATE&targetType=user&startTime=2026-01-01T00:00:00
```

| 参数         | 类型     | 必填 | 说明                         |
|------------|--------|:--:|----------------------------|
| page       | int    | 否  | 页码，默认 1                    |
| size       | int    | 否  | 每页条数，默认 10                 |
| adminId    | long   | 否  | 操作管理员 ID                   |
| action     | string | 否  | 操作类型（见下表）                  |
| targetType | string | 否  | 目标类型（见下表）                  |
| startTime  | string | 否  | 开始时间 `yyyy-MM-ddTHH:mm:ss` |
| endTime    | string | 否  | 结束时间                       |

| action   | 说明 |
|----------|----|
| `LOGIN`  | 登录 |
| `CREATE` | 创建 |
| `UPDATE` | 更新 |
| `DELETE` | 删除 |
| `RETRY`  | 重试 |

| targetType     | 说明    |
|----------------|-------|
| `admin`        | 管理员登录 |
| `user`         | 用户    |
| `api_key`      | API密钥 |
| `announcement` | 公告    |
| `quota_config` | 额度配置  |
| `user_quota`   | 用户额度  |
| `file`         | 文件    |
| `project`      | 项目    |
| `task`         | 任务    |

响应 `BaseResponse<Page<OperationLogVO>>`：

```json
{
  "code": 0,
  "data": {
    "current": 1,
    "size": 10,
    "total": 50,
    "records": [
      {
        "id": "100",
        "adminId": "1",
        "adminName": "admin",
        "action": "UPDATE",
        "targetType": "user",
        "targetId": "5",
        "detail": "封禁用户",
        "createTime": "2026-05-27T15:30:00"
      }
    ]
  }
}
```

### 13.3 项目管理

#### 项目列表

```
GET /admin/project/list?page=1&size=10&name=课件&userId=1&status=1
```

| 参数     | 类型     | 必填 | 说明         |
|--------|--------|:--:|------------|
| page   | int    | 否  | 页码，默认 1    |
| size   | int    | 否  | 每页条数，默认 10 |
| name   | string | 否  | 项目名称模糊搜索   |
| userId | long   | 否  | 按创建者筛选     |
| status | int    | 否  | 项目状态筛选     |

响应 `BaseResponse<Page<Project>>`

#### 项目详情

```
GET /admin/project/{id}
```

响应 `BaseResponse<Project>`

#### 删除项目

```
DELETE /admin/project/{id}
```

> 级联将项目下文件移入回收站

### 13.4 文件管理

#### 文件列表

```
GET /admin/file/list?page=1&size=10&userId=1&projectId=5&fileName=报告&fileType=1
```

| 参数        | 类型     | 必填 | 说明                        |
|-----------|--------|:--:|---------------------------|
| page      | int    | 否  | 页码，默认 1                   |
| size      | int    | 否  | 每页条数，默认 10                |
| userId    | long   | 否  | 按用户 ID 筛选                 |
| projectId | long   | 否  | 按项目 ID 筛选                 |
| fileName  | string | 否  | 文件名模糊搜索                   |
| fileType  | int    | 否  | 文件类型 1-PPT 2-Word 3-PDF 等 |

响应 `BaseResponse<Page<FileRecord>>`

#### 删除文件

```
DELETE /admin/file/{id}
```

> 同时删除 OSS 中的物理文件

### 13.5 任务管理

#### 13.5.1 任务列表

```
GET /admin/task/list
```

遍历 Redis 中所有 `task:*:status` 键，按状态返回对应的字段，结果按任务 ID 降序排列。

响应 `BaseResponse<List<AdminTaskVO>>`：

```json
{
  "code": 0,
  "data": [
    {
      "taskId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "status": "completed",
      "fileId": "128",
      "fileUrl": "https://storage.example.com/files/128.pptx",
      "fileName": "Java基础语法教学.pptx",
      "qaLowestScore": 72,
      "qaTotalCount": 15
    },
    {
      "taskId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "status": "failed",
      "errorCode": 5002,
      "errorMessage": "大纲验证失败：缺少主标题字段"
    },
    {
      "taskId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "status": "processing",
      "stage": "running_qa",
      "progress": 65,
      "message": "正在质量评审：第 10/15 页"
    },
    {
      "taskId": "d4e5f6a7-b8c9-0123-defa-234567890123",
      "status": "pending"
    }
  ]
}
```

> 字段按 status 不同返回不同内容，使用 `@JsonInclude(NON_NULL)` 省略 null 字段。
>
> - `pending` — 仅 taskId、status
> - `processing` — 额外返回 stage、progress、message
> - `completed` — 额外返回 fileId、fileUrl、fileName、qaLowestScore、qaTotalCount
> - `failed` — 额外返回 errorCode、errorMessage

#### 13.5.2 任务详情

```
GET /admin/task/{taskId}
```

返回指定任务的完整信息，格式同列表中的单条记录。若任务不存在或已过期返回 404。

响应示例：同 13.5.1 中对应状态的单条记录。

---

## 十五、管理员后台 — 公告管理

> 需 Admin Token

### 14.1 公告列表

```
GET /admin/announcement/list?page=1&size=10&status=1
```

| 参数     | 类型  | 必填 | 说明                  |
|--------|-----|:--:|---------------------|
| page   | int | 否  | 页码，默认 1             |
| size   | int | 否  | 每页条数，默认 10          |
| status | int | 否  | 发布状态 0-草稿 1-已发布（可选） |

响应 `BaseResponse<Page<AnnouncementVO>>`：

```json
{
  "code": 0,
  "data": {
    "current": 1,
    "size": 10,
    "total": 5,
    "records": [
      {
        "id": "1",
        "title": "系统维护通知",
        "content": "本系统将于 2026-06-01 00:00-02:00 进行维护...",
        "status": 1,
        "publishTime": "2026-05-27T10:00:00",
        "createTime": "2026-05-27T09:30:00",
        "updateTime": "2026-05-27T10:00:00"
      }
    ]
  }
}
```

### 14.2 公告详情

```
GET /admin/announcement/{id}
```

响应 `BaseResponse<AnnouncementVO>`，格式同列表中的单条记录。

### 14.3 新增公告

```
POST /admin/announcement
```

| 参数      | 类型     | 必填 | 说明            |
|---------|--------|:--:|---------------|
| title   | string | 是  | 公告标题          |
| content | string | 是  | 公告正文内容        |
| status  | int    | 否  | 发布状态，默认 0（草稿） |

```json
{
  "title": "系统维护通知",
  "content": "本系统将于 2026-06-01 00:00-02:00 进行维护...",
  "status": 1
}
```

> **发布时间说明**：创建公告时 `publishTime` 默认为 null，前端无需传入。当公告状态首次从草稿变为"已发布"时，后端自动将
`publishTime` 设为当前时间。已发布公告再次编辑不会重置发布时间。

### 14.4 修改公告

```
PUT /admin/announcement
```

| 参数      | 类型     | 必填 | 说明              |
|---------|--------|:--:|-----------------|
| id      | long   | 是  | 公告 ID           |
| title   | string | 否  | 公告标题            |
| content | string | 否  | 公告正文内容          |
| status  | int    | 否  | 发布状态 0-草稿 1-已发布 |

```json
{
  "id": 1,
  "title": "系统维护通知（更新）",
  "status": 1
}
```

> 当 status 从 0（草稿）变为 1（已发布）时，自动设置 `publishTime = 当前时间`。仅更新标题/内容而不改 status 时，发布时间不变。

### 14.5 删除公告

```
DELETE /admin/announcement/{id}
```

---

## 十六、用户端公告查询

> 无需登录

### 15.1 查询已发布公告

```
GET /announcement/active
```

返回 `BaseResponse<List<AnnouncementVO>>`，按发布时间倒序排列已发布的公告。

---

## 十七、通用错误码

|  code   | 说明             |
|:-------:|----------------|
|   `0`   | 成功             |
| `40000` | 参数校验失败         |
| `40001` | 用户不存在          |
| `40002` | 用户名已存在         |
| `40003` | 手机号已注册         |
| `40004` | 邮箱已注册          |
| `40005` | 密码错误           |
| `40006` | 验证码错误或已过期      |
| `40007` | 账号已被禁用         |
| `40008` | 原密码错误          |
| `40009` | 今日额度已用完        |
| `40010` | 项目不存在          |
| `40011` | 项目名称已存在        |
| `40012` | API 密钥不存在      |
| `40013` | 公告不存在          |
| `40014` | 用户额度记录不存在      |
| `40015` | 额度配置不存在        |
| `40100` | 未登录或 Token 已过期 |
| `40101` | 无权限访问          |
| `40102` | 需要管理员权限        |
| `40300` | 无权操作           |
| `40400` | 资源不存在          |
| `50000` | 系统内部错误         |

---

## 十八、分页响应格式

所有列表接口返回统一分页结构：

```json
{
  "code": 0,
  "data": {
    "current": 1,
    "size": 10,
    "total": 25,
    "pages": 3,
    "records": [
      ...
    ]
  }
}
```

| 字段        | 说明      |
|-----------|---------|
| `current` | 当前页码    |
| `size`    | 每页条数    |
| `total`   | 总记录数    |
| `pages`   | 总页数     |
| `records` | 当前页数据列表 |
