# SDD 任务门禁与依赖规则

本文只定义任务开工门禁、跨阶段依赖、Agent 分工和交接规则。`docs/plan/06-task-implementation-checklist.md` 是唯一任务事实源，负责维护任务编号、任务范围、验收标准和完整执行顺序；`docs/plan/04-roadmap.md` 只负责阶段视角、里程碑和退出标准。

P0 覆盖 `docs/plan/06-task-implementation-checklist.md` 中定义的完整项目基础功能，包括用户端、后端、管理端、运行态治理、验证和发布证据。P1 只承接后续含金量优化、工程增强和生产化提升，不承接 P0 基础功能缺口。

阶段视角见 `docs/plan/04-roadmap.md`；完整任务顺序见 `docs/plan/06-task-implementation-checklist.md#16-p0-项目推进顺序`；文档阅读流和各文档职责见 `docs/README.md`。

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

## P0 依赖门禁

本节只说明“哪些条件解锁下一类工作”，不重复定义具体任务。任务编号、任务说明和建议提交统一维护在 `docs/plan/06-task-implementation-checklist.md`。

### M0 文档治理

退出标准：

- README、文档地图、路线图、任务清单、SDD、Harness 和开发规范职责清楚。
- `docs/plan/06-task-implementation-checklist.md` 已能作为唯一任务事实源使用。
- 后续文档只保留摘要和链接，不重新维护任务清单。

### M1 契约与模型

依赖：M0 文档治理完成。

退出标准：

- OpenAPI 覆盖 P0 用户端、管理端和运行态治理端点。
- 成功和代表性失败示例足以支撑前端 Mock。
- 状态枚举、错误码和字段映射可以被前端、Mock、后端 DTO 和 migration 复用。
- 不创建后端 Java DTO、Controller、Service、Mapper 或 Repository。

收口状态：

- M1 已验证并可作为后续阶段输入；OpenAPI lint 已确认 API description valid。
- OpenAPI 已覆盖 P0 用户端、管理端和运行态治理端点。
- 统一响应固定为 `code/message/data/traceId`，分页固定为 `pageNo/pageSize/total/items`。
- 状态枚举和错误码已定义，并与契约对齐文档中的映射保持一致。
- lint 剩余 `localhost` 本地服务地址和健康检查缺少 4XX 响应两个可接受 warning，不阻塞 M2/M9/M10。
- M2 已完成上述风险收口：`deploy/migration/V1__init_schema.sql` 已纳入 Git，标签任务字段、活动 SKU 绑定来源渠道、审计 `traceId`、试算时间字段映射、可靠事件执行时间和种子数据已完成验证。

### M2 数据库与 migration

依赖：M1 的关键 schema、枚举和状态字段稳定；M1 收口后可继续做 migration 验证。

退出标准：

- V1 migration 能表达 P0 交易事实、可靠事件、标签、DCC 和审计表结构。
- 唯一键、索引和状态字段与 OpenAPI 关键 schema 对齐。
- MySQL 作为交易事实源的兜底边界已经落在 migration 中。
- 不创建 Mapper、Repository、领域模型或业务 handler。

收口状态：

- M2 已验证并可作为 M3 输入；`deploy/migration/V1__init_schema.sql` 已纳入 Git。
- V1 migration 已在云服务器 Docker MySQL 8.4 临时空库中执行成功，验证后临时库已删除。
- 关键唯一键和索引已覆盖入口绑定、锁单幂等、占位防重复、限购检查、可靠事件幂等、后台审计追踪和运行态查询。
- 种子数据已覆盖管理员、普通用户、3 个 SKU、2 个活动、2 类折扣、标签主表/任务/明细和 DCC 配置。
- 本阶段未创建 Mapper、Repository、领域模型、业务 handler 或后端 Java 模块。

### M3 后端骨架

依赖：M1/M2 提供可落地的契约和数据事实源。

退出标准：

- 后端 Maven 多模块、统一响应、异常处理、profile、MyBatis、Redis 和健康检查可启动。
- 模块依赖方向清晰，domain 不依赖 Web、MyBatis、Redis 或 Controller DTO。
- 空骨架下测试命令可运行。

### M4 到 M8 后端领域能力

依赖：M3 后端骨架完成；对应 API 和 migration 门禁满足。

依赖关系：

- 认证和登录态先落地，后续交易、订单和后台接口都以后端身份上下文为准。
- 会场、折扣、标签和缓存先支撑试算，交易锁单必须复用后端试算。
- 交易闭环先保证状态机、幂等、价格快照、支付、退款和订单归属。
- 运行态治理在交易闭环后补齐超时关单、队伍过期、巡检重建、可靠事件和可观测性。
- 后台 API 按已具备的业务能力开放治理入口，写操作必须具备审计。

退出标准：

- 用户端 API 可以本地调用。
- 管理端可以完成活动、标签、订单、任务、DCC、线程池和审计治理。
- 高风险 API 具备幂等、归属、状态流转和补偿验证。

### M9 到 M10 前端并行开发

依赖：M1 契约和 Mock 示例完成后可以启动壳层和 Mock 页面；M1 收口后 M9/M10 可基于 Mock 并行启动，真实联调依赖对应后端能力完成。

退出标准：

- 两个前端类型检查和构建通过。
- 用户端流程可以连接本地后端运行。
- 管理端治理流程可以连接本地后端运行。

### M11 到 M12 QA、CI 与发布证据

依赖：后端、用户端和管理端 P0 能力完成；验证命令具备可运行入口。

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
