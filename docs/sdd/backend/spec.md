# 后端 SDD

## 目标

把后端重构为清晰的 Java 17 Spring Boot 多模块系统，在保留旧项目业务行为的基础上，强化契约纪律、领域边界、幂等、事务安全和运行态治理。

## M3 后实现目标

```text
backend/
  zhongxiangpin-contract
  zhongxiangpin-common
  zhongxiangpin-domain
  zhongxiangpin-application
  zhongxiangpin-infrastructure
  zhongxiangpin-trigger
  zhongxiangpin-app
```

## 阶段边界

本文描述的是 M3 后端骨架及后续业务实现的目标结构。`ZXP-CONTRACT-*` 和 `ZXP-DB-*` 是后端实现的输入，不代表在 M1/M2 提前创建 Java 模块。M1 只固定 OpenAPI、Mock 示例、错误码和状态枚举；M2 只固定 migration、唯一键、索引、状态字段和 MySQL 交易事实源；M3 才开始创建 `backend/zhongxiangpin-*` 模块和基础代码。

## 必读输入

- `docs/plan/openapi.yaml`
- `docs/plan/01-architecture.md`
- `docs/plan/02-business-design.md`
- `docs/plan/05-validation.md`
- `docs/plan/06-task-implementation-checklist.md`
- `docs/harness/reference-map.md`

## 模块职责

| 模块 | 负责 | 不负责 |
| --- | --- | --- |
| `contract` | 请求/响应 DTO、OpenAPI 生成或手写模型、共享枚举名称 | 业务决策、持久化注解 |
| `common` | `ApiResponse`、分页模型、错误码、基础异常、trace/audit 上下文 | 领域规则 |
| `domain` | 实体、值对象、状态机、领域服务、仓储端口 | Spring Web、MyBatis、Redis 客户端、Controller DTO |
| `application` | 用例编排、事务边界、权限敏感命令处理 | SQL、Redis 脚本、HTTP 细节 |
| `infrastructure` | MyBatis 适配、Redis、Lua 脚本、缓存、Mock 支付适配、任务锁 | 不经端口暴露的业务分支 |
| `trigger` | Controller、Scheduler、Interceptor、ExceptionHandler | 直接调用 Mapper、承载复杂事务业务 |
| `app` | 启动类、profile 装配、配置属性、健康检查装配 | 领域规则 |

## 横切契约

### API 响应

所有响应使用统一信封：

```json
{
  "code": "0000",
  "message": "成功",
  "data": {},
  "traceId": "..."
}
```

`message` 是唯一响应消息字段，M3 后端 DTO、序列化配置和前端契约不得保留旧项目 `info` 或 `msg` 兼容别名。

错误响应也使用相同信封，但 HTTP 状态必须表达协议层语义：参数错误 400、未登录 401、权限不足 403、资源不存在 404、资源状态冲突 409、系统异常 500。M3 的 `ExceptionHandler` 必须同时设置正确 HTTP 状态和 `code/message/data/traceId` body，不能为了统一 body 把失败响应全部返回 200。

分页结构统一为：

```json
{
  "pageNo": 1,
  "pageSize": 10,
  "total": 0,
  "items": []
}
```

### 身份

- 当前用户只来自后端 session/token 上下文。
- 请求中的 `userId` 永远不能作为归属、支付或退款决策依据。
- 后台写操作必须记录操作者 id、角色、动作、目标和结果。

### 金额

- 锁单前必须在后端重新试算。
- 订单保存原价、优惠金额、实付价、活动快照、折扣快照和试算时间。
- 活动或折扣后续变更不影响历史订单解释。

### 幂等

| 操作 | 幂等键 | 重复请求预期行为 |
| --- | --- | --- |
| 锁单 | `userId + clientOrderNo` | 返回已有订单或稳定的幂等响应。 |
| 支付结算 | `payNo` | 不重复增加支付数或成团人数。 |
| 退款 | `refundNo` | 不重复退款，不重复扣减团队人数。 |
| 可靠事件 | `eventType + bizKey` | handler 可安全重复执行。 |

### 状态机

订单状态：

```text
WAIT_PAY -> PAID -> REFUNDED
WAIT_PAY -> CLOSED_TIMEOUT
```

队伍状态：

```text
PROGRESS -> COMPLETE
PROGRESS -> EXPIRED_UNFORMED
COMPLETE -> COMPLETE_AFTER_REFUND
```

任务状态：

```text
INIT -> PROCESSING -> SUCCESS
INIT -> PROCESSING -> FAILED -> RETRY_WAIT -> PROCESSING
```

任何状态更新都必须检查前置状态。

## 数据库 SDD

migration 放在 `deploy/migration`，合并后不可修改，只能新增下一版。

MySQL 是交易事实源，队伍、订单、支付、退款、可靠事件、审计和标签批次的最终状态必须能从 migration 定义的表结构中恢复。Redis 只承载缓存、占位和运行态，不能替代 MySQL 的唯一键、条件更新和状态持久化。

首批必要表：

| 范围 | 表 | 必要约束 |
| --- | --- | --- |
| 认证 | `user_account` | username 唯一、phone 唯一、role/status 字段。 |
| 会场 | `sku`、`activity`、`activity_sku`、`discount`、`activity_version` 或版本字段 | 活动状态/时间/version；SKU 绑定唯一性。 |
| 标签 | `crowd_tag`、`crowd_tag_job`、`crowd_tag_detail` | `tagId + batchId + userAccountId`，current batch 指针。 |
| 交易 | `team`、`trade_order`、`pay_record`、`refund_record` | `userId + clientOrderNo`、`orderId`、`payNo`、`refundNo`、队伍条件更新。 |
| 运行态 | 优先 `reliable_event`，或兼容 notify/compensation 表 | `eventType + bizKey`、重试字段、下次执行时间、payload JSON。 |
| 治理 | `dcc_config`、`admin_operation_log`，线程池持久化表可按运行态治理需要后续新增 | 写操作可追踪操作者。 |

## 用例切片

### 认证

任务：`ZXP-CONTRACT-002`、`ZXP-DB-001`、`ZXP-BE-AUTH-001` 到 `ZXP-BE-AUTH-004`。

Harness 文件：

- `AuthController.java`
- `AuthServiceImpl.java`
- `RefreshTokenInterceptor.java`
- `LoginInterceptor.java`
- `AdminInterceptor.java`

验收：

- 注册、密码登录、验证码登录、登出、当前用户 API 可用。
- 注册必须提交手机号和验证码，验证码用途为 `REGISTER`，且必须与手机号匹配并在有效期内。
- 禁用账号和密码错误返回稳定错误码。
- 订单和退款 API 拒绝未登录用户。
- 后台 API 拒绝非管理员用户。

### 会场与试算

任务：`ZXP-CONTRACT-003`、`ZXP-DB-002`、`ZXP-DB-005`、`ZXP-BE-MARKET-001` 到 `ZXP-BE-MARKET-005`。

验收：

- 会场列表一次返回商品、活动摘要、拼团价、团队统计和可参团队伍，前端可直接展示。
- 试算校验活动状态、时间、渠道、折扣策略、标签命中和限购。
- 后台变更活动后，缓存通过版本或精确 key 失效。
- 缓存缺失时可回查 MySQL，避免 N+1 查询。

### 交易

任务：`ZXP-CONTRACT-004`、`ZXP-DB-003`、`ZXP-BE-TRADE-001` 到 `ZXP-BE-TRADE-006`。

验收：

- 锁单使用 Redis Lua 加 MySQL 条件更新保护名额。
- 重复 `clientOrderNo`、`payNo`、`refundNo` 都具备幂等行为。
- 支付只把订单推进到 `PAID` 一次，并且只完成团队一次。
- 退款记录来源和原因，并遵守订单归属或管理员权限。
- 用户订单详情不能暴露其他用户订单。

### 运行态治理

任务：`ZXP-DB-004`、`ZXP-BE-RUNTIME-001` 到 `ZXP-BE-RUNTIME-005`。

验收：

- 超时未支付订单会关闭并释放队伍锁定数。
- 过期未成团队伍会为已支付订单创建退款。
- Redis 队伍运行态丢失或脏数据可从 MySQL 重建。
- 可靠事件可重试、可手动执行、可查看。
- 运行态日志尽量包含 traceId、orderId 和 teamId。

### 后台 API

任务：`ZXP-CONTRACT-005`、`ZXP-BE-ADMIN-001` 到 `ZXP-BE-ADMIN-005`。

验收：

- 后台必须支持活动、标签、订单、任务、DCC、线程池和审计查询。
- 后台写操作必须记录审计数据。
- 危险操作需要显式请求字段，例如原因或操作者确认。

## 验证

| 层级 | 命令或证据 |
| --- | --- |
| 契约 | OpenAPI lint；所有端点响应引用统一信封。 |
| 单元 | 领域状态机、折扣和标签逻辑测试。 |
| 应用 | 认证、会场试算、锁单、支付、退款和运行态修复用例测试。 |
| 持久化 | 唯一键、条件更新和 JSON payload 持久化 Mapper 测试。 |
| Controller | 成功、参数错误、鉴权失败和业务失败测试。 |
| 集成 | 并发锁单、重复支付/退款、Redis/MySQL 不一致修复测试。 |

## 后端 P0 完成标准

- 在 `backend` 下执行 `mvn test` 通过。
- local profile 下 `/api/v1/health` 返回 app/db/redis 状态。
- OpenAPI 与 M3 后落地的后端 DTO 对齐。
- 后台活动、标签、订单、任务、DCC、线程池和审计治理可用。
- `docs/plan/05-validation.md` 中的高风险场景有测试或验收记录。
