# 任务实施清单

## 1. 使用方式

这份清单是项目唯一任务事实源，用于把已有项目的真实实现、当前规划文档和新项目目标对齐，后续按任务逐步手写代码。任务编号、任务范围、验收标准和完整推进顺序只在本文维护；`docs/plan/04-roadmap.md` 只管阶段，`docs/sdd/tasks.md` 只管门禁和依赖。旧项目只作为业务行为、接口形态和异常场景参考，不复制源码。

执行任何任务前，先读取：

- `docs/sdd/tasks.md`：确认任务就绪门禁和依赖顺序。
- `docs/harness/reference-map.md`：确认旧项目参考文件。
- 当前任务对应 SDD：后端、用户端、管理端或 Mock 策略。
- `docs/plan/07-development-standard.md`：确认阿里巴巴 Java 开发手册、注释和质量门禁要求。

每个任务建议按以下标准完成：

- M1 只更新或确认 `docs/plan/openapi.yaml`、状态枚举、错误码、Mock 示例和字段映射，不创建后端 Java 模块。
- M2 只更新或确认 migration、表结构、唯一键、索引、状态持久化和数据事实源，不创建 Mapper、Repository 或领域代码。
- 从 M3 后端骨架开始，再落地 Maven 模块、统一响应、异常处理、基础配置和健康检查。
- 从 M4 业务任务开始，再写领域模型、用例编排、基础设施适配、HTTP 入口和测试。
- 每个任务只提交一个可验证增量。
- 所有高风险任务必须有验收用例，尤其是身份、金额、状态机、并发和补偿。
- 每个实现任务记录 Harness 证据：旧文件、提取行为、隐性约束、新实现决策和偏差。
- 每个后端实现任务记录开发规范检查：阿里巴巴 Java 开发手册适用情况、注释完整性、编码格式、静态扫描或替代验证结果。

任务字段说明：

| 字段 | 含义 |
| --- | --- |
| 参考依据 | 从已有代码或规划文档中抽取的行为依据 |
| 目标落地 | 新项目中要创建或修改的模块 |
| 实现要点 | 手写代码时必须覆盖的细节 |
| 验收标准 | 任务完成后必须能证明的结果 |
| 建议提交 | 推荐 Git commit message |

## 2. 里程碑总览

| 里程碑 | 目标 | 可展示价值 |
| --- | --- | --- |
| M0 项目治理 | 文档、契约、任务清单、Git 规范成型 | 展示需求拆解和工程规划能力 |
| M1 契约与模型 | OpenAPI、错误码、状态枚举、Mock 示例和字段映射 | 展示契约先行和领域建模能力 |
| M2 数据库与 migration | 用户、商品、活动、交易、可靠事件、标签和审计表 | 展示交易事实源和一致性建模能力 |
| M3 后端骨架 | Maven 多模块、统一响应、异常、配置和健康检查 | 展示清晰分层和基础工程能力 |
| M4 认证与权限 | 用户登录态、管理员权限、审计上下文 | 展示身份可信和越权防护 |
| M5 会场与试算 | 多商品会场、折扣、标签、缓存 | 展示营销规则建模和高频读优化 |
| M6 交易闭环 | 开团、参团、锁单、支付、退款、订单 | 展示交易状态机、幂等和并发控制 |
| M7 运行态治理 | 超时关单、过期队伍、补偿、巡检、通知 | 展示最终一致和自愈能力 |
| M8 管理后台 API | 活动、标签、订单、任务、DCC、线程池、审计 | 展示业务治理平台能力 |
| M9 用户端 | 登录、会场、详情、结算、支付结果、订单 | 展示完整用户体验闭环 |
| M10 管理端 | 治理页面、类型化接口、操作反馈 | 展示运营后台产品化能力 |
| M11 测试矩阵与验收 | 自动化测试、并发一致性、前端质量、端到端验收 | 展示高风险场景验证能力 |
| M12 Git、CI、发布 | CI、tag、发布和回滚 | 展示企业级交付能力 |

## 3. M0 项目治理与文档

### ZXP-DOC-001 初始化文档入口

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新项目已有 `README.md`、`docs/plan/00-overview.md` 到 `05-validation.md` |
| 目标落地 | `README.md`、`docs/plan/` |
| 实现要点 | 保持项目定位为高质量工程项目；文档入口覆盖概览、架构、业务、Git、路线图、验证和本任务清单；所有表述围绕工程质量、业务完整度和竞争力展开 |
| 验收标准 | README 可以引导新成员从 0 理解项目目标、目录和开发顺序 |
| 建议提交 | `docs: add implementation checklist entry` |

### ZXP-DOC-002 建立任务卡规范

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 用户要求任务要有更多细节，避免后续实现时出现问题 |
| 目标落地 | `docs/plan/06-task-implementation-checklist.md` |
| 实现要点 | 统一任务编号、参考依据、目标模块、实现要点、验收标准、验证方式和提交信息；后续每完成一个任务，把实际偏差记录到任务下方 |
| 验收标准 | 任意任务都能独立成为一个 PR 或一次小提交 |
| 建议提交 | `docs: define task execution format` |

### ZXP-DOC-003 建立决策记录

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新项目需要逐步提升亮点，避免架构选择无法复盘 |
| 目标落地 | `docs/adr/`，如 `docs/adr/0001-auth-session.md` |
| 实现要点 | 每个关键选择记录背景、备选方案、最终决策和影响；优先记录登录态、Redis/MySQL 一致性、任务表、缓存策略、前端状态管理 |
| 验收标准 | 面试或复盘时能说明为什么这样设计，而不是只说明做了什么 |
| 建议提交 | `docs: add architecture decision records` |

### ZXP-DOC-004 建立项目开发规范

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 用户要求项目开发必须遵守阿里巴巴 Java 开发手册，并且代码包含完整详细的注释；项目需要把规范固化到每次 Agent 执行流程中 |
| 目标落地 | `docs/plan/07-development-standard.md`、`AGENTS.md`、`README.md`、`docs/sdd/tasks.md`、`docs/plan/06-task-implementation-checklist.md` |
| 实现要点 | 新增项目开发规范文档；明确后端 Java 以阿里巴巴 Java 开发手册和 P3C 规约为基准；明确 public 类、接口、枚举、用例入口、领域服务、Repository port、Controller endpoint、Scheduler、任务 handler 的 Javadoc 要求；明确类、方法、字段、常量、枚举、方法内部、SQL 的注释格式；明确 UTF-8、LF、缩进、末尾换行、Maven 编译编码、数据库字符集和时区要求；明确状态机、幂等、事务、并发、补偿、权限和审计逻辑必须写清业务意图与边界；把规范检查加入 Agent 上下文读取顺序、任务就绪门禁和 PR/提交检查清单 |
| 验收标准 | 后续任意后端任务开始前都会读取开发规范；任务总结必须说明阿里巴巴 Java 开发手册适用情况、注释完整性、编码格式和静态扫描或替代验证结果 |
| 验证方式 | 手工检查 `AGENTS.md`、`docs/sdd/tasks.md`、README 导航和任务清单均已引用开发规范；检查 `.editorconfig` 和 `.gitattributes` 已存在；后续 CI 建立后补充 P3C、编码检查或等价静态扫描 |
| 建议提交 | `docs: add project development standards` |

### ZXP-DOC-005 建立 Redis 运行态规范

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新项目规划中 Redis 承担 session、验证码、缓存、Lua 占位、任务锁和运行态巡检；`docs/plan/02-business-design.md` 已明确 Redis 只做缓存和运行态，MySQL 是交易事实源 |
| 目标落地 | `docs/plan/09-redis-runtime-standard.md`、`README.md`、`docs/README.md`、`docs/plan/02-business-design.md`、`docs/plan/06-task-implementation-checklist.md` |
| 实现要点 | 新增 Redis 运行态规范文档；明确 Redis 与 MySQL 事实源边界；统一 key 前缀、业务域、敏感信息处理和 TTL；固定验证码、session、会场缓存、标签命中、锁单占位、任务锁和巡检重建的使用规则；明确 Redis 成功 MySQL 失败、MySQL 成功 Redis 失败、Redis 丢失或脏数据时的处理策略；明确 Lua 只用于 Redis 内部原子操作，不替代 MySQL 条件更新和唯一键；把 Redis 规范加入文档导航、文档地图和任务启动检查 |
| 验收标准 | 后续涉及 Redis session、缓存、Lua、任务锁或运行态巡检的任务，都能先从 Redis 运行态规范确认 key、TTL、一致性和验证边界；本任务不创建后端 Java 模块、Redis 配置类、Lua 脚本或真实运行配置 |
| 验证方式 | 手工检查 README、docs/README、业务设计和任务清单均已引用 Redis 运行态规范；检查文档未包含敏感连接串或密码；检查 Redis 用途均能映射到 M3 到 M11 的现有任务编号；执行 `git diff --check` |
| 建议提交 | `docs: add redis runtime standard` |

## 4. M1 契约、错误码与状态枚举

### ZXP-CONTRACT-001 完善统一响应契约

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `Response`、`ResponseCode`；新文档 `openapi.yaml` 最小骨架 |
| 目标落地 | `docs/plan/openapi.yaml`、`docs/plan/08-contract-alignment.md`、OpenAPI 示例 |
| 实现要点 | 定义 `code`、`message`、`data`、`traceId`；`message` 是唯一响应消息字段，禁止 `info`、`msg` 等别名；分页结构统一为 `pageNo/pageSize/total/items`；错误响应也返回同一结构，并通过 400/401/403/404/409/500 等 HTTP 状态表达协议层语义；时间统一 ISO-8601 字符串 |
| 验收标准 | OpenAPI 能表达成功和失败响应；前后端不再各自定义返回体 |
| 验证方式 | OpenAPI lint；手动检查所有接口引用统一 `ApiResponse` |
| 建议提交 | `docs: define unified api response contract` |

### ZXP-CONTRACT-002 定义身份认证接口

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `AuthController`：注册、发送验证码、密码登录、验证码登录、当前用户、登出；`AuthServiceImpl`：用户名规则、手机号规则、Redis session |
| 目标落地 | `docs/plan/openapi.yaml`、认证 schema 命名约定、认证 Mock 示例 |
| 实现要点 | 接口包括 `POST /api/v1/auth/register`、`POST /api/v1/auth/code`、`POST /api/v1/auth/login/password`、`POST /api/v1/auth/login/code`、`GET /api/v1/auth/me`、`POST /api/v1/auth/logout`；登录返回 token 和最小用户信息；管理员端复用登录接口但要校验角色 |
| 验收标准 | 用户端和管理端都能基于契约生成或手写类型 |
| 验证方式 | schema 字段与 OpenAPI 示例一致；登录失败场景有错误码 |
| 建议提交 | `docs: add auth api contract` |

### ZXP-CONTRACT-003 定义会场和试算接口

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `MarketController`、`MarketServiceImpl`、用户端 `userApi.queryActivityList` 和 `queryMarket` |
| 目标落地 | `docs/plan/openapi.yaml`、会场 schema 命名约定、会场 Mock 示例 |
| 实现要点 | 接口包括会场分页列表、商品详情或试算；列表返回商品、活动、拼团价、统计和 top teams；试算返回价格快照、活动快照、命中状态和可参团队伍；后端 userId 来源以后端 session 为准 |
| 验收标准 | 前端无需拼装复杂业务数据即可展示会场和详情 |
| 验证方式 | Mock 示例覆盖普通活动、标签未命中、活动过期、无可参团队伍 |
| 建议提交 | `docs: add market and trial api contract` |

### ZXP-CONTRACT-004 定义交易、支付、退款和订单接口

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `TradeController`、`OrderController`、`TradeServiceImpl`、用户端订单类型 |
| 目标落地 | `docs/plan/openapi.yaml`、交易/订单 schema 命名约定、幂等失败 Mock 示例 |
| 实现要点 | 锁单接口使用 `clientOrderNo`；支付接口使用 `payNo`；退款接口使用 `refundNo`、`refundSource`、`reason`；订单列表只查询当前用户；订单详情必须归属校验；Debug 接口只允许 local profile |
| 验收标准 | 交易链路中每个外部请求都有幂等键和可回放响应定义 |
| 验证方式 | OpenAPI 示例覆盖重复锁单、重复支付、重复退款 |
| 建议提交 | `docs: add trade and order api contract` |

### ZXP-CONTRACT-005 定义后台治理接口

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 管理端 `admin.ts`：task、activity、dcc、tag、order、threadPool 接口；后台规划文档 |
| 目标落地 | `docs/plan/openapi.yaml`、后台治理 schema 命名约定、后台 Mock 示例 |
| 实现要点 | 活动配置、SKU 绑定、标签任务保存/更新/执行/命中检查、订单列表/详情/退款、通知任务/补偿任务列表/详情/执行/重试、DCC 配置、线程池配置、审计日志查询；所有写操作记录 operator |
| 验收标准 | 管理端不再使用 `any`，所有列表、表单和操作弹窗都有类型依据 |
| 验证方式 | TypeScript 类型检查不依赖隐式 any；OpenAPI 示例覆盖分页和操作失败 |
| 建议提交 | `docs: add admin governance api contract` |

### ZXP-CONTRACT-006 定义状态枚举和错误码

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `TradeOrderStatus`、`GroupBuyOrderStatus`、`NotifyTaskStatus`、`TradeCompensationTaskStatus`、`UserRole`、`ResponseCode` |
| 目标落地 | `docs/plan/openapi.yaml`、`docs/plan/08-contract-alignment.md`、前后端枚举命名约定 |
| 实现要点 | 订单状态只允许 `WAIT_PAY/PAID/REFUNDED/CLOSED_TIMEOUT`；队伍状态只允许 `PROGRESS/COMPLETE/EXPIRED_UNFORMED/COMPLETE_AFTER_REFUND`；任务状态定义 `INIT/PROCESSING/SUCCESS/FAILED/RETRY_WAIT`；错误码按认证、会场、交易、后台、系统分段 |
| 验收标准 | 状态机和前端展示使用同一套枚举，不写魔法字符串 |
| 验证方式 | OpenAPI 枚举值、错误码分段和契约对齐映射一致；后续 M3/M9/M10 再落地 Java/TypeScript 枚举 |
| 建议提交 | `docs: define status enums and error codes` |

### M1 收口记录

| 项目 | 结论 |
| --- | --- |
| 当前状态 | M1 契约与模型已验证并可作为后续阶段输入。 |
| 覆盖范围 | OpenAPI 已覆盖 P0 用户端、管理端和运行态治理端点，可支撑后续后端、前端和 Mock 围绕同一契约推进。 |
| 统一响应 | 响应体统一为 `code/message/data/traceId`；分页结构统一为 `pageNo/pageSize/total/items`。 |
| 枚举与错误码 | 状态枚举和错误码已定义，并与 `docs/plan/08-contract-alignment.md` 中的契约映射保持一致。 |
| 阶段边界 | 本次 M1 收口不创建后端 Java 模块，不修改 migration，符合 M1 只固定契约、错误码、状态枚举、Mock 示例和字段映射的边界。 |
| 验证说明 | OpenAPI lint 已确认 API description valid，剩余 `localhost` 本地服务地址和健康检查缺少 4XX 响应两个可接受 warning。 |
| 解锁事项 | M2 可继续做 migration 验证；M9 用户端和 M10 管理端可基于 Mock 并行启动，真实联调仍依赖对应后端能力。 |
| M2 风险 | `deploy/migration/V1__init_schema.sql` 当前需要纳入 Git；M2 开始前继续核对标签任务字段、活动 SKU 绑定 source/channel 继承关系、审计 `traceId` 是否应强制非空、`calculatedAt/trialTime/trial_time` 映射。 |

## 5. M2 数据库与 migration

### M2 收口修订计划入口

当前 M2 已完成只读审计，但尚不能标记为收口。待修点、执行顺序、验证方案和停止条件统一记录在 `docs/superpowers/plans/2026-05-30-m2-migration-closeout.md`。下一阶段执行时，先按该计划修订 `deploy/migration/V1__init_schema.sql` 和 `docs/plan/08-contract-alignment.md`，验证通过后再在本文补充 M2 收口记录；验证前不得提前更新 README 为 M2 已完成。

### ZXP-DB-001 设计用户与权限表

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `user_account`、`AuthServiceImpl` 的 username/phone/passwordHash/role/status |
| 目标落地 | `deploy/migration`、`docs/plan/08-contract-alignment.md` |
| 实现要点 | 表包括 `user_account`；username、phone 唯一；密码只存 bcrypt hash；role 区分 USER/ADMIN；status 支持禁用；记录 create/update time；后台审计表由 `ZXP-DB-005` 统一维护 |
| 验收标准 | 支持密码登录、验证码登录自动创建账号、手机号绑定和管理员鉴权 |
| 验证方式 | migration 空库执行成功；唯一键冲突可复现 |
| 建议提交 | `feat: add user account migration` |

### ZXP-DB-002 设计商品、活动和折扣表

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `sku`、`sc_sku_activity`、`group_buy_activity`、`group_buy_discount`；`MarketServiceImpl` 试算行为 |
| 目标落地 | `deploy/migration`、`docs/plan/08-contract-alignment.md` |
| 实现要点 | 商品表保存名称、图片、原价、分类；活动表保存 source/channel、targetCount、validTime、takeLimitCount、start/end/status、tagId、version；折扣表保存 marketPlan 和规则表达式；SKU 活动绑定表保存生效关系 |
| 验收标准 | 一个商品能绑定一个或多个活动版本，试算能拿到稳定活动快照 |
| 验证方式 | 初始化数据可支撑至少 3 个商品、2 类折扣、1 个标签活动 |
| 建议提交 | `feat: add market activity migrations` |

### ZXP-DB-003 设计队伍、订单、支付和退款表

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `group_buy_order`、`group_buy_order_list`、交易状态设计；新文档要求支付/退款独立记录 |
| 目标落地 | `deploy/migration`、`docs/plan/08-contract-alignment.md` |
| 实现要点 | `team` 保存 targetCount、completeCount、lockCount、refundCount、status、validStart/End；`trade_order` 保存 orderId、clientOrderNo、userId、teamId、activityId、goodsId、occupyNo、价格快照、状态；`pay_record` 以 payNo 幂等；`refund_record` 以 refundNo 幂等并记录来源、原因、operator |
| 验收标准 | MySQL 可以用唯一键和条件更新兜底防重复、防超卖 |
| 验证方式 | 检查唯一键：`clientOrderNo+userId`、`orderId`、`teamId+occupyNo`、`payNo`、`refundNo` |
| 建议提交 | `feat: add trade order migrations` |

### ZXP-DB-004 设计可靠事件、通知和补偿表

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `notify_task`、`trade_compensation_task`、面试文档建议 Outbox + 补偿统一表 |
| 目标落地 | `deploy/migration`、`docs/plan/08-contract-alignment.md` |
| 实现要点 | 优先设计统一 `reliable_event`，用 `eventType/bizKey/status/retryCount/nextExecuteTime/payload/lastError` 承接成团通知、Redis 释放、队伍重建、超时修复、退款修复；如果第一版拆表，也要保持字段一致 |
| 验收标准 | 所有不可丢的异步副作用都能落库、重试、查询和手动触发 |
| 验证方式 | 唯一键 `eventType + bizKey`；payload 使用 JSON 对象序列化而不是字符串拼接 |
| 建议提交 | `feat: add reliable event migration` |

### ZXP-DB-005 设计标签和审计表

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `crowd_tags`、`crowd_tags_job`、`crowd_tags_detail`；后台标签任务和审计规划 |
| 目标落地 | `deploy/migration`、`docs/plan/08-contract-alignment.md` |
| 实现要点 | 标签主表保存 tagId、name、currentBatchId、statistics；任务表保存 ruleType、ruleExpr、status、batchId；明细表保存 tagId、batchId、userAccountId；审计表保存 operator、action、targetType、targetId、before/after、result |
| 验收标准 | 标签任务可重跑，线上命中永远指向完整 currentBatchId；后台写操作可追溯 |
| 验证方式 | 同 tagId 多 batch 不混淆；审计记录可按 operator 和 target 查询 |
| 建议提交 | `feat: add tag and audit migrations` |

## 6. M3 后端骨架

### ZXP-BE-SKEL-001 创建 Maven 多模块

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 Maven 模块；新架构文档要求 contract/common/domain/application/infrastructure/trigger/app |
| 目标落地 | `backend/pom.xml` 和 7 个子模块 |
| 实现要点 | 父 POM 固定 Java 17；dependencyManagement 统一版本；模块依赖方向为 `trigger -> application -> domain`，`infrastructure -> domain/application port`，`app` 负责装配；禁止 domain 依赖 Spring Web、MyBatis、Redis |
| 验收标准 | `mvn test` 在空骨架下通过；依赖方向清晰 |
| 验证方式 | Maven Enforcer 检查 Java 17；ArchUnit 可后续补充依赖规则 |
| 建议提交 | `chore: initialize backend multi-module project` |

### ZXP-BE-SKEL-002 实现统一响应和异常处理

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `Response`、`BizException`、`ResponseCode` |
| 目标落地 | `common`、`trigger/handler` |
| 实现要点 | `BizException` 携带错误码；全局异常处理返回统一 `ApiResponse`；参数校验错误返回字段级提示；响应中注入 traceId；日志不输出密码、验证码、token |
| 验收标准 | Controller 不手写 try/catch；错误响应格式稳定 |
| 验证方式 | MockMvc 覆盖参数错误、业务错误、系统错误 |
| 建议提交 | `feat: add unified response and exception handling` |

### ZXP-BE-SKEL-003 配置 profile、MyBatis、Redis 和健康检查

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目后端启动方式、Redis session、缓存、Lua 运行态 |
| 目标落地 | `app`、`infrastructure`、`trigger/health` |
| 实现要点 | profile 至少包括 local/dev/test；DB/Redis 通过环境变量覆盖；健康检查返回 app、db、redis 状态；Debug/Mock Controller 仅 local 注册 |
| 验收标准 | 本地可启动并访问 `/api/v1/health` |
| 验证方式 | 启动测试；缺少 Redis 时给出明确错误或降级说明 |
| 建议提交 | `feat: add backend runtime configuration` |

### ZXP-BE-SKEL-004 建立端口和仓储规范

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 Repository 接口与 Impl；新文档要求 application/domain 与 infrastructure 解耦 |
| 目标落地 | `domain/repository`、`application/port`、`infrastructure/repository` |
| 实现要点 | 领域层只定义仓储端口；基础设施实现 MyBatis/Redis；Repository 方法使用业务语言命名；禁止 Controller 直接调用 Mapper |
| 验收标准 | 核心领域服务可以用 fake repository 做单元测试 |
| 验证方式 | 单测不启动 Spring 也能测试状态机和规则 |
| 建议提交 | `chore: define repository port conventions` |

## 7. M4 认证、权限与审计上下文

### ZXP-BE-AUTH-001 实现注册和密码登录

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `AuthServiceImpl.register`、`loginByPassword` |
| 目标落地 | `domain/auth`、`application/auth`、`trigger/http/auth`、`infrastructure/auth` |
| 实现要点 | username 规则 `4-20` 位字母数字下划线；手机号可选但必须唯一；密码 bcrypt；账号禁用不能登录；登录后生成 token 写 Redis session；返回最小用户信息 |
| 验收标准 | 注册、重复用户名、密码错误、禁用账号、登录成功均有明确结果 |
| 验证方式 | 单元测试 + 接口测试 |
| 建议提交 | `feat: implement password auth flow` |

### ZXP-BE-AUTH-002 实现验证码登录和手机号绑定

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `AuthServiceImpl.sendCode`、`loginByCode`、`bindPhoneIfNecessary` |
| 目标落地 | `domain/auth`、`infrastructure/redis` |
| 实现要点 | 6 位验证码；Redis TTL；验证码登录时手机号不存在则创建轻量账号；密码登录时携带手机号和验证码则绑定；验证码使用后删除；本地环境可返回 mock code，非本地不直接返回验证码 |
| 验收标准 | 支持手机号首次登录、重复绑定拦截、验证码过期和错误码 |
| 验证方式 | Redis TTL 测试；接口测试覆盖 code expired/incorrect |
| 建议提交 | `feat: implement code login and phone binding` |

### ZXP-BE-AUTH-003 实现登录态拦截器和 UserContext

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `RefreshTokenInterceptor`、`LoginInterceptor`、`UserHolder`；面试文档登录链路 |
| 目标落地 | `common/context`、`trigger/interceptor` |
| 实现要点 | Refresh 拦截器只尝试解析 token 并刷新 TTL；Login 拦截器负责强制登录；请求结束清理 ThreadLocal；业务层从 UserContext 获取 userId；前端传入 userId 不可信 |
| 验收标准 | 订单查询、锁单、退款都以后端 session 用户为准 |
| 验证方式 | 伪造 userId 攻击测试；ThreadLocal 清理测试 |
| 建议提交 | `feat: add session interceptors and user context` |

### ZXP-BE-AUTH-004 实现管理员权限和审计上下文

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 `AdminInterceptor`、管理端接口、审计规划 |
| 目标落地 | `trigger/interceptor/admin`、`application/audit` |
| 实现要点 | ADMIN 角色才能访问 `/api/v1/admin/**`；写操作记录 operator、action、target、result；审计记录与业务操作尽量同事务落库 |
| 验收标准 | 普通用户访问后台接口被拒绝；后台写操作可查询审计 |
| 验证方式 | 接口测试覆盖未登录、普通用户、管理员 |
| 建议提交 | `feat: add admin authorization and audit context` |

## 8. M5 会场、试算、折扣、标签与缓存

### ZXP-BE-MARKET-001 实现会场活动列表

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `MarketController.queryMarketActivityList`、`MarketServiceImpl.queryMarketActivityList`、README 会场链路 |
| 目标落地 | `application/market`、`domain/market`、`infrastructure/market` |
| 实现要点 | pageNo 最小 1；pageSize 最大 50；source/channel 必填；category 可选；返回活动、商品、价格、统计、top teams；统计和 top teams 批量查询，避免 N+1 |
| 验收标准 | 用户端首页一次请求可展示多商品会场 |
| 验证方式 | Mapper 测试检查分页、分类、空结果；SQL 日志确认无 N+1 |
| 建议提交 | `feat: implement market activity list` |

### ZXP-BE-MARKET-002 实现活动试算服务

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `MarketServiceImpl.queryGroupBuyMarketConfig`、策略树 Root/Switch/Market/Tag/End 节点设计 |
| 目标落地 | `domain/trial`、`application/trial` |
| 实现要点 | 校验商品、活动、渠道、时间、状态；命中标签；计算折扣；返回价格快照、活动快照、团队统计和可参团队伍；锁单前必须重新试算，不能信任前端价格 |
| 验收标准 | 试算结果能直接用于详情页展示和锁单价格快照 |
| 验证方式 | 单测覆盖活动未开始、已结束、禁用、标签未命中、价格计算 |
| 建议提交 | `feat: implement trial calculation service` |

### ZXP-BE-MARKET-003 实现折扣策略

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目折扣策略 `ZJ`、`ZK`、`MJ`、`N` |
| 目标落地 | `domain/discount` |
| 实现要点 | `ZJ` 直减、`ZK` 折扣、`MJ` 满减、`N` 无优惠；金额使用 BigDecimal；实付金额不能小于 0；规则解析失败返回明确错误 |
| 验收标准 | 新增折扣策略无需改试算主流程 |
| 验证方式 | 参数化单测覆盖边界金额和精度 |
| 建议提交 | `feat: add discount calculation strategies` |

### ZXP-BE-MARKET-004 实现标签任务和在线命中

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目标签链路：USERS、PARTICIPATE_COUNT、RoaringBitmap、Redis + 本地缓存 + MySQL 回退 |
| 目标落地 | `domain/tag`、`application/tag`、`infrastructure/tag` |
| 实现要点 | 标签任务保存规则但不立即圈人；执行时生成 batchId；先写 MySQL 明细，再写 Redis bitmap，再更新 currentBatchId；在线命中优先本地/Redis，缺失回查 MySQL；任务状态可重试 |
| 验收标准 | 活动试算能基于 tagId 判定用户准入 |
| 验证方式 | 单测覆盖指定用户、人群参与次数、重复执行、Redis 缺失回退 |
| 建议提交 | `feat: implement crowd tag targeting` |

### ZXP-BE-MARKET-005 实现会场缓存治理

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 面试文档会场缓存：本地短缓存、Redis Cache Aside、版本号失效、互斥重建、Bloom Filter 防穿透 |
| 目标落地 | `infrastructure/cache`、`application/admin/activity` |
| 实现要点 | 会场列表 key 包含 version；活动变更递增相关 version；热点缓存失效用互斥锁重建；不存在商品写短 TTL 空值；Bloom Filter 用于商品/活动存在性判断 |
| 验收标准 | 后台修改活动后用户端不长期看到旧数据；热点回源受控 |
| 验证方式 | 缓存命中、版本失效、空值缓存、互斥重建测试 |
| 建议提交 | `feat: add market cache governance` |

## 9. M6 交易闭环

### ZXP-BE-TRADE-001 实现交易状态机

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新业务文档订单/队伍状态机；旧项目枚举和状态推进逻辑 |
| 目标落地 | `domain/trade/state` |
| 实现要点 | 订单只允许 `WAIT_PAY -> PAID -> REFUNDED`、`WAIT_PAY -> CLOSED_TIMEOUT`；队伍只允许 `PROGRESS -> COMPLETE`、`PROGRESS -> EXPIRED_UNFORMED`、`COMPLETE -> COMPLETE_AFTER_REFUND`；所有状态更新都校验前置状态 |
| 验收标准 | 非法状态流转被拒绝且有错误码 |
| 验证方式 | 状态机单测覆盖所有合法和非法转换 |
| 建议提交 | `feat: add trade state machines` |

### ZXP-BE-TRADE-002 实现开团与参团锁单

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `TradeServiceImpl.lockMarketPayOrder`、Redis Lua 占位、MySQL 兜底、takeLimitCount 校验 |
| 目标落地 | `application/trade/LockOrderUseCase`、`infrastructure/redis/lua`、`infrastructure/trade` |
| 实现要点 | 未传 teamId 创建新团；传 teamId 参团；锁单前重新试算；校验活动时间、用户资格、限购、队伍有效期和容量；Redis Lua 原子占位；MySQL 条件更新带 `complete_count + lock_count < target_count`；失败释放 Redis slot |
| 验收标准 | 并发参团不会超过 targetCount；重复 clientOrderNo 返回同一订单或明确幂等结果 |
| 验证方式 | 并发测试、重复请求测试、Redis 成功 MySQL 失败补偿测试 |
| 建议提交 | `feat: implement trade lock order flow` |

### ZXP-BE-TRADE-003 实现订单价格快照和幂等键

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新文档要求保存活动版本、折扣版本、原价、优惠、实付价、试算时间；旧项目 `outTradeNo` 幂等需要升级为 `clientOrderNo` |
| 目标落地 | `domain/order`、`infrastructure/order` |
| 实现要点 | 锁单保存 `clientOrderNo`、activityVersion、discountVersion、originalPrice、deductionPrice、payPrice、trialTime；同一用户同一 clientOrderNo 幂等；价格以后端试算为准 |
| 验收标准 | 活动后续变更不影响历史订单金额解释 |
| 验证方式 | 修改活动后查询历史订单仍显示原快照 |
| 建议提交 | `feat: persist order price snapshots` |

### ZXP-BE-TRADE-004 实现 Mock 支付结算

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `TradeServiceImpl.settlementMarketPayOrder`、旧项目支付后 `WAIT_PAY -> PAID`、团队人数更新和成团通知任务 |
| 目标落地 | `application/payment`、`domain/payment`、`infrastructure/payment` |
| 实现要点 | payNo 幂等；校验订单归属、金额、状态和队伍仍可支付；MySQL 事务内推进订单 PAID、lockCount-1、completeCount+1；达到 targetCount 改队伍 COMPLETE 并写可靠事件；事务后清理 Redis slot 且不回补 release |
| 验收标准 | 重复支付不重复增加 completeCount；队伍满员自动成团 |
| 验证方式 | 重复支付、过期队伍支付、金额不一致、成团通知事件测试 |
| 建议提交 | `feat: implement mock payment settlement` |

### ZXP-BE-TRADE-005 实现退款流程

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `TradeServiceImpl.refundMarketPayOrder`、旧项目 `ORDER_REFUND_REPAIR`；新文档退款来源区分 |
| 目标落地 | `application/refund`、`domain/refund`、`infrastructure/refund` |
| 实现要点 | refundNo 幂等；只允许 PAID 订单退款；区分 USER/ADMIN/AUTO_EXPIRED 来源；记录 reason/operator/refundTime；事务内订单 PAID->REFUNDED、团队 completeCount/refundCount/status 调整、写退款修复事件 |
| 验收标准 | 重复退款不重复扣人数；管理员退款有审计；自动退款来源可追溯 |
| 验证方式 | 用户退款、管理员退款、重复 refundNo、非法状态退款测试 |
| 建议提交 | `feat: implement refund flow` |

### ZXP-BE-TRADE-006 实现用户订单列表和详情

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `OrderController`、用户端 `OrderListView/OrderDetailView` 类型 |
| 目标落地 | `application/order`、`trigger/http/order` |
| 实现要点 | 列表只查当前用户；详情做归属校验；返回商品、团队、订单、支付、退款和倒计时；支持 canPay/canRefund 派生字段；Debug 详情只在 local profile 开启 |
| 验收标准 | 用户不能查看其他用户订单；前端订单页无需二次计算核心状态 |
| 验证方式 | 水平越权测试、状态展示测试 |
| 建议提交 | `feat: implement user order query` |

## 10. M7 运行态治理、补偿与通知

### ZXP-BE-RUNTIME-001 实现超时关单

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `TradeRuntimeScheduler.closeTimeoutOrders`、面试文档超时关单链路 |
| 目标落地 | `application/runtime`、`infrastructure/runtime`、`trigger/scheduler` |
| 实现要点 | 扫描 WAIT_PAY 且超过支付截止或队伍有效期的订单；MySQL 事务内订单改 CLOSED_TIMEOUT、团队 lockCount-1、写 ORDER_TIMEOUT_REPAIR 事件；Redis 释放异步处理并回补 release |
| 验收标准 | 未支付订单不会永久占名额 |
| 验证方式 | 重复扫描幂等；Redis 失败后事件落库 |
| 建议提交 | `feat: add timeout order closing job` |

### ZXP-BE-RUNTIME-002 实现队伍过期和未成团自动退款

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `TradeRuntimeScheduler.expireTeams`、`repairExpiredUnformedPaidOrders` |
| 目标落地 | `application/runtime` |
| 实现要点 | PROGRESS 队伍过期且未 complete 改 EXPIRED_UNFORMED；已支付但未成团订单自动退款，refundSource=AUTO_EXPIRED；每笔退款写 refund_record 和修复事件 |
| 验收标准 | 过期未成团用户最终看到已退款或退款中状态 |
| 验证方式 | 队伍过期、部分支付、重复任务测试 |
| 建议提交 | `feat: add expired team refund repair` |

### ZXP-BE-RUNTIME-003 实现 Redis/MySQL 运行态巡检和重建

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `TradeRuntimeScheduler.inspectTeamRuntime`、面试文档巡检与重建 |
| 目标落地 | `application/runtime`、`infrastructure/redis` |
| 实现要点 | 巡检 PROGRESS 且未过期团队；从 MySQL 加载 WAIT_PAY activeSlots；对比 Redis cursor/slot/release；发现不一致只挂 TEAM_REBUILD 事件；重建时以 MySQL 快照覆盖 Redis |
| 验收标准 | Redis 丢失或脏数据可从 MySQL 恢复 |
| 验证方式 | 手动删除 Redis key 后巡检产生重建事件并修复 |
| 建议提交 | `feat: add team runtime inspection and rebuild` |

### ZXP-BE-RUNTIME-004 实现可靠事件执行器

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `executeNotifyTasks`、`executeCompensationTasks`、旧项目任务治理 |
| 目标落地 | `application/event`、`infrastructure/event`、`trigger/scheduler` |
| 实现要点 | 批量拉取 due events；使用执行锁防并发重复；状态 INIT/PROCESSING/SUCCESS/FAILED；失败记录 lastError 并退避重试；超过最大次数进入 FAILED；所有 handler 幂等 |
| 验收标准 | 通知、Redis 释放、超时修复、退款修复、队伍重建共用执行机制 |
| 验证方式 | 任务重复执行、失败重试、手动重试测试 |
| 建议提交 | `feat: implement reliable event executor` |

### ZXP-BE-RUNTIME-005 增加运行态指标和可观测性

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 面试文档生产化优化：补偿积压、不一致团队、重建次数等指标 |
| 目标落地 | `common/metrics`、`admin/dashboard` |
| 实现要点 | 指标包括待处理/失败事件数、运行态不一致队伍数、名额释放失败数、队伍重建次数、锁单延迟、试算缓存命中率；日志带 traceId/orderId/teamId |
| 验收标准 | 出现异常时能通过日志和指标定位是哪条链路 |
| 验证方式 | 人为制造失败事件后后台和日志可定位 |
| 建议提交 | `feat: add runtime governance metrics` |

## 11. M8 管理后台 API

### ZXP-BE-ADMIN-001 实现活动治理 API

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 管理端 `activityList/saveActivity/bindSku`；新业务文档活动治理 |
| 目标落地 | `application/admin/activity`、`trigger/http/admin` |
| 实现要点 | 活动分页、保存、启停、绑定 SKU；校验时间、targetCount、validTime、折扣规则；变更活动后递增缓存 version；写审计日志 |
| 验收标准 | 后台变更能影响用户端会场且有审计记录 |
| 验证方式 | 活动保存、非法时间、缓存失效测试 |
| 建议提交 | `feat: add admin activity governance api` |

### ZXP-BE-ADMIN-002 实现标签治理 API

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 管理端 `tagJobList/saveTagJob/updateTagJob/executeTagJob/tagHitCheck` |
| 目标落地 | `application/admin/tag` |
| 实现要点 | 标签任务分页、保存、更新、执行、命中检查；规则支持 USERS 和 PARTICIPATE_COUNT；执行结果返回统计和 batchId；操作写审计 |
| 验收标准 | 管理员能创建标签并绑定到活动试算 |
| 验证方式 | 标签任务执行、重复执行、命中检查测试 |
| 建议提交 | `feat: add admin tag governance api` |

### ZXP-BE-ADMIN-003 实现订单治理 API

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 管理端 `orderList/orderDetail/refundOrder`；新文档管理员退款来源和审计 |
| 目标落地 | `application/admin/order` |
| 实现要点 | 支持按订单号、队伍号、用户、状态、时间查询；详情展示价格快照、支付、退款、团队状态；管理员退款必须填写原因，refundSource=ADMIN，记录 operator 和审计 |
| 验收标准 | 管理员能定位异常订单并安全退款 |
| 验证方式 | 普通用户越权、管理员退款、重复退款测试 |
| 建议提交 | `feat: add admin order governance api` |

### ZXP-BE-ADMIN-004 实现任务治理 API

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 管理端 taskOverview、notify/compensation task list/detail/execute/retry |
| 目标落地 | `application/admin/event` |
| 实现要点 | 任务概览、列表、详情、手动执行、重试；展示 payload、retryCount、nextExecuteTime、lastError；手动执行也要抢执行锁并写审计 |
| 验收标准 | 异常事件可以在后台定位和人工推动 |
| 验证方式 | FAILED 任务手动 retry 后状态变化正确 |
| 建议提交 | `feat: add admin event governance api` |

### ZXP-BE-ADMIN-005 实现 DCC 和线程池治理 API

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 管理端 `dccConfig/updateDcc/threadPoolList/threadPoolConfig/updateThreadPool` |
| 目标落地 | `application/admin/config`、`infrastructure/config` |
| 实现要点 | DCC 配置支持读取和更新；线程池参数展示 activeCount、queueSize、core/max、拒绝次数；更新参数要校验范围；所有更新写审计 |
| 验收标准 | 运行时可调整非核心风险配置并追踪操作者 |
| 验证方式 | 参数非法被拒绝；更新后线程池配置生效 |
| 建议提交 | `feat: add runtime config governance api` |

## 12. M9 用户端

### ZXP-FE-USER-001 初始化用户端工程

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧用户端 Vue 3、Vite、Pinia、Axios、Element Plus；路由 login/goods/checkout/pay-result/orders |
| 目标落地 | `frontend/user-web` |
| 实现要点 | 统一 request 封装、token 注入、401 跳登录、错误 toast；路由守卫加载当前用户；类型来自 OpenAPI 或集中声明 |
| 验收标准 | 用户端可本地启动，登录态失效处理一致 |
| 验证方式 | `npm run type-check`、`npm run build` |
| 建议提交 | `feat: initialize user web shell` |

### ZXP-FE-USER-002 实现登录与会场首页

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧用户端 `LoginView`、`GoodsHomeView`、`userApi.queryActivityList` |
| 目标落地 | `frontend/user-web/src/views` |
| 实现要点 | 登录/注册切换；会场列表支持分类、分页或加载更多；展示拼团价、原价、成团人数、倒计时、活动状态；空态和错误态清晰 |
| 验收标准 | 登录后可浏览多商品活动列表 |
| 验证方式 | Mock 数据和真实接口各跑一次 |
| 建议提交 | `feat: add user login and market home` |

### ZXP-FE-USER-003 实现商品详情、参团和确认订单

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧用户端 `GoodsDetailView`、`CheckoutView`、试算和锁单接口 |
| 目标落地 | `frontend/user-web` |
| 实现要点 | 详情页进入时试算；展示可参团队伍；开团/参团带 teamId；确认订单生成 clientOrderNo；前端不传可信 userId；锁单成功跳支付结果或收银台页 |
| 验收标准 | 用户能开团或参团并创建 WAIT_PAY 订单 |
| 验证方式 | 重复点击锁单按钮不会重复提交；失败提示明确 |
| 建议提交 | `feat: add goods detail and checkout flow` |

### ZXP-FE-USER-004 实现 Mock 支付、订单列表和详情

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧用户端 `PayResultView`、`OrderListView`、`OrderDetailView` |
| 目标落地 | `frontend/user-web` |
| 实现要点 | 支付提交生成 payNo；订单列表按状态展示 canPay/canRefund；详情展示价格快照、团队进度、支付/退款信息；退款生成 refundNo 并二次确认 |
| 验收标准 | 主链路从登录到退款可演示 |
| 验证方式 | 构建通过；完整链路手工验收 |
| 建议提交 | `feat: add user payment and order pages` |

## 13. M10 管理端

### ZXP-FE-ADMIN-001 初始化管理端工程和类型化 API

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧管理端 `admin.ts` 目前大量 `any`，新项目要求提升工程质量 |
| 目标落地 | `frontend/admin-web` |
| 实现要点 | request 封装、管理员路由守卫、布局导航；所有 API 定义 Request/Response 类型；禁止新增隐式 any；错误处理和操作确认统一 |
| 验收标准 | TypeScript strict 下通过构建 |
| 验证方式 | `npm run type-check`、`npm run build` |
| 建议提交 | `feat: initialize typed admin web shell` |

### ZXP-FE-ADMIN-002 实现活动和标签治理页面

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧管理端 `ActivityView`、`TagView` |
| 目标落地 | `frontend/admin-web/src/views` |
| 实现要点 | 活动列表、编辑表单、SKU 绑定、启停；标签任务列表、规则表单、执行、命中检查；表单校验与后端规则一致 |
| 验收标准 | 管理员能配置活动并创建标签人群 |
| 验证方式 | 表单校验、保存成功、失败提示、审计展示联调 |
| 建议提交 | `feat: add admin activity and tag pages` |

### ZXP-FE-ADMIN-003 实现订单、任务、DCC 和线程池页面

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧管理端 `OrderView`、`TaskView`、`DccView`、`ThreadPoolView` |
| 目标落地 | `frontend/admin-web/src/views` |
| 实现要点 | 订单查询和退款弹窗；任务概览、详情、执行、重试；DCC 配置修改；线程池指标和参数调整；危险操作二次确认 |
| 验收标准 | 后台能支撑交易异常排查和人工治理 |
| 验证方式 | 与后端联调每个写操作都有审计记录 |
| 建议提交 | `feat: add admin operations pages` |

### ZXP-FE-ADMIN-004 实现治理仪表盘

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新项目竞争力目标、运行态指标规划 |
| 目标落地 | `frontend/admin-web/src/views/DashboardView.vue` |
| 实现要点 | 展示今日订单、成团率、退款率、待处理事件、失败事件、缓存命中、运行态不一致数量；支持跳转到对应治理页 |
| 验收标准 | 面试演示时能从仪表盘讲清系统健康状况 |
| 验证方式 | Mock 和真实接口均可展示 |
| 建议提交 | `feat: add admin governance dashboard` |

## 14. M11 测试矩阵与验收

### ZXP-QA-001 建立后端测试分层

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧项目 ControllerTest、枚举契约测试；新验证计划 |
| 目标落地 | `backend/**/src/test` |
| 实现要点 | domain 单测覆盖状态机、折扣、标签命中；application 测试覆盖用例编排；Mapper 测试覆盖关键 SQL；Controller 测试覆盖成功/失败响应 |
| 验收标准 | 每个高风险业务都有自动化测试，不只靠手工演示 |
| 验证方式 | `mvn test` 通过 |
| 建议提交 | `test: add backend validation matrix` |

### ZXP-QA-002 建立并发和一致性测试

| 字段 | 内容 |
| --- | --- |
| 参考依据 | Redis Lua + MySQL 兜底、面试文档异常矩阵 |
| 目标落地 | `backend/integration-test` 或测试文档 |
| 实现要点 | 测试并发满团、Redis 占位成功 MySQL 失败、支付重复回调、退款重复请求、Redis 丢失后巡检重建、任务重复执行 |
| 验收标准 | 能证明不超卖、幂等、最终一致和可恢复 |
| 验证方式 | 本地集成测试或脚本化压测报告 |
| 建议提交 | `test: add trade consistency scenarios` |

### ZXP-QA-003 建立前端质量门禁

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 旧前端构建命令和新项目类型化要求 |
| 目标落地 | `frontend/user-web`、`frontend/admin-web` |
| 实现要点 | TypeScript strict、eslint、构建检查；API 类型集中；登录过期、接口错误、空态、加载态都有测试或检查清单 |
| 验收标准 | 两个前端构建通过且无隐式 any |
| 验证方式 | `npm run type-check`、`npm run build` |
| 建议提交 | `test: add frontend quality gates` |

### ZXP-QA-004 完成端到端验收脚本

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新验证计划主链路；README 演示流程 |
| 目标落地 | `docs/plan/05-validation.md`、可选 `e2e/` |
| 实现要点 | 验收链路：注册/登录 -> 会场 -> 详情 -> 试算 -> 开团/参团 -> 锁单 -> 支付 -> 订单 -> 退款 -> 后台查看订单和任务；记录每一步请求、响应、数据库状态和截图 |
| 验收标准 | P0 完整验收链路可稳定复现，失败时能定位到具体任务或模块 |
| 验证方式 | 手工验收表或 Playwright 脚本 |
| 建议提交 | `test: add end-to-end acceptance checklist` |

## 15. M12 Git、CI、发布与版本管理

### ZXP-GIT-001 建立分支策略

| 字段 | 内容 |
| --- | --- |
| 参考依据 | `docs/plan/03-git-versioning.md` 企业级版本管理规划 |
| 目标落地 | Git 仓库、README、CI |
| 实现要点 | `main` 始终可发布；功能分支使用 `feat/*` 或 `codex/*`；每个任务一个分支或一个小提交；禁止把多个领域混在一次提交；PR 描述包含任务编号、变更、验证、风险 |
| 验收标准 | 从 Git 历史能看出项目逐步成长路径 |
| 验证方式 | `git log --oneline` 语义清晰；tag 对应可复现版本 |
| 建议提交 | `docs: define branch workflow` |

### ZXP-GIT-002 建立提交规范和版本 tag

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 用户要求细致的企业级 Git 安排 |
| 目标落地 | `docs/plan/03-git-versioning.md`、`.github` |
| 实现要点 | Conventional Commits；规划阶段 tag `planning-v0.1.0`；后端骨架 `backend-skeleton-v0.2.0`；P0 完整功能 tag `p0-complete-v1.0.0`；重大架构亮点用 release note 记录 |
| 验收标准 | 每个里程碑可通过 tag 回到当时状态 |
| 验证方式 | tag 列表和 release note 对齐 |
| 建议提交 | `docs: add release tagging policy` |

### ZXP-GIT-003 建立 CI 质量门禁

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 新项目竞争力目标和验证计划 |
| 目标落地 | `.github/workflows` |
| 实现要点 | 后端执行 `mvn test`；用户端和管理端执行 install/type-check/build；OpenAPI lint；可选 markdown lint；CI 失败不合并 main |
| 验收标准 | main 分支质量由自动化门禁保护 |
| 验证方式 | 推送 PR 后 CI 全绿 |
| 建议提交 | `ci: add project quality gates` |

### ZXP-GIT-004 建立发布和回滚文档

| 字段 | 内容 |
| --- | --- |
| 参考依据 | 路线图 M12 发布演练 |
| 目标落地 | `docs/deploy/release-notes`、`docs/plan/03-git-versioning.md` |
| 实现要点 | 每次发布记录版本号、提交范围、数据库 migration、配置变更、验证结果、回滚方式；回滚要说明应用回滚和数据库回滚策略 |
| 验收标准 | 项目不只是能跑，还能说明如何上线和回退 |
| 验证方式 | 按文档从 tag 重新构建一次 |
| 建议提交 | `docs: add release and rollback guide` |

## 16. P0 项目推进顺序

本节只维护完整执行顺序，不重复任务细节。同一阶段内的任务在满足 `docs/sdd/tasks.md` 门禁后可以并行，但不得越过阶段边界提前落地后端 Java、Mapper、Repository 或业务实现。

| 顺序 | 任务 | 解锁条件或说明 |
| --- | --- | --- |
| 1 | `ZXP-DOC-001` -> `ZXP-DOC-002` -> `ZXP-DOC-003` -> `ZXP-DOC-004` -> `ZXP-DOC-005` | 先统一文档入口、任务卡、ADR、开发规范和 Redis 运行态规范；`ZXP-DOC-004` 和 `ZXP-DOC-005` 必须先进入后续门禁。 |
| 2 | `ZXP-GIT-001` -> `ZXP-GIT-002` | 建立分支、提交和 tag 规则，避免后续任务落在不可追溯历史中。 |
| 3 | `ZXP-CONTRACT-001` -> `ZXP-CONTRACT-002` -> `ZXP-CONTRACT-003` -> `ZXP-CONTRACT-004` -> `ZXP-CONTRACT-005` -> `ZXP-CONTRACT-006` | 先固定统一响应，再补认证、会场、交易、后台治理和枚举错误码；M1 不创建后端 Java 代码。 |
| 4 | `ZXP-DB-001` -> `ZXP-DB-002` -> `ZXP-DB-003` -> `ZXP-DB-004` -> `ZXP-DB-005` | 在契约字段和枚举稳定后固定 V1 migration；M2 不创建 Mapper、Repository 或领域代码。 |
| 5 | `ZXP-BE-SKEL-001` -> `ZXP-BE-SKEL-002` -> `ZXP-BE-SKEL-003` -> `ZXP-BE-SKEL-004` | 从 M3 开始创建后端工程，先保证模块、统一响应、配置和仓储端口可运行。 |
| 6 | `ZXP-BE-AUTH-001` -> `ZXP-BE-AUTH-002` -> `ZXP-BE-AUTH-003` -> `ZXP-BE-AUTH-004` | 先完成用户登录能力，再建立后端身份上下文、管理员权限和审计上下文。 |
| 7 | `ZXP-BE-MARKET-001` -> `ZXP-BE-MARKET-003` -> `ZXP-BE-MARKET-004` -> `ZXP-BE-MARKET-002` -> `ZXP-BE-MARKET-005` | 会场列表先跑通；折扣和标签完成后再让试算具备完整规则；最后补缓存治理。 |
| 8 | `ZXP-BE-TRADE-001` -> `ZXP-BE-TRADE-003` -> `ZXP-BE-TRADE-002` -> `ZXP-BE-TRADE-004` -> `ZXP-BE-TRADE-005` -> `ZXP-BE-TRADE-006` | 先固定状态机、价格快照和幂等语义，再落锁单、支付、退款和订单查询。 |
| 9 | `ZXP-BE-RUNTIME-001` -> `ZXP-BE-RUNTIME-002` -> `ZXP-BE-RUNTIME-003` -> `ZXP-BE-RUNTIME-004` -> `ZXP-BE-RUNTIME-005` | 交易闭环后补超时、过期退款、巡检重建、可靠事件执行器和可观测性。 |
| 10 | `ZXP-BE-ADMIN-001` -> `ZXP-BE-ADMIN-002` -> `ZXP-BE-ADMIN-003` -> `ZXP-BE-ADMIN-004` -> `ZXP-BE-ADMIN-005` | 后台 API 按活动、标签、订单、任务、配置治理补齐；所有写操作必须落审计。 |
| 11 | `ZXP-FE-USER-001` -> `ZXP-FE-USER-002` -> `ZXP-FE-USER-003` -> `ZXP-FE-USER-004` | M1 后可基于 Mock 并行启动；真实联调依赖认证、会场和交易 API。 |
| 12 | `ZXP-FE-ADMIN-001` -> `ZXP-FE-ADMIN-002` -> `ZXP-FE-ADMIN-003` -> `ZXP-FE-ADMIN-004` | M1 后可基于 Mock 并行启动；真实联调依赖后台治理 API。 |
| 13 | `ZXP-QA-001` -> `ZXP-QA-002` -> `ZXP-QA-003` -> `ZXP-QA-004` | 后端、前端主能力具备后补齐分层测试、并发一致性、前端质量和端到端验收。 |
| 14 | `ZXP-GIT-003` -> `ZXP-GIT-004` | 可运行的后端、前端和 OpenAPI lint 命令存在后建立 CI；验收稳定后补发布和回滚。 |

## 17. P1 含金量提升任务

P0 基础功能完成后，优先补以下亮点：

| 任务 | 价值 | 依赖 |
| --- | --- | --- |
| 统一可靠事件表替代分散任务表 | 提升最终一致设计完整度 | M6/M7 |
| Redis 运行态 version 机制 | 降低重建期间读写污染风险 | M7 |
| 延迟队列 + 定时扫描双关单 | 提升关单及时性和可靠性 | M7 |
| 缓存指标和后台可视化 | 展示生产化治理能力 | M5/M7/M10 |
| ArchUnit 依赖边界测试 | 展示架构纪律 | M3 |
| Testcontainers 集成测试 | 展示真实依赖下的验证能力 | M6/M7 |
| OpenAPI 生成前端类型 | 降低前后端字段漂移 | M1/M9/M10 |
| 灰度开关和降级策略 | 展示高并发保护意识 | M5/M6 |

## 18. 实现时的风险提醒

- 身份风险：用户端传来的 userId 只能作为兼容字段，不能作为交易归属依据。
- 金额风险：锁单必须重新试算，订单必须保存价格快照。
- 并发风险：Redis Lua 是前置裁决，MySQL 条件更新和唯一键是最终兜底。
- 幂等风险：锁单、支付、退款分别使用不同幂等键，不能混用。
- 任务风险：任务可以重复执行，所以 handler 必须幂等。
- 缓存风险：后台活动变更必须有版本失效或精确失效策略。
- 前端风险：管理端不要继续使用 `any`，否则后续接口变更很难发现。
- Git 风险：每个任务保持小提交，避免后续回滚时牵连多个业务域。
