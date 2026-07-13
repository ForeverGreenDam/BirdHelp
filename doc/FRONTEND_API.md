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

| 模块                                            | 是否需要 Token  |
|-----------------------------------------------|:-----------:|
| 注册、登录、发送验证码、重置密码                              |      否      |
| 用户信息、项目管理、文件管理、PPT/Word/PDF 生成、对话修改、额度查询、会员模块 |      是      |
| 管理员后台所有接口                                     | Admin Token |
| 用户端公告查询                                       |      否      |
| 支付宝回调通知                                       |      否      |

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
    "fileUrl": "https://oss.example.com/...",
    "outline": null,
    "versionOf": null,
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

| source | 含义          |
|:------:|-------------|
|   1    | 用户上传（知识库素材） |
|   2    | AI 生成       |

### 4.2 文件列表（含搜索）

```
GET /file/list?projectId=1&page=1&size=10&fileType=3&source=1&keyword=课件
```

| 参数          | 类型     | 必填 | 说明                                    |
|-------------|--------|:--:|---------------------------------------|
| `projectId` | long   | 是  | 项目 ID                                 |
| `page`      | int    | 否  | 页码，默认 1                               |
| `size`      | int    | 否  | 每页条数，默认 10                            |
| `fileType`  | int    | 否  | 筛选文件类型：1-PPT 2-Word 3-PDF 4-图片 5-其他   |
| `source`    | int    | 否  | 筛选文件来源：**1-用户上传（知识库）** 2-AI生成。不传则显示全部 |
| `keyword`   | string | 否  | 搜索关键词，按文件名模糊匹配。不传或为空则不做文件名过滤          |

> **注意**：`search` 端点已与 `list` 合并，直接使用 `list` 传 `keyword` 参数即可。

**列表自动隐藏旧版本：** AI 修改文档后会生成新文件，旧版本通过 `versionOf`
字段形成版本链。列表只展示链尾文件（最新版），旧版本自动隐藏。用户可在对话窗口的历史消息中回顾旧版本。

**知识库使用场景：**

| 页面    | 调用方式                                             | 说明           |
|-------|--------------------------------------------------|--------------|
| 全部文件  | `GET /file/list?projectId=1`                     | 显示该项目下所有链尾文件 |
| 知识库   | `GET /file/list?projectId=1&source=1`            | 只显示用户上传的原始素材 |
| AI 生成 | `GET /file/list?projectId=1&source=2`            | 只显示 AI 生成的文件 |
| 搜索知识库 | `GET /file/list?projectId=1&source=1&keyword=报告` | 在素材中搜索       |
| 按类型筛选 | `GET /file/list?projectId=1&fileType=1&source=1` | 只看 PPT 素材    |

响应 `BaseResponse<Page<FileRecordVO>>`（分页格式见 §十八）。

### 4.3 文件预览

```
GET /file/{id}/preview
```

获取文件的预览图片。采用三级缓存策略：Redis（1h）→ MySQL → 重新渲染。

渲染管道：LibreOffice 无头模式转 PDF → PDFBox 逐页渲染 150 DPI PNG → 上传 OSS。

每页会标注 `layoutType`（布局类型）和 `title`（页面标题），来自文档生成时记录的大纲 JSON。

**使用场景：** 文件列表中点击文件卡片时调用，展示文件缩略图预览；对话修改页面中展示当前编辑文档的预览。

响应 `BaseResponse<PreviewVO>`：

```json
{
  "code": 0,
  "data": {
    "fileId": "42",
    "fileHash": "abc123def456",
    "totalPages": 12,
    "pages": [
      {
        "pageNumber": 1,
        "imageUrl": "https://oss.example.com/preview/1/42/page_001.png",
        "layoutType": "cover",
        "title": "Java基础语法教学"
      },
      {
        "pageNumber": 2,
        "imageUrl": "https://oss.example.com/preview/1/42/page_002.png",
        "layoutType": "text_only",
        "title": "课程目标"
      }
    ]
  }
}
```

| 字段           | 类型     | 说明                          |
|--------------|--------|-----------------------------|
| `fileId`     | string | 文件 ID                       |
| `fileHash`   | string | 文件哈希，用于缓存校验                 |
| `totalPages` | int    | 总页数                         |
| `pages`      | array  | 每页预览数据                      |
| `pageNumber` | int    | 页码（从 1 开始）                  |
| `imageUrl`   | string | 预览 PNG 图片 URL（OSS）          |
| `layoutType` | string | 页面布局类型（如 cover/text_only 等） |
| `title`      | string | 页面标题                        |

> **首次预览可能较慢**（需 LibreOffice 转换 + PDFBox 渲染），后续命中缓存后秒级返回。

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

## 十、会员模块 — `/member`

> 需 Token

### 10.1 查询套餐列表

```
GET /member/plans
```

返回所有上架的会员套餐，按价格升序排列。

响应 `BaseResponse<List<MemberPlanVO>>`：

```json
{
  "code": 0,
  "data": [
    {
      "id": "1",
      "name": "月卡",
      "level": 1,
      "price": 29.90,
      "actualPrice": 29.90,
      "durationDays": 30,
      "dailyLimit": 30
    },
    {
      "id": "2",
      "name": "季卡",
      "level": 2,
      "price": 79.90,
      "actualPrice": 79.90,
      "durationDays": 90,
      "dailyLimit": 60
    },
    {
      "id": "3",
      "name": "年卡",
      "level": 3,
      "price": 299.00,
      "actualPrice": 299.00,
      "durationDays": 365,
      "dailyLimit": 100
    }
  ]
}
```

| 字段             | 类型      | 说明                  |
|----------------|---------|---------------------|
| `id`           | long    | 套餐 ID               |
| `name`         | string  | 套餐名称（月卡/季卡/年卡）      |
| `level`        | int     | 会员等级：1-月卡 2-季卡 3-年卡 |
| `price`        | decimal | 原价（展示用），单位：元        |
| `actualPrice`  | decimal | 实际售价（支付用），单位：元      |
| `durationDays` | int     | 有效天数                |
| `dailyLimit`   | int     | 每日生成次数上限            |

### 10.2 创建订单并发起支付

```
POST /member/orders
```

| 参数       | 类型   | 必填 | 说明    |
|----------|------|:--:|-------|
| `planId` | long | 是  | 套餐 ID |

```json
{
  "planId": 1
}
```

**响应说明：**

此接口返回支付宝支付表单的 HTML 片段（`Content-Type: text/html;charset=UTF-8`）。前端需要将返回的 HTML
直接写入页面或通过隐藏表单自动提交到支付宝。

**前端处理方式：**

```javascript
// 方式1：直接写入页面（推荐）
const response = await fetch('/member/orders', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', 'token': jwtToken},
    body: JSON.stringify({planId: 1})
});
const html = await response.text();
document.write(html); // 或写入隐藏的 div

// 方式2：通过隐藏 iframe 提交
const iframe = document.createElement('iframe');
iframe.name = 'payFrame';
iframe.style.display = 'none';
document.body.appendChild(iframe);
document.write(html);
```

| 错误码     | 说明    |
|---------|-------|
| `40016` | 套餐不存在 |
| `40017` | 套餐已下架 |

### 10.3 查询订单详情

```
GET /member/orders/{id}
```

| 参数   | 类型   | 必填 | 说明    |
|------|------|:--:|-------|
| `id` | long | 是  | 订单 ID |

响应 `BaseResponse<MemberOrderVO>`：

```json
{
  "code": 0,
  "data": {
    "id": "1",
    "orderNo": "171950000012340001",
    "planId": "1",
    "planName": "月卡",
    "amount": 29.90,
    "payType": 2,
    "status": 1,
    "tradeNo": "2026052722001401230500000001",
    "paidAt": "2026-05-27T15:30:00",
    "expireAt": "2026-05-27T15:45:00",
    "createTime": "2026-05-27T15:30:00"
  }
}
```

| 字段           | 类型      | 说明                     |
|--------------|---------|------------------------|
| `id`         | long    | 订单 ID                  |
| `orderNo`    | string  | 订单号                    |
| `planId`     | long    | 套餐 ID                  |
| `planName`   | string  | 套餐名称                   |
| `amount`     | decimal | 支付金额，单位：元              |
| `payType`    | int     | 支付方式：2-支付宝             |
| `status`     | int     | 订单状态：0-待支付 1-已支付 2-已过期 |
| `tradeNo`    | string  | 支付宝交易号（支付成功后返回）        |
| `paidAt`     | string  | 支付成功时间                 |
| `expireAt`   | string  | 订单过期时间（创建后15分钟）        |
| `createTime` | string  | 订单创建时间                 |

### 10.4 查询我的订单列表

```
GET /member/orders?page=1&size=10
```

| 参数     | 类型  | 必填 | 说明         |
|--------|-----|:--:|------------|
| `page` | int | 否  | 页码，默认 1    |
| `size` | int | 否  | 每页条数，默认 10 |

响应 `BaseResponse<Page<MemberOrderVO>>`

### 10.5 查询会员状态

```
GET /member/status
```

响应 `BaseResponse<MemberStatusVO>`：

```json
{
  "code": 0,
  "data": {
    "memberLevel": 1,
    "memberLevelName": "月卡",
    "memberExpireAt": "2026-06-27T15:30:00",
    "isExpired": false,
    "dailyLimit": 30
  }
}
```

| 字段                | 类型      | 说明                       |
|-------------------|---------|--------------------------|
| `memberLevel`     | int     | 当前等级：0-免费 1-月卡 2-季卡 3-年卡 |
| `memberLevelName` | string  | 等级名称                     |
| `memberExpireAt`  | string  | 到期时间，免费用户为 null          |
| `isExpired`       | boolean | 是否已过期                    |
| `dailyLimit`      | int     | 每日额度上限                   |

### 10.6 支付宝回调通知（无需鉴权）

> 此接口由支付宝服务器调用，前端无需处理

```
POST /pay/alipay/notify
```

支付宝支付成功后会异步调用此接口通知支付结果。后端处理完成后返回 `"success"` 字符串。

### 10.7 支付宝同步跳转（无需鉴权）

> 此接口由支付宝跳转，前端无需处理

```
GET /pay/alipay/return
```

用户支付完成后，浏览器会跳转到此接口，后端会通过 `response.sendRedirect()` 重定向到前端支付结果页面。

**前端需要做的事：**

1. **设计支付结果页面**（如路由 `/pay/result`），用于展示支付状态
2. 页面需从 URL 参数中获取 `orderNo`，调用 `GET /member/orders/{id}` 查询订单详情并展示

**支付宝开放平台配置：**

在支付宝开放平台的应用配置中，设置同步回调地址为：

```
https://your-domain.com/api/pay/alipay/return
```

> 注意：回调地址必须是公网可访问的 HTTPS 地址，本地开发可使用 ngrok 等内网穿透工具。

### 10.8 前端支付流程

```
1. 用户点击「开通会员」→ GET /member/plans → 展示套餐列表
2. 用户选择套餐 → POST /member/orders({planId}) → 返回支付宝表单 HTML
3. 前端自动提交表单 → 跳转到支付宝收银台
4. 用户完成支付 → 支付宝异步通知 POST /pay/alipay/notify（后端处理）
5. 支付宝同步跳转 → GET /pay/alipay/return → 重定向到前端订单页
6. 前端轮询订单状态 → GET /member/orders/{id} → 确认支付成功
7. 更新会员状态展示 → GET /member/status
```

---

## 十一、管理员后台 — 认证

### 11.1 管理员登录

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

## 十二、管理员后台 — 用户管理

> 所有接口需 Admin Token

### 12.1 用户列表

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

### 12.2 用户详情

```
GET /admin/user/{id}
```

### 12.3 封禁/启用

```
PUT /admin/user/{id}/status?status=0
```

| 参数     | 类型  | 必填 | 说明        |
|--------|-----|:--:|-----------|
| status | int | 是  | 0-禁用，1-正常 |

### 12.4 修改用户信息

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

### 12.5 重置密码

```
PUT /admin/user/{id}/password?newPassword=abc123
```

| 参数          | 类型     | 必填 | 说明          |
|-------------|--------|:--:|-------------|
| newPassword | string | 是  | 新密码，6-100 位 |

### 12.6 设置角色

```
PUT /admin/user/{id}/role?userType=2
```

| 参数       | 类型  | 必填 | 说明           |
|----------|-----|:--:|--------------|
| userType | int | 是  | 1-普通用户，2-管理员 |

---

## 十三、管理员后台 — 额度管理

> 所有接口需 Admin Token

### 13.1 额度配置列表

```
GET /admin/quota/config/list
```

响应 `BaseResponse<List<QuotaConfig>>`

### 13.2 修改额度配置

```
PUT /admin/quota/config
```

```json
{
  "id": 1,
  "dailyLimit": 50
}
```

### 13.3 用户额度列表

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

### 13.4 修改会员等级（含手动授予）

```
PUT /admin/quota/user/member
```

| 参数             | 类型     | 必填 | 说明                           |
|----------------|--------|:--:|------------------------------|
| userId         | long   | 是  | 用户 ID                        |
| memberLevel    | int    | 是  | 会员等级 0-免费 1-月卡 2-季卡 3-年卡     |
| memberExpireAt | string | 否  | 会员到期时间 `yyyy-MM-ddTHH:mm:ss` |

```json
{
  "userId": 1,
  "memberLevel": 2,
  "memberExpireAt": "2027-05-27T10:00:00"
}
```

> **到期时间计算逻辑：**
> - 若 `memberLevel=0`（免费用户），自动清除到期时间
> - 若指定了 `memberExpireAt`，直接使用该时间
> - 若未指定 `memberExpireAt` 且为付费等级，根据对应套餐时长自动计算：
    >
- 当前会员未过期时，在现有到期时间上**追加**天数
>   - 已过期或无会员时，从当前时间开始计算

### 13.5 额度流水查询

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

## 十四、管理员后台 — API Key 管理

> 所有接口需 Admin Token

### 14.1 Key 列表

```
GET /admin/api-key/list?page=1&size=10
```

响应中 `apiKeyMasked` 为脱敏值（`sk-xx****xxxx`），不返回完整密钥。

### 14.2 Key 详情

```
GET /admin/api-key/{id}
```

返回完整 `apiKeyMasked`、`baseUrl`、`modelName`。

### 14.3 新增 Key

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

### 14.4 修改 Key

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

### 14.5 删除 Key

```
DELETE /admin/api-key/{id}
```

### 14.6 启用/禁用

```
PUT /admin/api-key/{id}/enabled?enabled=false
```

---

## 十五、管理员后台 — 其他

> 所有接口需 Admin Token

### 15.1 数据看板

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

### 15.2 操作日志

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
| `GRANT`  | 授予 |

| targetType     | 说明    |
|----------------|-------|
| `admin`        | 管理员登录 |
| `user`         | 用户    |
| `api_key`      | API密钥 |
| `announcement` | 公告    |
| `quota_config` | 额度配置  |
| `user_quota`   | 用户额度  |
| `member_plan`  | 会员套餐  |
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

### 15.3 项目管理

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

### 15.4 文件管理

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

### 15.5 任务管理

#### 15.5.1 任务列表

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

#### 15.5.2 任务详情

```
GET /admin/task/{taskId}
```

返回指定任务的完整信息，格式同列表中的单条记录。若任务不存在或已过期返回 404。

响应示例：同 13.5.1 中对应状态的单条记录。

---

## 十六、管理员后台 — 公告管理

> 需 Admin Token

### 16.1 公告列表

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

### 16.2 公告详情

```
GET /admin/announcement/{id}
```

响应 `BaseResponse<AnnouncementVO>`，格式同列表中的单条记录。

### 16.3 新增公告

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

### 16.4 修改公告

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

### 16.5 删除公告

```
DELETE /admin/announcement/{id}
```

---

## 十七、用户端公告查询

> 无需登录

### 17.1 查询已发布公告

```
GET /announcement/active
```

返回 `BaseResponse<List<AnnouncementVO>>`，按发布时间倒序排列已发布的公告。

---

## 十八、对话修改模块 — `/chat`

> 需 Token。左侧栏展示全局会话列表，点击标签进入具体会话后多轮对话修改。

```
┌─ 左侧栏 ───────────┬── 右侧主区域 ──────────────┐
│ + 新建对话          │                            │
│                    │   选中某个会话后：            │
│ ▸ Java课件制作      │   - 聊天消息列表             │
│   大二课件 · 12条    │   - 版本时间线              │
│                    │   - 当前文件预览             │
│ ▸ 英语论文修改       │                            │
│   大三论文 · 5条     │   未选中时：                  │
│                    │   - 文件选择器（新建对话）     │
│ ▸ 语文PPT优化       │                            │
│   语文课 · 3条      │                            │
└────────────────────┴────────────────────────────┘
```

### 18.1 创建新会话

```
POST /chat/session
```

**使用场景：** 用户点击「新建对话」→ 选择文件 → 调用此接口创建空会话，拿到 `sessionId` 后用于后续对话。Java 端生成
UUID，前端无需自行生成。

| 参数          | 类型     | 必填 | 说明                     |
|-------------|--------|:--:|------------------------|
| `fileId`    | string | 是  | 选中的源文件 ID              |
| `docType`   | string | 是  | `ppt` / `word` / `pdf` |
| `projectId` | long   | 是  | 所属项目 ID                |
| `title`     | string | 否  | 自定义标题，不传则取原始文件名去扩展名    |

```json
{ "fileId": "100", "docType": "ppt", "projectId": 1 }
```

响应：

```json
{ "code": 0, "data": { "sessionId": "uuid-v4", "title": "课件素材" } }
```

### 18.2 会话列表（左侧栏全局）

```
GET /chat/sessions
```

**使用场景：** 页面加载时调用，渲染左侧栏。返回该用户所有会话（不限项目），按最后更新时间倒序。无需传任何参数（用户 ID 从 JWT
获取）。

响应 `BaseResponse<List<ChatSessionVO>>`：

```json
{
  "code": 0,
  "data": [
    {
      "sessionId": "a1b2c3d4-...",
      "title": "Java课件制作",
      "projectId": "1",
      "originalFileId": "100",
      "currentFileId": "105",
      "originalFileName": "课件素材.pptx",
      "docType": "ppt",
      "messageCount": 12,
      "lastMessagePreview": "已修改第二页标题，从「课程目标」改为「...",
      "createTime": "2026-05-30T10:00:00",
      "updateTime": "2026-05-31T14:30:00"
    }
  ]
}
```

### 18.3 会话详情（点击标签加载历史）

```
GET /chat/session/{sessionId}
```

**使用场景：** 用户点击左侧标签 → 恢复完整聊天界面，包括历史消息和当前文件信息。

响应 `BaseResponse<ChatSessionDetailVO>`：

```json
{
  "code": 0,
  "data": {
    "sessionId": "a1b2c3d4-...",
    "title": "Java课件制作",
    "originalFileId": "100",
    "currentFileId": "105",
    "originalFileName": "课件素材.pptx",
    "docType": "ppt",
    "createTime": "2026-05-30T10:00:00",
    "updateTime": "2026-05-31T14:30:00",
    "messages": [
      {"id": "1", "role": "user", "content": "把第二页标题改激进些", "fileId": null, "createTime": "..."},
      {"id": "2", "role": "assistant", "content": "已修改第二页...", "fileId": "101", "createTime": "..."},
      {"id": "3", "role": "user", "content": "第三第四页对调", "fileId": null, "createTime": "..."},
      {"id": "4", "role": "assistant", "content": "已对调...", "fileId": "102", "createTime": "..."}
    ]
  }
}
```

**版本时间线：** 前端遍历 messages，收集 `role=assistant` 且 `fileId` 非空的条目即可。

### 18.4 前端完整流程

```
1. 页面加载 → GET /chat/sessions → 渲染左侧栏
2. 点击「新建对话」→ 弹出文件选择器（调 /file/list）→ 选文件
3. POST /chat/session({fileId, docType, projectId}) → 拿到 sessionId
4. 左侧栏即时更新（前端本地插入新标签，或重新拉 /chat/sessions）
5. 用户输入消息 → POST /chat/modify({sessionId, fileId, docType, message, projectId, history:[]})
6. 点击左侧已有标签 → GET /chat/session/{sessionId} → 恢复全部历史
7. 继续对话 → POST /chat/modify（复用同一个 sessionId，history 从本地 state 取）
8. 删除会话 → DELETE /chat/session/{sessionId} → 左侧栏移除该标签
```

### 18.5 删除会话

```
DELETE /chat/session/{sessionId}
```

**使用场景：** 用户在左侧栏右键（或长按）会话标签 → 删除。软删除，仅隐藏会话及其历史，不删除关联的任何文件。

无请求体，无响应 data。删除后左侧栏重新拉取即可。

### 18.6 对话修改文档

```
POST /chat/modify
```

**使用场景：** 用户在聊天窗口发送修改指令。每轮对话可修改文档（改文字、增删页、调顺序、换布局），修改后自动生成新版本文件。
`sessionId` 须为 `POST /chat/session` 返回的值（Java 生成），不能前端自行生成。

| 参数               | 类型     | 必填 | 默认值    | 说明                                                         |
|------------------|--------|:--:|--------|------------------------------------------------------------|
| `sessionId`      | string | 是  | —      | 会话 ID（UUID v4），同一修改对话的唯一标识                                 |
| `fileId`         | string | 是  | —      | 当前编辑的源文件 ID                                                |
| `docType`        | string | 是  | —      | 文档类型：`"ppt"` / `"word"` / `"pdf"`                          |
| `message`        | string | 是  | —      | 用户当前的修改指令                                                  |
| `projectId`      | long   | 是  | —      | 所属项目 ID                                                    |
| `history`        | array  | 否  | `[]`   | 历史消息列表，前端维护。每项：`{"role":"user/assistant","content":"..."}` |
| `regenerateFile` | bool   | 否  | `true` | 是否重建文件。`false` 时只返回 AI 文本回复，不生成新文件                         |

```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "fileId": "100",
  "docType": "ppt",
  "message": "把第二页标题改得激进一些，并把第三页和第四页顺序对调",
  "projectId": 1,
  "history": [
    {
      "role": "user",
      "content": "帮我看看这篇PPT"
    },
    {
      "role": "assistant",
      "content": "这是一篇关于Java基础语法的PPT，共10页..."
    }
  ],
  "regenerateFile": true
}
```

响应 `BaseResponse<Map>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "reply": "已根据您的指令修改文档大纲（3 处变更）。",
    "outline": {
      "title": "Java基础语法教学",
      "doc_type": "ppt",
      "slides": [
        {
          "page_number": 1,
          "title": "封面",
          "layout_type": "cover",
          ...
        },
        {
          "page_number": 2,
          "title": "激进版课程目标",
          "layout_type": "text_only",
          ...
        }
      ]
    },
    "changes": [
      {
        "page_number": 2,
        "action": "modified",
        "summary": "标题: \"课程目标\" → \"激进版课程目标\""
      },
      {
        "page_number": 3,
        "action": "modified",
        "summary": "与第4页对调顺序"
      },
      {
        "page_number": 4,
        "action": "modified",
        "summary": "与第3页对调顺序"
      }
    ],
    "fileId": "101",
    "fileUrl": "https://oss.example.com/1/ppt/2026-05/uuid.pptx",
    "title": "Java课件标题优化",
    "success": true
  }
}
```

> **`title` 字段**：仅在首轮对话（`history` 为空）时返回，由 LLM 根据大纲和用户首条消息自动生成。Java 端自动回填到
`chat_session.title`，前端可用于即时更新左侧栏标签。

| 字段        | 类型     | 说明                                                                             |
|-----------|--------|--------------------------------------------------------------------------------|
| `reply`   | string | AI 文本回复，可直接展示在聊天界面                                                             |
| `outline` | object | 修改后的完整文档大纲 JSON。可用于前端展示大纲树或高亮变更                                                |
| `changes` | array  | 变更摘要列表，每项含 `page_number`（页码）、`action`（modified/added/deleted）、`summary`（一句话描述） |
| `fileId`  | string | 新生成的文件 ID。`regenerateFile=false` 时为 `null`                                     |
| `fileUrl` | string | 新文件的 OSS URL                                                                   |
| `title`   | string | LLM 生成的会话标题（首轮对话返回，后续为空），前端用于更新左侧栏标签                                           |
| `success` | bool   | 是否成功                                                                           |

**单次 modify 调用后的前端处理：**

```
1. 发送 POST /chat/modify（sessionId 来自 POST /chat/session 的返回值）
2. 收到响应：
   a. 在聊天区展示 reply 文本
   b. 如果 fileId 不为空 → 更新预览区为新文件
   c. 在变更面板展示 changes 列表
   d. 如果 title 非空 → 更新左侧栏标签标题
   e. 将 user 消息和 assistant 回复追加到本地 history
3. 用户继续输入 → 发送下一条 modify（复用 sessionId，携带累积 history）
```

**修改能力覆盖：**

| 修改类型      | PPT | Word | PDF |
|-----------|:---:|:----:|:---:|
| 修改标题/正文   |  ✅  |  ✅   |  ❌  |
| 增删页面/章节   |  ✅  |  ✅   |  ❌  |
| 调整顺序      |  ✅  |  ✅   |  ❌  |
| 切换布局类型    |  ✅  |  —   |  —  |
| 修改图表/表格数据 |  ✅  |  ✅   |  ❌  |
| 改变整体风格/配色 |  ✅  |  ✅   |  ❌  |
| 仅讨论/给建议   |  ✅  |  ✅   |  ✅  |

### 18.7 仅讨论/问答

```
POST /chat/discuss
```

**使用场景：** 用户想先听听 AI 的建议，不着急生成新文件。例如"这篇文档结构有什么问题？"、"第一页还能怎么优化？"。

参数与 `/chat/modify` 相同，但 **不需要 `regenerateFile` 字段**（内部固定为 `false`）。

```json
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "fileId": "100",
  "docType": "ppt",
  "message": "这篇文档的第一页标题有什么问题？给些优化建议",
  "projectId": 1,
  "history": []
}
```

响应结构与 `/chat/modify` 相同，但 `fileId` 和 `fileUrl` 固定为 `null`，`changes` 为空数组。

### 18.8 版本链与文件回顾

每次 `/chat/modify` 成功后返回的 `fileId` 是新版本文件。旧版本文件自动在文件列表中隐藏（通过 `versionOf`链表机制），但可通过会话历史消息中的
`fileId` 回顾。

```
文件 A (versionOf=null)  ← 原始生成
    ↓ B.versionOf = A.id
文件 B                   ← 第1轮修改
    ↓ C.versionOf = B.id  
文件 C                   ← 第2轮修改（链尾，列表可见）
```

> 前端可在聊天区的每条 assistant 消息旁展示对应版本的文件预览链接。

---

## 十九、管理员后台 — 会员管理

> 所有接口需 Admin Token

### 19.1 套餐列表

```
GET /admin/member/plan/list
```

返回所有套餐（含下架），按价格升序排列。

响应 `BaseResponse<List<MemberPlan>>`：

```json
{
  "code": 0,
  "data": [
    {
      "id": "1",
      "name": "月卡",
      "level": 1,
      "price": 29.90,
      "actualPrice": 29.90,
      "durationDays": 30,
      "dailyLimit": 30,
      "status": 1
    }
  ]
}
```

### 19.2 套餐详情

```
GET /admin/member/plan/{id}
```

响应 `BaseResponse<MemberPlan>`

### 19.3 新增套餐

```
POST /admin/member/plan
```

| 参数             | 类型      | 必填 | 说明                  |
|----------------|---------|:--:|---------------------|
| `name`         | string  | 是  | 套餐名称                |
| `level`        | int     | 是  | 会员等级：1-月卡 2-季卡 3-年卡 |
| `price`        | decimal | 是  | 原价（展示用），单位：元        |
| `actualPrice`  | decimal | 是  | 实际售价（支付用），单位：元      |
| `durationDays` | int     | 是  | 有效天数                |
| `dailyLimit`   | int     | 是  | 每日生成次数上限            |

```json
{
  "name": "半年卡",
  "level": 4,
  "price": 179.90,
  "actualPrice": 159.90,
  "durationDays": 180,
  "dailyLimit": 80
}
```

### 19.4 修改套餐

```
PUT /admin/member/plan
```

| 参数             | 类型      | 必填 | 说明       |
|----------------|---------|:--:|----------|
| `id`           | long    | 是  | 套餐 ID    |
| `name`         | string  | 否  | 套餐名称     |
| `price`        | decimal | 否  | 原价       |
| `actualPrice`  | decimal | 否  | 实际售价     |
| `durationDays` | int     | 否  | 有效天数     |
| `dailyLimit`   | int     | 否  | 每日生成次数上限 |

### 19.5 上架/下架套餐

```
PUT /admin/member/plan/{id}/status?status=1
```

| 参数     | 类型  | 必填 | 说明        |
|--------|-----|:--:|-----------|
| status | int | 是  | 0-下架 1-上架 |

### 19.6 订单列表

```
GET /admin/member/order/list?page=1&size=10&userId=1&status=1
```

| 参数     | 类型   | 必填 | 说明                     |
|--------|------|:--:|------------------------|
| page   | int  | 否  | 页码，默认 1                |
| size   | int  | 否  | 每页条数，默认 10             |
| userId | long | 否  | 按用户 ID 精确筛选            |
| status | int  | 否  | 订单状态：0-待支付 1-已支付 2-已过期 |

响应 `BaseResponse<Page<AdminMemberOrderVO>>`：

```json
{
  "code": 0,
  "data": {
    "current": 1,
    "size": 10,
    "total": 50,
    "records": [
      {
        "id": "1",
        "orderNo": "171950000012340001",
        "userId": "100",
        "username": "zhangsan",
        "nickname": "张三",
        "planId": "1",
        "planName": "月卡",
        "amount": 29.90,
        "payType": 2,
        "status": 1,
        "tradeNo": "2026052722001401230500000001",
        "paidAt": "2026-05-27T15:30:00",
        "expireAt": "2026-05-27T15:45:00",
        "createTime": "2026-05-27T15:30:00"
      }
    ]
  }
}
```

### 19.7 订单详情

```
GET /admin/member/order/{id}
```

响应 `BaseResponse<AdminMemberOrderVO>`

---

## 二十、通用错误码

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
| `40016` | 套餐不存在          |
| `40017` | 套餐已下架          |
| `40018` | 订单不存在          |
| `40019` | 无权操作该订单        |
| `40020` | 订单已过期          |
| `40021` | 订单已支付          |
| `40022` | 支付验签失败         |
| `40023` | 支付回调处理异常       |
| `40100` | 未登录或 Token 已过期 |
| `40101` | 无权限访问          |
| `40102` | 需要管理员权限        |
| `40300` | 无权操作           |
| `40400` | 资源不存在          |
| `50000` | 系统内部错误         |

---

## 二十一、分页响应格式

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
