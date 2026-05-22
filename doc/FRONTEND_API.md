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

| 模块                                  | 是否需要 Token |
|-------------------------------------|:----------:|
| 注册、登录、发送验证码、重置密码                    |     否      |
| 用户信息、项目管理、文件管理、PPT/Word/PDF 生成、额度查询 |     是      |

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

## 五、PPT 生成模块 — `/ppt`

> 需 Token。同步接口，请求会阻塞 30–90 秒，前端需设置充足的超时时间（建议 ≥ 120 秒）。

### 5.1 生成 PPT

```
POST /ppt/generate
```

| 参数             | 类型     | 必填 | 默认值        | 说明                                                                                   |
|----------------|--------|:--:|------------|--------------------------------------------------------------------------------------|
| `projectId`    | string | 是  | —          | 项目 ID                                                                                |
| `topic`        | string | 是  | —          | PPT 主题，最长 200 字符                                                                     |
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

响应 `BaseResponse<PptGenerateResultVO>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "fileId": "128",
    "fileUrl": "https://storage.example.com/files/128.pptx",
    "fileName": "Java基础语法教学.pptx"
  }
}
```

可能的错误码：

| 场景       | message                     |
|----------|-----------------------------|
| 额度不足     | `PPT 生成失败: 额度不足，无法开始生成任务`   |
| 大纲生成失败   | `PPT 生成失败: 大纲生成失败，已达最大重试次数` |
| AI 模块未配置 | `AI 模块未配置，无法生成 PPT`         |
| 网络异常     | `PPT 生成请求失败，请稍后重试`          |

---

## 六、Word 生成模块 — `/word`

> 需 Token。同步接口，请求会阻塞 30–90 秒，前端需设置充足的超时时间（建议 ≥ 120 秒）。

### 6.1 生成 Word

```
POST /word/generate
```

| 参数             | 类型     | 必填 | 默认值        | 说明                                                                                   |
|----------------|--------|:--:|------------|--------------------------------------------------------------------------------------|
| `projectId`    | string | 是  | —          | 项目 ID                                                                                |
| `topic`        | string | 是  | —          | 文档主题，最长 200 字符                                                                       |
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

响应 `BaseResponse<WordGenerateResultVO>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "fileId": "129",
    "fileUrl": "https://storage.example.com/files/129.docx",
    "fileName": "人工智能发展报告.docx"
  }
}
```

可能的错误码：

| 场景       | message                      |
|----------|------------------------------|
| 额度不足     | `Word 生成失败: 额度不足，无法开始生成任务`   |
| 内容生成失败   | `Word 生成失败: 大纲生成失败，已达最大重试次数` |
| AI 模块未配置 | `AI 模块未配置，无法生成 Word`         |
| 网络异常     | `Word 生成请求失败，请稍后重试`          |

---

## 七、PDF 生成模块 — `/pdf`

> 需 Token。同步接口，请求会阻塞 30–120 秒（含 LibreOffice 转换），前端需设置充足的超时时间（建议 ≥ 150 秒）。

### 7.1 生成 PDF

```
POST /pdf/generate
```

| 参数             | 类型     | 必填 | 默认值        | 说明                                                                                   |
|----------------|--------|:--:|------------|--------------------------------------------------------------------------------------|
| `projectId`    | string | 是  | —          | 项目 ID                                                                                |
| `topic`        | string | 是  | —          | 文档主题，最长 200 字符                                                                       |
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

响应 `BaseResponse<PdfGenerateResultVO>`：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "fileId": "130",
    "fileUrl": "https://storage.example.com/files/130.pdf",
    "fileName": "年度工作总结.pdf"
  }
}
```

可能的错误码：

| 场景       | message                     |
|----------|-----------------------------|
| 额度不足     | `PDF 生成失败: 额度不足，无法开始生成任务`   |
| 内容生成失败   | `PDF 生成失败: 大纲生成失败，已达最大重试次数` |
| AI 模块未配置 | `AI 模块未配置，无法生成 PDF`         |
| 网络异常     | `PDF 生成请求失败，请稍后重试`          |

---

## 八、额度模块 — `/quota`

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

## 九、通用错误码

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
| `40100` | 未登录或 Token 已过期 |
| `40300` | 无权操作           |
| `40400` | 资源不存在          |
| `50000` | 系统内部错误         |

---

## 十、分页响应格式

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
