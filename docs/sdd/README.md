# SDD 索引

本目录保存新项目重构所需的可执行软件设计文档。每份 SDD 都要说明要构建什么、要查看哪些旧项目 Harness 文件、编码前必须具备哪些契约，以及如何验证结果。

如果不确定某个任务应该先读哪份文档，先回到 `docs/README.md` 查看文档地图和开发阅读流。

## SDD 文档

| 文档 | 范围 | 主要任务 |
| --- | --- | --- |
| `backend/spec.md` | 后端模块、契约、migration、领域规则、运行态治理 | `ZXP-CONTRACT-*`、`ZXP-DB-*`、`ZXP-BE-*`、`ZXP-QA-001`、`ZXP-QA-002` |
| `frontend-user/spec.md` | 用户端 Web、登录、会场、结算、支付、订单和退款流程 | `ZXP-FE-USER-*`、用户端相关 OpenAPI 任务 |
| `frontend-admin/spec.md` | 管理端 Web、活动/标签/订单/任务/DCC/线程池治理 | `ZXP-FE-ADMIN-*`、管理端 OpenAPI 任务 |
| `mock/strategy.md` | Mock 数据、本地调试接口、契约示例和并行开发策略 | `ZXP-CONTRACT-*`、前端壳层任务、联调任务 |
| `tasks.md` | 跨 Agent 执行顺序、依赖门禁和完成标准 | 全部任务 |

## SDD 规则

- SDD 是新项目实现决策的依据。
- Harness 只作为旧行为证据来源。
- OpenAPI 是前后端共享契约。
- OpenAPI、Mock、前端类型和 migration 状态字段的轻量对齐规则见 `docs/plan/08-contract-alignment.md`。
- 契约、状态、错误码和验证方式不明确的任务，不能进入实现。
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

同时满足以下条件时，P0 才算完成：

- OpenAPI 覆盖认证、会场、交易、订单和后台治理端点。
- 后端可以本地启动，并且后端测试通过。
- 用户端可以完成登录 -> 会场 -> 试算 -> 锁单 -> Mock 支付 -> 订单 -> 退款。
- 管理端可以完成活动、标签、订单、任务、DCC、线程池、审计和仪表盘治理操作。
- `docs/plan/05-validation.md` 中的高风险场景有自动化测试或手工验收证据。
- P0 tag 具备发布说明和回滚说明。

P1 只承接后续含金量优化、工程增强和生产化提升，不承接 P0 基础功能缺口。
