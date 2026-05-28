# SDD 任务执行计划

本文把路线图转成可执行的 Agent 工作包。它不替代 `docs/plan/06-task-implementation-checklist.md`，只补充依赖门禁、分工和交接规则。

P0 覆盖 `docs/plan/06-task-implementation-checklist.md` 中定义的完整项目基础功能，包括用户端、后端、管理端、运行态治理、验证和发布证据。P1 只承接后续含金量优化、工程增强和生产化提升，不承接 P0 基础功能缺口。

阶段视角见 `docs/plan/04-roadmap.md`；文档阅读流和各文档职责见 `docs/README.md`。

阶段边界必须保持一致：M1 只做契约和 Mock 示例，M2 只做 migration 和数据事实源，M3 才创建后端 Maven 工程和基础代码。任何任务如果需要提前创建 Java 后端模块、Mapper、Repository、Controller 或 Service，都必须重新评估是否已经越过当前阶段边界。

## 任务就绪门禁

只有满足所有适用门禁后，任务才可以进入实现：

| 门禁 | 要求 |
| --- | --- |
| 契约 | OpenAPI 路径、请求、响应、错误码和示例已定义，或明确说明该任务不需要契约；M1 不落地后端 Java DTO。 |
| 数据 | 所需表、唯一键和状态字段已定义；M2 不落地 Mapper、Repository 或领域代码。 |
| Harness | 旧项目参考文件已列入 `docs/harness/reference-map.md`。 |
| SDD | 后端、前端或 Mock SDD 已描述对应行为。 |
| 契约对齐 | 已按 `docs/plan/08-contract-alignment.md` 检查 OpenAPI lint、Mock 示例、前端类型和 migration 字段映射。 |
| 开发规范 | 已确认 `docs/plan/07-development-standard.md` 对当前任务的要求；后端任务必须遵守阿里巴巴 Java 开发手册并规划必要注释。 |
| 编码格式 | 新增和修改文件必须符合 UTF-8、LF、缩进、末尾换行和 `.editorconfig` 规则。 |
| 验证 | 测试命令、Mock 检查或手工验收路径已明确。 |
| 范围 | 任务可以落在一个小提交或一个小 PR 内。 |

## 推荐 Agent 分工

| Agent | 负责内容 | 必须协同的事项 |
| --- | --- | --- |
| 契约 Agent | OpenAPI、共享 schema、状态枚举、Mock 示例 | 所有前后端任务。 |
| 数据 Agent | migration、表结构、唯一键、索引、状态字段和种子数据约束 | OpenAPI 枚举、关键字段映射。 |
| 后端 Agent | Maven 模块、domain/application/infrastructure/trigger、测试 | 契约变更、migration 字段。 |
| 用户端 Agent | `frontend/user-web`、用户端 Mock 数据、用户流程验证 | 会场、交易、订单 schema。 |
| 管理端 Agent | `frontend/admin-web`、管理端 Mock 数据、后台流程验证 | 后台 schema、审计字段。 |
| QA Agent | 验证矩阵、端到端验收清单或脚本、CI 门禁 | 高风险用例和发布证据。 |

## P0 依赖顺序

### 阶段 1：契约与 Harness 基线

0. `ZXP-DOC-004`：建立项目开发规范、阿里巴巴 Java 开发手册约束和注释要求。
1. `ZXP-CONTRACT-001`：统一响应、分页、错误响应 schema。
2. `ZXP-CONTRACT-002`：认证 API。
3. `ZXP-CONTRACT-003`：会场/试算 API。
4. `ZXP-CONTRACT-004`：交易、订单、支付、退款 API。
5. `ZXP-CONTRACT-005`：后台治理 API，覆盖活动、标签、订单、任务、DCC、线程池和审计查询。
6. `ZXP-CONTRACT-006`：状态枚举和错误码分段。

退出标准：

- OpenAPI lint 可以运行。
- 用户端流程和后台治理流程具备 Mock 示例。
- 前端可以基于 OpenAPI 手写或生成类型。
- 不创建 `backend/zhongxiangpin-*` Java 模块，不提交 DTO、Controller 或 Service 实现。

### 阶段 2：数据库与 migration

1. `ZXP-DB-001` 到 `ZXP-DB-005`，V1 migration 支撑用户、商品、活动、交易、可靠事件、标签和审计等基础表结构。

退出标准：

- migration 可以在空库执行，或至少具备契约测试。
- 数据字段、唯一键和状态枚举与 OpenAPI 关键 schema 对齐。
- MySQL 作为交易事实源的字段、唯一键和条件更新边界已经体现在 migration 中。
- 不提交 Mapper、Repository、领域模型或业务 handler。

### 阶段 3：后端骨架

1. `ZXP-BE-SKEL-001` 到 `ZXP-BE-SKEL-004`。

退出标准：

- 后端可以本地启动。
- 空骨架下 `mvn test` 通过。
- 健康检查、统一响应、异常处理和模块依赖方向清晰。

### 阶段 4：后端 P0 领域

1. `ZXP-BE-AUTH-001`、`ZXP-BE-AUTH-003`、`ZXP-BE-AUTH-004`。
2. `ZXP-BE-MARKET-001` 到 `ZXP-BE-MARKET-003`。
3. `ZXP-BE-TRADE-001` 到 `ZXP-BE-TRADE-006`。
4. `ZXP-BE-RUNTIME-001`、`ZXP-BE-RUNTIME-004`。
5. `ZXP-BE-ADMIN-001` 到 `ZXP-BE-ADMIN-005`。

退出标准：

- 用户端 API 可以本地调用。
- 管理端可以完成活动、标签、订单、任务、DCC、线程池和审计治理。
- 高风险 API 具备幂等和归属校验测试。

### 阶段 5：前端并行开发

阶段 1 完成后即可基于 Mock 数据启动：

1. `ZXP-FE-USER-001` 到 `ZXP-FE-USER-004`。
2. `ZXP-FE-ADMIN-001` 到 `ZXP-FE-ADMIN-004`。

后端 API 可用后继续：

- 把 Mock 调用替换或切换为真实 HTTP。
- 验证完整用户端和管理端流程。

退出标准：

- 两个前端类型检查和构建通过。
- 用户端流程可以连接本地后端运行。
- 管理端治理流程可以连接本地后端运行。

### 阶段 6：QA 与发布证据

1. `ZXP-QA-001` 到 `ZXP-QA-004`。
2. `ZXP-GIT-003`、`ZXP-GIT-004`。

退出标准：

- 后端测试通过。
- 前端构建通过。
- OpenAPI lint 通过。
- 端到端验收记录存在。
- 发布说明和回滚说明存在。

## PR / 提交检查清单

每个任务提交或 PR 应包含：

- 任务编号。
- Harness 证据块。
- 开发规范检查：阿里巴巴 Java 开发手册适用情况、注释完整性、编码格式、静态扫描或替代验证结果。
- 契约对齐检查：OpenAPI lint、Mock 示例、前端类型和 migration 映射检查结果。
- 契约和数据变更摘要。
- 验证结果。
- 与 SDD 的已知偏差。
- 后续任务，如有。

## 漂移控制

如果实现需要偏离 SDD：

1. 先更新 SDD，或在同一提交中更新。
2. 如果前后端契约变化，同步更新 OpenAPI。
3. 如果数据库事实源变化，同步更新 migration 和契约对齐映射。
4. 如果任务范围变化，同步更新任务清单说明。
5. 记录为什么没有沿用旧 Harness 行为。

## 可执行 Harness + SDD 的定义

项目达到可执行 Harness + SDD 状态，需要满足：

- `docs/harness/README.md` 和 `reference-map.md` 明确告诉 Agent 去旧项目看哪里。
- `docs/sdd/*/spec.md` 明确告诉 Agent 在新项目构建什么。
- `docs/sdd/mock/strategy.md` 明确告诉 Agent 在接口未完成前如何工作。
- `docs/plan/openapi.yaml` 具备足够端点细节，可以解锁前后端并行开发。
- 每个任务都有验证路径。
