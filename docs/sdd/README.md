# SDD 索引

本目录保存新项目重构所需的可执行软件设计文档。每份 SDD 都要说明要构建什么、要查看哪些旧项目 Harness 文件、编码前必须具备哪些契约，以及如何验证结果。任务编号、任务范围和完整执行顺序只维护在 `docs/plan/06-task-implementation-checklist.md`，本目录不重复定义任务事实源。

如果不确定某个任务应该先读哪份文档，先回到 `docs/README.md` 查看文档地图和开发阅读流。

## SDD 文档

| 文档 | 范围 | 对应任务来源 |
| --- | --- | --- |
| `backend/spec.md` | M3 后端模块、横切契约、领域规则、运行态治理和持久化实现边界 | 以任务清单中的后端、数据库、契约和 QA 任务为准。 |
| `frontend-user/spec.md` | 用户端 Web、登录、会场、结算、支付、订单和退款流程 | 以任务清单中的用户端任务为准。 |
| `frontend-admin/spec.md` | 管理端 Web、活动/标签/订单/任务/DCC/线程池治理 | 以任务清单中的管理端任务为准。 |
| `mock/strategy.md` | Mock 数据、本地调试接口、契约示例和并行开发策略 | 以任务清单中的契约、前端和联调任务为准。 |
| `tasks.md` | 跨 Agent 门禁、依赖和交接规则 | 不维护任务清单，只引用任务事实源。 |

## SDD 规则

- SDD 是新项目实现决策的依据。
- Harness 只作为旧行为证据来源。
- OpenAPI 是前后端共享契约。
- migration 是表结构、唯一键、状态持久化和 MySQL 交易事实源。
- `docs/plan/06-task-implementation-checklist.md` 是唯一任务事实源。
- `docs/plan/04-roadmap.md` 只维护阶段、里程碑和退出标准。
- `docs/sdd/tasks.md` 只维护任务门禁、阶段依赖和交接规则。
- OpenAPI、Mock、前端类型和 migration 状态字段的轻量对齐规则见 `docs/plan/08-contract-alignment.md`。
- 契约、状态、错误码和验证方式不明确的任务，不能进入实现。
- M1/M2 不落地后端 Java 代码；M3 才开始创建后端工程骨架。
- 任何影响 API、数据库或状态机的 SDD 变更，都必须同步更新相关规划文档或任务说明。

## Agent 交接格式

把任务交给 Agent 时使用以下格式：

```text
任务编号：ZXP-...
SDD：docs/sdd/.../spec.md
Harness 参考：docs/harness/reference-map.md#...
契约文件：docs/plan/openapi.yaml
契约对齐：docs/plan/08-contract-alignment.md
实现目标：...
验证方式：...
建议提交：...
```

## P0 完成定义

P0 完成定义以 `docs/plan/04-roadmap.md` 为准，完整任务顺序以 `docs/plan/06-task-implementation-checklist.md#16-p0-项目推进顺序` 为准。SDD 只负责说明对应工作包如何构建和验证，不单独维护一份 P0 清单。
