# ZXP-BE-SKEL-003 运行配置、MyBatis、Redis 和健康检查收口记录

本文记录 `ZXP-BE-SKEL-003` 的实现完成情况、验证结论和偏差说明。任务定义见 `docs/plan/06-task-implementation-checklist.md` 中 M3 后端骨架章节。

## 1. 任务目标

在 M3 后端骨架内补齐 `local/dev/test` profile、MyBatis 基础配置、Redis 基础配置和 `/api/v1/health` 健康检查，保证后端应用可以在无业务 Mapper、无真实 DB/Redis 的骨架阶段启动，并能明确报告基础设施状态。

## 2. 实现文件清单

| 模块 | 文件 | 职责 |
| --- | --- | --- |
| parent | `backend/pom.xml` | 增加 MyBatis Spring Boot 版本管理。 |
| app | `zhongxiangpin-app/pom.xml` | 增加测试依赖，支撑启动和健康检查集成测试。 |
| app | `application.yml`、`application-local.yml`、`application-dev.yml`、`application-test.yml` | 固定 profile、DB/Redis 环境变量覆盖、Hikari 启动容错、Redis 连接池和日志目录。 |
| app | `mybatis/config/mybatis-config.xml` | 固定 MyBatis 驼峰映射和 `jdbcTypeForNull`。 |
| app test | `ZhongxiangpinApplicationTest.java` | 随机端口启动 Web 环境并访问 `/api/v1/health`。 |
| contract | `HealthStatusResponse.java` | 对齐 OpenAPI `HealthStatus` 响应数据。 |
| application | `HealthCheckService.java` 等 | 编排健康检查端口，不直接依赖 JDBC、MyBatis 或 Redis。 |
| infrastructure | `MybatisInfrastructureConfiguration.java` | 注册 infrastructure 模块 Mapper 扫描范围。 |
| infrastructure | `RedisInfrastructureConfiguration.java`、`RedisRuntimeProperties.java` | 启用 Redis 运行态属性和统一 key 前缀配置。 |
| infrastructure | `DatabaseHealthIndicator.java`、`RedisHealthIndicator.java` | 分别实现 DB 与 Redis 只读探针。 |
| trigger | `HealthController.java` | 暴露 `/api/v1/health`，返回统一响应信封。 |
| trigger | `logback-spring.xml` | 日志目录改为 `zxp.logging.dir` / `ZXP_LOG_DIR` 可覆盖，默认系统临时目录。 |

## 3. Harness 证据

| 旧项目文件 | 提取行为 | 新项目决策 |
| --- | --- | --- |
| `zhongxiangpin-app/src/main/java/com/zxp/zhongxiangpin/Application.java` | 旧项目通过 `@MapperScan` 装配 Mapper。 | 新项目在 infrastructure 配置类中扫描 `com.zxp.zhongxiangpin.infrastructure`，不让 app 直接承载技术扫描细节。 |
| `zhongxiangpin-app/src/main/resources/application.yml`、`application-dev.yml` | 旧项目使用 profile、MySQL、MyBatis 和 Redis 配置。 | 新项目固定 `local/dev/test`，敏感值只通过环境变量覆盖，不写云服务器地址或真实密码。 |
| `zhongxiangpin-infrastructure/src/main/java/.../RedisConfiguration.java` | 旧项目使用 `StringRedisTemplate` 承载 session、缓存和运行态。 | M3 只启用 Spring Boot Data Redis 自动配置和 key 前缀属性，业务 key/TTL 留给后续任务。 |
| `zhongxiangpin-trigger/src/main/java/.../HealthController.java` | 旧项目已有简单健康端点。 | 新项目按 OpenAPI 返回 `app/db/redis/profile`，并使用统一 `ApiResponse` 信封。 |

## 4. 事务边界说明

健康检查不启动业务事务。`HealthCheckService` 只编排只读探针；DB 探针执行单次 `SELECT 1`，Redis 探针执行 `PING`，两者互不共享事务或业务上下文。单个探针异常只把对应组件标记为 `DOWN`，不影响统一响应返回，也不修改任何业务状态。

后续涉及订单、支付、退款、Redis 占位释放和可靠事件的事务边界，仍由对应 Application 用例任务单独定义；本任务不提前落业务事务。

## 5. 验证结果

| 验证项 | 结果 |
| --- | --- |
| `mvn test` | 通过，完整 Reactor 绿。 |
| 健康检查集成测试 | `ZhongxiangpinApplicationTest` 使用随机端口启动 Web 环境，访问 `/api/v1/health` 并断言统一响应、traceId、`app/db/redis/profile` 字段。 |
| 探针单元测试 | 覆盖 DB `SELECT 1` 成功/异常值、Redis `PING` 成功/异常值、Application 单探针失败降级。 |
| `git diff --check` | 通过。 |
| 本地 jar 手工启动 | 已打包成功；直接脚本后台启动受当前 PowerShell/Job 环境限制，不作为最终验收，已由随机端口集成测试替代。 |

## 6. 偏差与说明

| 项目 | 说明 |
| --- | --- |
| MyBatis Mapper warning | 当前阶段尚未创建业务 Mapper，启动测试出现 “No MyBatis mapper was found” 属预期状态；`ZXP-BE-SKEL-004` 后会逐步补充仓储端口和 Mapper。 |
| Redis 业务 key | 本任务只固定 `zxp.redis` 前缀配置，不创建验证码、session、缓存、锁单或 Lua key。 |
| OpenAPI | 未修改 OpenAPI；实现沿用既有 `/api/v1/health` 和 `HealthStatus` 契约。 |
| migration | 未修改 migration；DB 探针不依赖业务表。 |

## 7. 开发规范检查

- 阿里巴巴 Java 开发手册：已检查，新增类命名、包边界、异常处理、配置和测试结构符合当前阶段要求。
- 注释完整性：已检查，Controller endpoint、Application 用例、基础设施端口和 DB/Redis 探针均补充 Javadoc 或必要块注释。
- 编码格式：已执行 `git diff --check`，UTF-8/LF/缩进未发现问题。
- 静态扫描：暂未执行 P3C，当前项目尚未引入 P3C/CI；以 `mvn test` 和 `git diff --check` 作为替代验证。
