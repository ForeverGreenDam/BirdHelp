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
├── controller/               (PptController, WordController, PdfController — async task submission)
├── exception/
│   ├── ErrorCode             (enum: 0=ok, 40000=params error, 40100=not login, 50000=system error, ...)
│   ├── BusinessException     (runtime, wraps ErrorCode)
│   └── GlobalExceptionHandler (@RestControllerAdvice)
├── handler/MyMetaObjectHandler  (MyBatis-Plus MetaObjectHandler for auto-fill)
├── interceptor/JwtTokenInterceptor  (validates JWT, sets userId into BaseContext)
├── internal/                 (internal controllers for AI module — RSA signed)
│   ├── FileInternalController
│   ├── QuotaInternalController
│   └── TaskInternalController  (task callback + progress)
├── mapper/                   (MyBatis-Plus mapper interfaces)
├── model/
│   ├── entity/               (BaseEntity audit superclass, SysUser, ...)
│   ├── dto/                  (GeneratePptDTO, DocGenerationMessage, TaskCallbackRequest, ...)
│   └── vo/                   (DocGenerateTaskVO, PptGenerateResultVO, ...)
├── properties/               (@ConfigurationProperties classes, incl. RabbitMQProperties)
└── service/
    └── impl/                 (IService/ServiceImpl from MyBatis-Plus)
```

### Key design patterns

**Audit fields via BaseEntity inheritance.** All entity classes extend `BaseEntity`, which provides `createTime`, `createBy`, `updateTime`, `updateBy`, `delFlag` (logical delete). MyBatis-Plus auto-fills these via `MyMetaObjectHandler` — timestamps from `LocalDateTime.now()`, userId from `BaseContext.getCurrentId()`.

**Request context flow:** `JwtTokenInterceptor.preHandle()` → parses JWT → `BaseContext.setCurrentId(userId)` → persists for request duration → clean up in `afterCompletion` (currently missing — should be added). The userId is then available to `MyMetaObjectHandler` during INSERT/UPDATE fills.

**Response format.** All controllers should return `BaseResponse<T>` (or `BaseResponse<Void>`). The exception handler automatically wraps `BusinessException` into `BaseResponse.error()`.

**MyBatis-Plus configuration:** camelToUnderscore, ASSIGN_ID strategy, `delFlag` = logical delete column. Mapper XML in `classpath*:/mapper/**/*.xml`.

**JWT:** token from request header `token`, parsed with HS256. TTL is 5 minutes (300000ms) in dev. Token contains `id` claim (userId).

**RabbitMQ async document generation:** PPT/Word/PDF generation is now asynchronous. Controllers publish messages to
exchange `birdhelp.doc.generation` with routing keys `doc.generate.ppt`/`word`/`pdf`. Python AI module consumes from
`birdhelp.doc.generation.tasks` queue, generates the document, uploads the file, and calls back to
`/internal/task/callback`. Task results are stored in Redis (24h TTL). Full protocol: `doc/RABBITMQ_ASYNC_PROTOCOL.md`.

## Conventions

- Mapper interfaces extend `BaseMapper<Entity>`, services extend `IService<Entity>` / `ServiceImpl<Mapper, Entity>`. Empty by default — MyBatis-Plus provides all standard CRUD.
- Controllers are protected by JWT interceptor on `/user/**` paths (excluding `/user/login`).
- Entity fields use camelCase; MyBatis-Plus converts to snake_case DB columns.
- `Long` types serialize to JSON as strings (per `JsonConfig`) to avoid JS precision loss.
