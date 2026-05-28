# 契约对齐规范

## 1. 目标

本文用于把 `docs/plan/openapi.yaml`、前端类型、Mock 数据和 `deploy/migration/V1__init_schema.sql` 对齐，防止字段、枚举和状态在实现中漂移。

本文只记录 P0 必须遵守的轻量规则，不替代 OpenAPI、SDD 或 migration。新增接口、字段、枚举或表结构时，优先更新源文件，再更新本文中的映射。

## 2. 单一事实源

| 内容 | 单一事实源 | 使用方 |
| --- | --- | --- |
| API 路径、请求、响应、错误码、示例 | `docs/plan/openapi.yaml` | 后端 Controller/DTO、前端 API 类型、Mock 数据 |
| 表、字段、索引、唯一键 | `deploy/migration/V1__init_schema.sql` 和后续 migration | 后端持久化、Mapper、测试数据 |
| 任务执行和漂移控制 | `docs/sdd/tasks.md` | Agent、PR、任务验收 |
| 前端 Mock 规则 | `docs/sdd/mock/strategy.md` | 用户端、管理端 |

## 3. OpenAPI 验证

推荐使用 Redocly CLI 做 lint。当前仓库尚未固定 Node 工具链时，可以用 `npx` 临时执行；建立前端工程后，应把命令固化到 `package.json` 或 CI。

```powershell
npx --yes @redocly/cli lint docs/plan/openapi.yaml
```

最低通过标准：

- `paths` 中 P0 端点全部可被解析。
- 所有成功和失败响应使用 `ApiResponse<T>` 或同构信封。
- 分页响应使用 `PageResponse<T>` 形状。
- P0 端点至少有一个成功示例；高风险端点必须有代表性失败示例。
- 新增枚举值不得改变旧值语义。

如果本地无法联网安装 CLI，需要在任务总结中说明原因，并至少手工检查 YAML 结构、端点响应引用、示例字段和枚举值。

## 4. Mock 示例检查

Mock 数据必须从 OpenAPI 的 schema 和 examples 派生，或手工逐项校验一致。

每次新增或修改前端 Mock 时检查：

- Mock 返回形状与真实 API 客户端一致，不绕过 `ApiResponse<T>` 解包规则。
- Mock 字段名、枚举值、时间格式、金额单位与 OpenAPI 一致。
- Mock ID 稳定可读，例如 `ORD-MOCK-001`、`TEAM-MOCK-001`。
- 高风险场景至少覆盖 401、403、参数错误、重复幂等键、非法状态和资源不存在。
- Mock 切换到真实 HTTP 只改数据源或环境变量，不改页面业务逻辑。

建议在前端工程建立后补充命令：

```powershell
npm run type-check
npm run build
```

如果使用 Mock fixture 文件，可增加轻量脚本校验字段名和枚举值；不要为了校验引入复杂 Mock 服务框架。

## 5. 前端类型规则

优先路线：

1. P0 初期允许手写类型，但类型名、字段名、枚举值必须与 `openapi.yaml` 保持一致。
2. 前端骨架稳定后，优先用 OpenAPI 生成类型，生成物放在明确目录，例如 `src/api/generated`。
3. 生成物不得手工修改；需要适配时在 `src/api` 包装层处理。
4. 用户端和管理端共享同一套枚举值，不各自写魔法字符串。
5. 任何 API 字段变更必须同步更新 OpenAPI、前端类型、Mock 数据和后端 DTO。

手写类型最低要求：

- `ApiResponse<T>`、分页结构、状态枚举集中定义。
- 订单、队伍、任务、活动、标签、退款来源等状态只能从集中枚举导出。
- 不新增隐式 `any`。
- 前端不把 `userId` 作为交易归属依据，归属以后端登录态为准。

## 6. 枚举和状态映射

| OpenAPI schema | 允许值 | 数据库字段 | 说明 |
| --- | --- | --- | --- |
| `UserRole` | `USER`、`ADMIN` | `user_account.role` | 管理端路由和后台 API 权限以该角色为准。 |
| `UserStatus` | `ENABLED`、`DISABLED` | `user_account.status` | 禁用账号不能登录。 |
| `ActivityStatus` | `DRAFT`、`ACTIVE`、`PAUSED`、`ENDED` | `activity.status` | 会场、试算、锁单都必须校验活动状态。 |
| `OrderStatus` | `WAIT_PAY`、`PAID`、`REFUNDED`、`CLOSED_TIMEOUT` | `trade_order.order_status` | 订单状态机只允许文档定义的前置状态流转。 |
| `TeamStatus` | `PROGRESS`、`COMPLETE`、`EXPIRED_UNFORMED`、`COMPLETE_AFTER_REFUND` | `team.status` | 队伍人数和退款状态推进必须和订单事务一致。 |
| `TaskStatus` | `INIT`、`PROCESSING`、`SUCCESS`、`FAILED`、`RETRY_WAIT` | `reliable_event.status`、`crowd_tag_job.status` | 可靠事件和标签任务共用任务状态语义。 |
| `RefundSource` | `USER`、`ADMIN`、`AUTO_EXPIRED` | `refund_record.refund_source` | 管理员退款必须写原因和操作者，自动退款由任务产生。 |
| `EventType` | `TEAM_COMPLETE_NOTIFY`、`REDIS_SLOT_RELEASE`、`TEAM_REBUILD`、`ORDER_TIMEOUT_REPAIR`、`REFUND_REPAIR` | `reliable_event.event_type` | 事件幂等键为 `event_type + biz_key`。 |
| `TagJobType` | `USERS`、`PARTICIPATE_COUNT` | `crowd_tag_job.rule_type` | 标签规则表达式保存在 `rule_expr` JSON 中。 |
| 绑定状态 | `ENABLED`、`DISABLED` | `sku.status`、`activity_sku.status`、`discount.status` | 与用户账号状态同值但语义不同，代码中应使用不同枚举或明确命名。 |
| 支付/退款记录状态 | `SUCCESS`、`FAILED` | `pay_record.status`、`refund_record.status`、`admin_operation_log.result` | 记录结果状态，不等同于订单状态。 |

实现规则：

- 后端枚举值必须与 OpenAPI 字符串完全一致。
- 数据库只保存稳定枚举值，不保存前端展示文案。
- 如果新增状态，必须同时更新 OpenAPI、migration 或下一版 migration、前端枚举、Mock 数据和状态机测试。

## 7. 关键字段映射

| OpenAPI 字段 | 数据库字段 | 约束 |
| --- | --- | --- |
| `userId` | `user_account.id`、`trade_order.user_account_id`、`pay_record.user_account_id`、`refund_record.user_account_id` | 用户端请求中的 `userId` 不可信；交易归属以后端登录态为准。 |
| `activityId` | `activity.activity_id`、`activity_sku.activity_id`、`discount.activity_id`、`team.activity_id`、`trade_order.activity_id` | 活动变更必须通过 `activity.version` 或精确 key 影响缓存。 |
| `skuId` | `sku.sku_id`、`activity_sku.sku_id`、`team.sku_id`、`trade_order.sku_id` | 会场列表和锁单必须校验 SKU 与活动绑定。 |
| `teamId` | `team.team_id`、`trade_order.team_id` | 参团时可传；开团由后端生成。 |
| `orderId` | `trade_order.order_id`、`pay_record.order_id`、`refund_record.order_id` | 订单详情必须做归属或管理员权限校验。 |
| `clientOrderNo` | `trade_order.client_order_no` | 与 `user_account_id` 组成唯一键，锁单幂等。 |
| `payNo` | `pay_record.pay_no` | 唯一键，Mock 支付幂等。 |
| `refundNo` | `refund_record.refund_no` | 唯一键，用户退款、管理员退款和自动退款都必须幂等。 |
| `originalPriceCent` | `trade_order.original_price_cent`、`sku.original_price_cent` | 订单保存价格快照，不能依赖活动后续变化。 |
| `discountCent` | `trade_order.discount_cent`、`discount.discount_value_cent` | 锁单前后端重新试算，前端金额不可信。 |
| `payPriceCent` / `amountCent` | `trade_order.pay_price_cent`、`pay_record.amount_cent`、`refund_record.amount_cent` | 金额单位统一为分。 |
| `activitySnapshot` | `trade_order.activity_snapshot` | JSON 快照，用于历史订单解释。 |
| `discountSnapshot` | `trade_order.discount_snapshot` | JSON 快照，用于历史订单解释。 |
| `trialNo`、`trialTime` | `trade_order.trial_no`、`trade_order.trial_time` | 锁单时保存试算证据。 |
| `eventId` | `reliable_event.event_id` | 事件业务 ID，单独唯一。 |
| `eventType`、`bizKey` | `reliable_event.event_type`、`reliable_event.biz_key` | 组成可靠事件幂等唯一键。 |
| `payload` | `reliable_event.payload` | 必须是结构化 JSON，不拼接字符串。 |
| `operatorId`、`operatorName` | `admin_operation_log.operator_id`、`admin_operation_log.operator_name` | 后台写操作必须记录。 |
| `traceId` | `admin_operation_log.trace_id`，API 响应信封字段 | 用于联动接口响应、日志和审计。 |

## 8. 变更检查清单

影响 API、数据库或前端类型的任务提交前必须检查：

- OpenAPI 是否更新路径、schema、枚举、错误码和 examples。
- migration 是否已有字段、唯一键、索引和状态字段；已合并 migration 不直接修改，只新增下一版。
- 本文枚举和关键字段映射是否仍然准确。
- 前端类型和 Mock 数据是否同步。
- 后端 DTO、领域枚举、Mapper 字段是否与 OpenAPI 和 migration 一致。
- 高风险变更是否补充测试或手工验收记录。

PR 或任务总结中建议增加：

```text
契约对齐检查：
- OpenAPI lint：已执行/暂未执行，原因：...
- Mock 示例：已检查/不适用，覆盖：...
- 前端类型：已生成/已手写对齐/不适用，位置：...
- migration 映射：已检查/不适用，涉及字段：...
```
