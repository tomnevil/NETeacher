# NETeacher 后端（Spring Boot 3 模块化单体）

基于 `docs/` 中《技术选型对比与决策》《系统架构设计》落地的后端骨架。当前为**全栈脚手架骨架**：已打通「统一返回 / 全局异常 / JWT 鉴权 / AI 适配器占位 / 7 个业务模块边界 / OpenAPI 文档」，**尚未接入真实业务数据与外部 AI**。

## 技术栈

- Java 17 + Spring Boot 3.3.x
- Spring Data JPA（dev 用 H2 内存库，prod 用 MySQL 8）
- Redis 7（会话/缓存/限流，prod 接入）
- MongoDB 7（学习行为日志，prod 接入）
- springdoc-openapi（API 文档）
- JJWT（JWT 鉴权）

## 模块划分

| 模块 | 职责 |
| --- | --- |
| `neteacher-common` | 公共：统一返回、异常、JWT、AI 适配器端口、基础实体 |
| `neteacher-user` (M1) | 用户与账号：注册登录、家长绑定、个人中心 |
| `neteacher-course` (M2) | 课程与内容：分级课程、题库 |
| `neteacher-learning` (M3) | 学习引擎：单词/语法/口语/听力/对话 |
| `neteacher-assessment` (M4) | 评测服务：单元测/阶段测 |
| `neteacher-recommend` (M5) | 推荐与路径：三维推荐、学习地图 |
| `neteacher-progress` (M6) | 进度与激励：仪表盘、勋章、积分 |
| `neteacher-ops` (M7) | 运营与订单：会员、活动 |
| `neteacher-server` | 主应用：聚合以上模块，单进程运行（模块化单体） |

> 模块边界按微服务粒度划分，未来可平滑拆分为独立服务。

## 本地环境（开发机已配置）

本机已安装并验证通过：

- **JDK 21**：`E:\Tom\tools\jdk21\jdk-21.0.6+7`（Microsoft Build of OpenJDK），用户级 `JAVA_HOME` 已写入
- **Maven 3.9.9**：`E:\Tom\tools\maven`，用户级 `MAVEN_HOME` 已写入
- 两者 `bin` 已加入用户级 `Path`，新开终端执行 `java -version` / `mvn -version` 即可使用

> 若在当前 IDE 宿主会话内运行命令而报 `java/mvn 不是内部或外部命令`，请先刷新进程环境变量：
> ```powershell
> $env:JAVA_HOME  = [Environment]::GetEnvironmentVariable('JAVA_HOME','User')
> $env:MAVEN_HOME = [Environment]::GetEnvironmentVariable('MAVEN_HOME','User')
> $env:Path = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;" + $env:Path
> ```

依赖下载使用阿里云镜像加速（见 `scripts/settings.xml`）。

## 运行方式

### 方式一：本地直接跑（无需 Docker，使用 H2 内存库）

前置：JDK 17+ 与 Maven 3.9+。

```bash
cd backend
mvn clean package -DskipTests
java -jar neteacher-server/target/neteacher-server-1.0.0-SNAPSHOT.jar
```

默认 `spring.profiles.active=dev`，使用 H2 内存库，启动即可访问：

- API 基址：`http://localhost:8080/api`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- H2 控制台：`http://localhost:8080/h2-console`（JDBC URL：`jdbc:h2:mem:neteacher`）
- 健康检查：`http://localhost:8080/actuator/health`

### 方式二：连真实中间件（Docker）

```bash
docker compose up -d          # 启动 MySQL/Redis/MongoDB
mvn clean package -DskipTests
java -jar neteacher-server/target/neteacher-server-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## 鉴权说明

- 登录：`POST /api/auth/login`（骨架阶段验证码任意），返回 `token`
- 其它 `/api/**` 接口需在请求头携带 `Authorization: Bearer <token>`
- 登录链路当前为演示实现（`AuthService` 直接签发 token），后续接入短信网关与用户表

## 待办（落到业务）

- 各模块 Service / Repository 真实实现与表结构（依据 `docs/03-engineering/数据字典.md`）
- AI 适配器替换为腾讯云智聆 SOE / TTS / 混元（见 `docs/03-engineering/技术选型对比与决策.md`）
- 分布式事务（Outbox 事件表）、消息队列、限流（见 `docs/03-engineering/系统架构设计.md`）
