# BirdHelp — AI 驱动的文档生成平台

BirdHelp 是一个基于 AI 的文档自动生成平台，支持上传参考素材（PDF、Word、PPT、文本），通过大语言模型智能生成高质量的 PPT、Word 和
PDF 文档。

## 特性

- **多格式文档生成** — 一键生成 PPT、Word、PDF 三种格式的文档
- **RAG 知识增强** — 基于用户上传的参考素材进行检索增强生成，提升内容质量
- **项目管理** — 以项目为单位管理文件与生成任务，支持归档
- **配额系统** — 灵活的日配额机制，支持不同会员等级
- **多存储后端** — 支持本地存储与阿里云 OSS，按环境切换
- **双向 RSA 签名** — Java 后端与 AI 模块之间采用 RSA-SHA256 签名保障通信安全
- **文件回收站** — 软删除机制 + 定时自动清理

## 技术栈

| 层级     | 技术                   |
|--------|----------------------|
| 后端框架   | Spring Boot 2.6      |
| ORM    | MyBatis-Plus 3.5     |
| 数据库    | MySQL 8.0            |
| 缓存     | Redis                |
| 认证     | JWT (HS256) + BCrypt |
| 内部通信签名 | RSA-SHA256           |
| AI 服务  | Python FastAPI（独立模块） |
| API 文档 | Knife4j (Swagger)    |
| 构建工具   | Maven 3.8+           |
| 容器化    | Docker               |

## 架构概览

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   前端应用     │────▶│  Java 后端    │────▶│  AI 模块      │
│  (Web/App)   │     │  Spring Boot │     │  Python       │
│              │◀────│  :7890       │◀────│  FastAPI      │
└──────────────┘     └──────┬───────┘     └──────────────┘
                            │
                     ┌──────┴───────┐
                     │    MySQL      │
                     │    Redis      │
                     │  文件存储     │
                     │ (本地/OSS)   │
                     └──────────────┘
```

- **Java 后端**负责用户、项目、文件、配额等业务逻辑，并将生成请求代理转发至 AI 模块
- **AI 模块**（Python FastAPI）负责素材向量化、LLM 调用和文档渲染
- 两个服务之间通过双向 RSA 签名实现互信通信

## 快速开始

### 环境要求

- JDK 19+
- Maven 3.8+
- MySQL 8.0
- Redis
- AI 模块服务（Python FastAPI，需单独部署）

### 1. 创建数据库

在 `src/main/resources/sql/` 目录下依次执行 SQL 脚本初始化表结构：

```
user.sql → project.sql → file_record.sql → quota_config.sql → quota_log.sql → quota_user_quota.sql
```

### 2. 配置文件

编辑 `src/main/resources/application-dev.yml`，填入你的数据库、Redis 和 OSS 配置。

### 3. 启动

```bash
# 开发环境
mvn spring-boot:run

# 或打包运行
mvn clean package -DskipTests
java -jar target/birdhelp.jar
```

服务启动后访问 `http://localhost:7890/api/`。

### 4. API 文档

启动后访问 Knife4j 文档页面：

```
http://localhost:7890/api/doc.html
```

账号：`admin`，密码：`123456`

### 5. Docker 部署

```bash
mvn clean package -DskipTests
docker build -t birdhelp .
docker run -d -p 7890:7890 birdhelp
```

## 项目结构

```
src/main/java/com/greendam/birdhelp/
├── BirdHelpApplication.java    # 启动入口
├── common/                     # 公共组件
│   ├── filter/                 # 签名验证过滤器
│   └── utils/                  # JWT、RSA、OSS、AI模块调用工具
├── config/                     # Spring 配置（MVC、CORS、MyBatis-Plus）
├── constant/                   # 常量定义
├── exception/                  # 全局异常处理
├── controller/                 # 对外 REST 控制器
├── internal/                   # 供 AI 模块调用的内部 API
├── service/                    # 业务服务层
├── mapper/                     # MyBatis-Plus Mapper
├── model/
│   ├── entity/                 # 数据库实体
│   ├── dto/                    # 请求体 DTO
│   └── vo/                     # 响应体 VO
├── interceptor/                # JWT 拦截器
├── properties/                 # 配置属性类
└── task/                       # 定时任务
```

## 主要 API

| 模块      | 路径                   | 说明              |
|---------|----------------------|-----------------|
| 用户      | `/api/user/**`       | 注册、登录、个人信息、密码管理 |
| 项目      | `/api/project/**`    | 项目 CRUD、归档      |
| 文件      | `/api/file/**`       | 文件上传下载、回收站      |
| 配额      | `/api/quota/**`      | 配额查询            |
| PPT 生成  | `/api/ppt/generate`  | 生成 PPT 文档       |
| Word 生成 | `/api/word/generate` | 生成 Word 文档      |
| PDF 生成  | `/api/pdf/generate`  | 生成 PDF 文档       |

详细 API 文档见 [FRONTEND_API.md](doc/FRONTEND_API.md)，架构设计见 [DESIGN.md](doc/DESIGN.md)。

## 许可证

MIT License
