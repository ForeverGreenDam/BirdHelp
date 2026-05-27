# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
mvn clean package          # build (tests included)
mvn spring-boot:run        # run dev server
mvn test                   # run all tests
mvn test -Dtest=ClassName  # run a single test class
```

Active profile: `dev`. Server starts on port **7890**, context path `/api`.
Knife4j API docs: http://localhost:7890/api/doc.html (basic auth: admin/123456).

## Architecture

Spring Boot 2.6.13 + MyBatis-Plus 3.5.3.1 + RabbitMQ, Java 19/21, MySQL, Redis.

### Package layout

```
com.greendam.birdhelp
├── BirdHelpApplication       (entry point, @MapperScan, @EnableRabbit)
├── common/
│   ├── BaseResponse<T>       (uniform API response: {code, data, message})
│   ├── context/BaseContext   (ThreadLocal<Long> for current userId)
│   ├── filter/               (SignFilter — RSA signature verification for /internal/**)
│   └── utils/                (JwtUtil, AliOssUtil, RsaSignUtil, AiModuleCaller,
│                              DocGenerationPublisher — RabbitMQ message publishing)
├── config/                   (CORS, JSON, OSS, RabbitMQ, WebMVC interceptor reg)
├── constant/                 (ErrorConstant, JwtClaimsConstant, UserRoleConstant)
├── controller/
│   ├── AnnouncementController    (user-facing announcement query)
│   ├── admin/                    (admin-side controllers — protected by JwtTokenAdminInterceptor)
│   │   ├── AdminAuthController       (admin login)
│   │   ├── AdminUserController       (user CRUD)
│   │   ├── AdminQuotaController      (quota config/user/list/log)
│   │   ├── AdminApiKeyController     (LLM API key CRUD)
│   │   ├── AdminOperationLogController (audit log query)
│   │   ├── AdminDashboardController  (stats)
│   │   ├── AdminProjectController    (project oversight)
│   │   ├── AdminFileController       (file oversight)
│   │   ├── AdminTaskController       (task list + retry)
│   │   └── AdminAnnouncementController (announcement CRUD)
│   ├── PptController
│   ├── WordController
│   └── PdfController
├── exception/
│   ├── ErrorCode             (enum: 0=ok, 40000=params error, 40100=not login, 50000=system error, ...)
│   ├── BusinessException     (runtime, wraps ErrorCode)
│   └── GlobalExceptionHandler (@RestControllerAdvice)
├── handler/MyMetaObjectHandler  (MyBatis-Plus MetaObjectHandler for auto-fill)
├── interceptor/
│   ├── JwtTokenInterceptor       (user JWT — reads "token" header, uses userSecretKey)
│   └── JwtTokenAdminInterceptor  (admin JWT — reads "admin-token" header, uses adminSecretKey)
├── internal/                 (internal controllers for AI module — RSA signed)
│   ├── FileInternalController
│   ├── QuotaInternalController
│   ├── TaskInternalController  (task callback + progress)
│   └── ApiKeyInternalController (fetch decrypted API keys for LLM calls)
├── mapper/                   (MyBatis-Plus mapper interfaces)
├── model/
│   ├── entity/               (BaseEntity audit superclass, SysUser, ApiKey, OperationLog, Announcement, ...)
│   ├── dto/
│   │   └── admin/            (AdminLoginDTO, ApiKeyCreateDTO, AnnouncementCreateDTO, ...)
│   └── vo/
│       └── admin/            (AdminUserVO, ApiKeyVO, DashboardVO, ...)
├── properties/               (@ConfigurationProperties classes, incl. RabbitMQProperties)
└── service/
    ├── admin/                    (admin-specific services for new domains)
    │   ├── ApiKeyService / impl
    │   ├── OperationLogService / impl
    │   ├── AnnouncementService / impl
    │   └── DashboardService / impl
    └── impl/                     (existing services: SysUser, Quota, Project, File)
```

### Key design patterns

**Audit fields via BaseEntity inheritance.** All entity classes extend `BaseEntity`, which provides `createTime`, `createBy`, `updateTime`, `updateBy`, `delFlag` (logical delete). MyBatis-Plus auto-fills these via `MyMetaObjectHandler` — timestamps from `LocalDateTime.now()`, userId from `BaseContext.getCurrentId()`.

**Request context flow:** `JwtTokenInterceptor.preHandle()` → parses JWT → `BaseContext.setCurrentId(userId)` → persists for request duration → clean up in `afterCompletion` (currently missing — should be added). The userId is then available to `MyMetaObjectHandler` during INSERT/UPDATE fills.

**Response format.** All controllers should return `BaseResponse<T>` (or `BaseResponse<Void>`). The exception handler automatically wraps `BusinessException` into `BaseResponse.error()`.

**MyBatis-Plus configuration:** camelToUnderscore, ASSIGN_ID strategy, `delFlag` = logical delete column. Mapper XML in `classpath*:/mapper/**/*.xml`.

**JWT:** user token from request header `token`, admin token from request header `admin-token`. Both parsed with HS256
but use different secret keys (userSecretKey vs adminSecretKey). User TTL: 1 hour, Admin TTL: 2 hours.

**Admin auth:** Separate `JwtTokenAdminInterceptor` protects `/admin/**` paths. Admin login uses `adminSecretKey` and
requires `userType=2`. Admin-only code lives in `admin/` subdirectories under controller/, service/, model/dto/,
model/vo/.

**API Key management:** LLM API keys are stored AES-encrypted in the `api_key` table (chat models only;
embedding model is hardcoded in Python). Document generation credentials (`apiKey`, `baseUrl`, `modelName`) are
passed directly via RabbitMQ messages, so Python no longer calls `/internal/api-key/fetch` for each generation.
The fetch endpoint is only used at startup to initialize the embedding model.

**RabbitMQ async document generation:** PPT/Word/PDF generation is now asynchronous. Controllers publish messages to
exchange `birdhelp.doc.generation` with routing keys `doc.generate.ppt`/`word`/`pdf`. Python AI module consumes from
`birdhelp.doc.generation.tasks` queue, generates the document, uploads the file, and calls back to
`/internal/task/callback`. Task results are stored in Redis (24h TTL). Full protocol: `doc/RABBITMQ_ASYNC_PROTOCOL.md`.

## Conventions

- All `.java` files MUST have full Javadoc comments in HTML format:
    - **Classes**: `@author ForeverGreenDam`, descriptive `<p>` paragraph, `<h3>` sub-sections when needed, `<ul><li>`
      or `<ol><li>` for lists.
    - **Public/protected methods**: `@param` for every parameter, `@return` (unless void), `@throws` for every
      checked/business exception with error code.
    - **Entities**: class-level Javadoc naming the mapped table and enumerating each field with its meaning and value
      constraints.
    - **DTOs/VOs**: class-level Javadoc listing all fields and their meanings, plus per-field `/** */` comments.
    - Use `{@code }` for inline code values, `{@link }` for cross-references to other classes.
- Mapper interfaces extend `BaseMapper<Entity>`, services extend `IService<Entity>` / `ServiceImpl<Mapper, Entity>`. Empty by default — MyBatis-Plus provides all standard CRUD.
- Controllers are protected by JWT interceptor on `/user/**` paths (excluding `/user/login`). Admin controllers on
  `/admin/**` (excluding `/admin/login`).
- Entity fields use camelCase; MyBatis-Plus converts to snake_case DB columns.
- `Long` types serialize to JSON as strings (per `JsonConfig`) to avoid JS precision loss.
- New SQL migration files go in `src/main/resources/sql/`.
- New documentation goes in `doc/`.
- Admin-only code goes in `admin/` subdirectories under the respective layer (controller/admin/, service/admin/,
  model/dto/admin/, model/vo/admin/).
