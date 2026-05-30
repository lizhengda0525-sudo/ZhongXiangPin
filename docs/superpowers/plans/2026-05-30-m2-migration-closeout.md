# M2 数据库与 Migration 收口修订计划

本文记录 M2 收口前必须完成的修改点、执行顺序和验收方式。当前阶段只落文档计划，不修改 `deploy/migration/V1__init_schema.sql`、不修改 `docs/plan/openapi.yaml`，也不创建 Mapper、Repository、领域代码或后端 Java 模块。

## 1. 目标与边界

目标：把 M2 需要补齐的数据库事实源、唯一键、索引、种子数据和契约映射修订点全部前置记录，确保下一阶段执行时可以逐项验证并收口。

阶段边界：

- M2 只允许修改 migration、种子数据约束、字段映射和收口记录。
- M2 不创建后端 Maven 模块，不创建 Mapper、Repository、Controller、Service 或领域代码。
- 未经明确授权，不修改 OpenAPI 契约。`tagRule` 与 `rule_expr` 的差异先在契约对齐文档中记录归一化策略。
- 旧项目只作为只读 Harness 证据来源，不复制旧项目源码和命名细节。

## 2. 事实源与复核结论

本计划来自三路只读复核结论：

| 复核视角 | 结论 | 对 M2 的影响 |
| --- | --- | --- |
| 契约映射复核 | OpenAPI、`docs/plan/08-contract-alignment.md` 与 migration 存在字段漂移。 | 下一阶段必须先修 migration，再同步契约映射。 |
| Harness 行为复核 | 旧项目渠道绑定、标签任务、支付运行态和可靠事件依赖的事实字段未完全进入新 migration。 | 必须补 `source/channel`、标签批次唯一键、执行时间和补偿审计字段。 |
| 验证方案复核 | 当前 migration 可作为初稿，但缺少种子数据和部分唯一键/索引验证点。 | M2 收口前必须完成空库执行、唯一键冲突和种子数据检查。 |

当前状态判断：M2 不能标记为收口。阻塞点集中在 `deploy/migration/V1__init_schema.sql` 与 `docs/plan/08-contract-alignment.md`，不是后端代码实现问题。

## 3. 总体执行顺序

1. 修订 `deploy/migration/V1__init_schema.sql` 的表字段、唯一键、索引和种子数据。
2. 同步 `docs/plan/08-contract-alignment.md` 的字段映射、状态说明和契约归一化策略。
3. 执行静态检查：字段名、唯一键、索引、种子数据插入语句和文档映射逐项核对。
4. 如果本地 MySQL 可用，执行空库 migration 验证和唯一键冲突验证。
5. 验证通过后，更新 `docs/plan/06-task-implementation-checklist.md` 的 M2 收口记录。
6. 只有 M2 已验证可作为 M3 输入后，才更新 `README.md` 的当前阶段说明。

## 4. 修改点总表

| 编号 | 修改对象 | 当前问题 | 完善计划 | 验收方式 | 对应任务 |
| --- | --- | --- | --- | --- | --- |
| M2-FIX-001 | `activity_sku` | 当前只表达 `activity_id + sku_id`，无法表达 OpenAPI 中 `source + channel + skuId -> activityId` 的绑定语义。 | 增加 `source`、`channel`；唯一键调整为 `source/channel/sku_id` 维度；补充按渠道、商品和状态查询的索引。 | 相同 `source/channel/sku_id` 不能重复绑定；不同渠道可绑定同一 SKU。 | `ZXP-DB-002` |
| M2-FIX-002 | `activity` | OpenAPI 有 `tagScope` 语义，migration 只保存 `tag_id`，缺少标签适用范围。 | 增加 `tag_scope`，用于保存标签活动的命中范围或适用策略。 | 标签活动种子数据能同时表达 `tag_id` 和 `tag_scope`。 | `ZXP-DB-002` |
| M2-FIX-003 | `team` | 队伍表缺少来源渠道快照，后续运行态治理无法按渠道隔离。 | 增加 `source`、`channel`；补充活动、渠道、状态、过期时间维度索引。 | 队伍查询和运行态扫描可以按 `activity_id/source/channel/status` 定位。 | `ZXP-DB-003` |
| M2-FIX-004 | `trade_order` | 订单缺少来源渠道快照，支付、退款、补偿和订单查询无法保留交易发生时的渠道事实。 | 增加 `source`、`channel`；补充限购检查索引 `user_account_id/activity_id/sku_id/order_status`；补充后台和运行态查询索引 `order_status/created_at`。 | 用户限购、后台订单列表、超时扫描都能命中明确索引。 | `ZXP-DB-003` |
| M2-FIX-005 | `crowd_tag` | 标签主表缺少描述字段，后台列表和审计展示信息不足。 | 增加 `tag_desc`。 | 标签种子数据可展示名称和描述。 | `ZXP-DB-005` |
| M2-FIX-006 | `crowd_tag_job` | `batch_id` 可空，但 OpenAPI 路径使用 `{tagId}/{batchId}`；缺少统计窗口和执行时间；唯一键不利于同标签多批次管理。 | 将 `batch_id` 改为必填；增加 `stat_start_time`、`stat_end_time`、`execute_time`；增加唯一键 `tag_id/batch_id`；保留 `tag_job_id` 作为业务 ID。 | 同一标签同一批次不能重复；不同批次可共存；任务可展示统计窗口和执行时间。 | `ZXP-DB-005` |
| M2-FIX-007 | `crowd_tag_job.rule_expr` | OpenAPI `tagRule` 是字符串，migration `rule_expr` 是 JSON，当前缺少归一化说明。 | 不直接改 OpenAPI；在 `08-contract-alignment.md` 说明 API 入参/展示使用 `tagRule` 字符串，入库前归一化为 `rule_expr` JSON。后续如需把 OpenAPI 改为结构化对象，单独授权。 | 契约对齐文档清楚说明 `tagRule -> rule_expr` 的转换责任和边界。 | `ZXP-DB-005` |
| M2-FIX-008 | `admin_operation_log` | `trace_id` 当前可空，但统一响应契约要求 `traceId` 必填并贯穿审计。 | 将 `trace_id` 改为 `NOT NULL`；增加 `trace_id` 查询索引。 | 每条后台写操作审计记录都能按 `traceId` 追踪。 | `ZXP-DB-005` |
| M2-FIX-009 | `reliable_event` | 缺少最近执行时间，补偿任务审计和后台排查不完整。 | 增加 `last_execute_time`；补充 `event_type/status/created_at` 查询索引；保留 `next_execute_time` 作为调度字段。 | 后台任务列表可展示下次执行和最近执行；失败事件可按类型和状态筛选。 | `ZXP-DB-004` |
| M2-FIX-010 | `reliable_event.next_execute_time` | OpenAPI 示例允许 `nextExecuteTime` 为空，migration 当前 `NOT NULL`。 | 默认保持 DB 更严格，要求待执行事件必须有下次执行时间；如果终态事件需要为空，需单独授权调整 OpenAPI 或 migration。 | 契约对齐文档说明 DB 严格约束和 API 展示语义的差异。 | `ZXP-DB-004` |
| M2-FIX-011 | 种子数据 | 当前 migration 没有 `INSERT`，无法支撑 M2 空库验收和后续 Mock 对齐。 | 增加基础种子数据：1 个管理员、1 个普通用户、至少 3 个商品、至少 2 类折扣、至少 1 个标签活动、标签主表/任务/明细、基础 DCC 配置。 | 空库执行后可用 SQL 查询验证每类种子数据数量和关键字段。 | `ZXP-DB-001` 到 `ZXP-DB-005` |
| M2-FIX-012 | `docs/plan/08-contract-alignment.md` | 字段映射仍有 `trialTime` 等旧命名，未完整覆盖来源渠道、标签任务和审计约束。 | 更新字段映射：`source/channel`、`calculatedAt -> trade_order.trial_time`、`traceId -> admin_operation_log.trace_id NOT NULL`、标签任务统计字段、可靠事件执行字段。 | 映射文档能逐项解释 OpenAPI 字段到数据库字段的落点。 | 契约对齐 |
| M2-FIX-013 | `docs/plan/06-task-implementation-checklist.md` | M2 任务仍是初始任务卡，缺少本轮审计后的收口阻塞记录。 | M2 验证通过后补充 M2 收口记录；验证前只保留本计划入口，不提前标记完成。 | 清单中能区分“待修计划已落文档”和“M2 已收口”。 | 收口记录 |
| M2-FIX-014 | `README.md` | 当前阶段仍停留在 M1 已完成、M2 待开始。 | 只有 M2 验证通过后，才更新当前阶段为 M2 已验证并可作为 M3 输入。 | README 不提前声明未完成里程碑。 | 收口记录 |

## 5. 分任务执行计划

### 5.1 ZXP-DB-001 用户与权限表

计划修改：

- 保持 `user_account.username`、`phone` 唯一。
- 检查密码字段只保存 bcrypt hash，不保存明文密码、验证码或 token。
- 增加 1 个管理员和 1 个普通用户种子数据。

验收：

- 空库执行后存在 1 个 `ADMIN`、1 个 `USER`。
- `username` 和 `phone` 唯一键冲突可复现。

### 5.2 ZXP-DB-002 商品、活动、折扣与活动 SKU

计划修改：

- `activity` 增加 `tag_scope`。
- `activity_sku` 增加 `source`、`channel`。
- `activity_sku` 唯一键按 `source/channel/sku_id` 约束，不再只用 `activity_id/sku_id` 表达入口绑定。
- 增加至少 3 个商品、2 类折扣、1 个带标签活动的种子数据。
- 保留 `activity.version`，作为后续缓存失效和活动变更的版本事实。

验收：

- `source + channel + sku_id` 能唯一定位 `activity_id`。
- 同一个 SKU 可以在不同渠道绑定不同活动。
- 标签活动可以表达 `tag_id` 和 `tag_scope`。

### 5.3 ZXP-DB-003 队伍、订单、支付与退款

计划修改：

- `team` 增加 `source`、`channel`。
- `trade_order` 增加 `source`、`channel`。
- `trade_order` 增加限购检查索引：`user_account_id/activity_id/sku_id/order_status`。
- `trade_order` 增加后台和运行态查询索引：`order_status/created_at`。
- 继续保留 `user_account_id/client_order_no`、`team_id/occupy_no`、`pay_no`、`refund_no` 等幂等和占位唯一键。

验收：

- 锁单幂等、防重复占位、限购检查都能通过唯一键或索引兜底。
- 订单保存价格快照、活动快照、折扣快照和试算时间。
- 渠道字段成为订单和队伍的交易快照，不依赖活动后续变化。

### 5.4 ZXP-DB-004 可靠事件、通知和补偿

计划修改：

- `reliable_event` 增加 `last_execute_time`。
- 增加 `event_type/status/created_at` 索引，用于后台列表和运行态巡检。
- 明确 `next_execute_time` 在 DB 中保持必填，终态展示是否为空由后续 API 层处理或另行授权调整。

验收：

- 可靠事件可以按 `event_type + biz_key` 幂等。
- 后台可按类型、状态和时间筛选事件。
- 每次执行可以记录最近执行时间、失败原因和重试次数。

### 5.5 ZXP-DB-005 标签和审计

计划修改：

- `crowd_tag` 增加 `tag_desc`。
- `crowd_tag_job.batch_id` 改为必填。
- `crowd_tag_job` 增加 `stat_start_time`、`stat_end_time`、`execute_time`。
- `crowd_tag_job` 增加唯一键 `tag_id/batch_id`。
- `admin_operation_log.trace_id` 改为必填，并增加 `trace_id` 索引。
- 增加标签主表、标签任务、标签明细和审计可验证种子数据。

验收：

- 同标签多批次不混淆。
- 标签任务能展示规则、统计窗口、执行时间、统计结果和失败原因。
- 后台审计能通过 `traceId`、操作者和目标对象查询。

## 6. 契约对齐计划

下一阶段修改 `docs/plan/08-contract-alignment.md` 时，至少补齐以下映射：

| OpenAPI 或业务字段 | 数据库落点 | 计划说明 |
| --- | --- | --- |
| `source`、`channel` | `activity.source/channel`、`activity_sku.source/channel`、`team.source/channel`、`trade_order.source/channel` | `activity_sku` 表达入口绑定，`team` 和 `trade_order` 表达交易快照。 |
| `tagScope` | `activity.tag_scope` | 用于表达标签活动适用范围。 |
| `calculatedAt` | `trade_order.trial_time` | 对齐 OpenAPI 当前字段名，不再用 `trialTime` 作为对外字段。 |
| `trialNo` | `trade_order.trial_no` | 锁单时保存试算流水号。 |
| `tagRule` | `crowd_tag_job.rule_expr` | API 使用字符串，入库前归一化为 JSON。 |
| `batchId` | `crowd_tag_job.batch_id`、`crowd_tag_detail.batch_id` | `batch_id` 必填，并与 `tag_id` 组成任务批次唯一键。 |
| `statStartTime`、`statEndTime`、`executeTime` | `crowd_tag_job.stat_start_time/stat_end_time/execute_time` | 支撑标签任务统计窗口和执行审计。 |
| `statistics`、`failReason` | `crowd_tag.statistics`、`crowd_tag_job.statistics/last_error` | 标签主表保存当前批次统计，任务表保存本次执行结果。 |
| `traceId` | `admin_operation_log.trace_id` | 审计字段必填，和 API 响应、日志追踪保持一致。 |
| `nextExecuteTime`、`lastExecuteTime` | `reliable_event.next_execute_time/last_execute_time` | 支撑可靠事件调度和后台治理展示。 |

## 7. 种子数据计划

M2 收口时，`V1__init_schema.sql` 至少包含以下种子数据：

| 数据类型 | 最低数量 | 用途 |
| --- | --- | --- |
| 管理员账号 | 1 | 支撑 M8/M10 后台治理和审计场景。 |
| 普通用户账号 | 1 | 支撑 M5/M6/M9 用户主链路。 |
| 商品 SKU | 3 | 支撑多商品会场展示。 |
| 活动 | 2 | 至少包含 1 个普通活动和 1 个标签活动。 |
| 折扣 | 2 类 | 覆盖固定金额或拼团价等不同折扣策略。 |
| 活动 SKU 绑定 | 每个活动至少 1 条 | 验证 `source/channel/sku_id -> activity_id`。 |
| 标签主表 | 1 | 支撑标签活动命中。 |
| 标签任务 | 1 | 支撑后台标签任务列表、执行和命中检查。 |
| 标签明细 | 至少 1 条 | 支撑用户标签命中。 |
| DCC 配置 | 至少 2 条 | 支撑基础运行态配置和后续治理页面。 |

种子数据只服务开发、Mock 对齐和空库验收，不代表生产初始化策略。

## 8. 验证计划

静态检查：

```powershell
rg -n "source|channel|tag_scope|last_execute_time|trace_id|stat_start_time|stat_end_time|execute_time" deploy/migration/V1__init_schema.sql docs/plan/08-contract-alignment.md
git diff --check
```

MySQL 空库验证：

```sql
CREATE DATABASE zxp_m2_verify DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zxp_m2_verify;
SOURCE deploy/migration/V1__init_schema.sql;
SHOW TABLES;
```

关键验收 SQL：

```sql
SHOW INDEX FROM activity_sku;
SHOW INDEX FROM trade_order;
SHOW INDEX FROM crowd_tag_job;
SHOW INDEX FROM reliable_event;
SHOW INDEX FROM admin_operation_log;

SELECT COUNT(*) FROM sku;
SELECT COUNT(*) FROM discount;
SELECT COUNT(*) FROM user_account WHERE role = 'ADMIN';
SELECT COUNT(*) FROM user_account WHERE role = 'USER';
SELECT COUNT(*) FROM crowd_tag;
SELECT COUNT(*) FROM crowd_tag_job;
SELECT COUNT(*) FROM crowd_tag_detail;
SELECT COUNT(*) FROM dcc_config;
```

唯一键冲突验证应在一次性事务或临时库中执行，验证后回滚或删除临时库，不污染开发数据。

## 9. 后续 Agent 分工建议

下一阶段如果继续使用子 Agent，建议按写入范围拆分：

| Agent | 写入范围 | 产出 |
| --- | --- | --- |
| 数据 Agent | `deploy/migration/V1__init_schema.sql` | 字段、唯一键、索引和种子数据补齐。 |
| 契约 Agent | `docs/plan/08-contract-alignment.md` | 字段映射、归一化策略和差异说明补齐。 |
| 验证 Agent | 不改生产文件，必要时新增临时验证记录需单独确认 | 空库执行脚本、唯一键验证步骤和检查结果。 |

三个 Agent 不能同时修改同一个文件。主 Agent 负责最终整合、运行验证、更新任务清单和 README。

## 10. 停止条件

执行 M2 修订时遇到以下情况必须暂停并请求确认：

- 需要修改 OpenAPI 字段、枚举或响应结构。
- `tagRule` 是否继续作为字符串存在需要产品或前后端共同确认。
- `reliable_event.next_execute_time` 是否允许终态为空需要改变 DB 约束。
- migration 空库执行失败且涉及金额、支付、退款或状态机事实源。
- 需要新增 MySQL 以外的基础设施或第三方依赖。

## 11. M2 收口标准

只有同时满足以下条件，才能把 M2 标记为已收口：

- `V1__init_schema.sql` 空库执行成功。
- 关键唯一键和索引存在，并能覆盖幂等、防重复、限购、运行态扫描和后台查询。
- 种子数据满足用户、管理员、商品、活动、折扣、标签和 DCC 的最低数量要求。
- `08-contract-alignment.md` 已同步所有字段映射和差异说明。
- `06-task-implementation-checklist.md` 已记录 M2 收口结论、验证结果、Harness 证据和剩余风险。
- 未创建 M3 才允许出现的后端 Java 模块、Mapper、Repository 或领域代码。
