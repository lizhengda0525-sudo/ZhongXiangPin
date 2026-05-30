# ZXP-CONTRACT-006 状态枚举与错误码契约实施计划

> **给 agentic 执行者：** 必须使用子技能：推荐 `superpowers:subagent-driven-development`，也可以使用 `superpowers:executing-plans`。请按任务逐项执行，步骤使用复选框 `- [ ]` 追踪进度。

**目标：** 固定 M1 阶段的状态枚举、错误码分段、错误响应示例和契约对齐映射，让后续 M3 后端枚举、M9 用户端类型、M10 管理端类型和 Mock 数据都复用同一套字符串值。

**架构：** 本任务只修改 OpenAPI、契约对齐文档和本计划文件，不创建后端 Java 模块、前端 TypeScript 类型或 migration。`docs/plan/openapi.yaml` 是错误码、状态枚举、schema 和 examples 的单一事实源；`docs/plan/08-contract-alignment.md` 记录枚举到数据库字段、Mock 和前端类型的对齐规则。

**技术栈：** OpenAPI 3.0.3 YAML、Redocly CLI lint、PowerShell、中文文档规范、M1 契约门禁。

---

## 文件边界

- 修改：`docs/plan/openapi.yaml`
  - 负责集中枚举：`ErrorCode`、`UserRole`、`UserStatus`、`ActivityStatus`、`OrderStatus`、`TeamStatus`、`TaskStatus`、`RefundSource`、`CodePurpose`、`EventType`、`TagJobType`、记录结果状态。
  - 负责错误码分段：认证 `AUTH_*`、会场 `MARKET_*`、交易 `TRADE_*`、后台 `ADMIN_*`、系统 `SYSTEM_*`。
  - 负责代表性错误示例：未登录、权限不足、管理员角色不足、参数错误、资源不存在、状态冲突、队伍满、价格变化、后台非法操作、系统异常。
  - 负责所有引用状态字段的 schema 统一使用 `$ref`，不在局部 schema 中手写魔法字符串。
- 修改：`docs/plan/08-contract-alignment.md`
  - 增补错误码分段规则、状态枚举映射、记录结果状态和前端/Mock 使用规则。
- 只读：`AGENTS.md`
  - 确认仓库实际协作入口文件名、M1 阶段边界、文档语言规则和停止条件。
- 只读：`README.md`
  - 确认当前阶段仍为契约与模型，不进入 M2/M3。
- 只读：`docs/README.md`
  - 确认 OpenAPI、Mock、migration 和契约对齐的职责。
- 只读：`docs/plan/06-task-implementation-checklist.md`
  - 确认 `ZXP-CONTRACT-006` 的参考依据、实现要点、验收标准和验证方式。
- 只读：`docs/sdd/tasks.md`
  - 确认 M1 只做契约和 Mock 示例，不落地后端 Java DTO。
- 只读：`docs/sdd/mock/strategy.md`
  - 确认 Mock 必须使用集中状态字符串、失败响应必须同时模拟 HTTP 状态和统一 body。
- 只读：`docs/plan/05-validation.md`
  - 确认契约验证、前端状态展示和高风险错误场景。
- 只读：`docs/harness/reference-map.md`
  - 确认旧项目只读参考规则；如果索引未列出枚举文件，使用 `rg` 定位真实枚举路径并在任务总结中记录。

## 范围护栏

- 不创建或修改 `backend/**`。
- 不创建 Java enum、DTO、Controller、Service、Mapper、Repository 或测试。
- 不创建或修改 `frontend/user-web/**`、`frontend/admin-web/**`。
- 不修改 `deploy/migration/**`；如果发现状态字段缺口，只在 `docs/plan/08-contract-alignment.md` 标注 M2 需要对齐。
- 不复制 `D:\ZXP\zhongxiangpin` 的旧项目源码，只提取行为证据：状态值、错误语义、角色取值和新项目需要主动收敛的差异。
- 不新增 `info`、`msg`、`IDEMPOTENT_SUCCESS` 或其他与统一响应契约冲突的别名。

## Harness 证据记录

执行前在任务日志中记录以下证据，不提交为仓库文件：

```text
ZXP-CONTRACT-006 Harness 证据：
- 旧项目枚举真实路径：
  D:\ZXP\zhongxiangpin\zhongxiangpin-types\src\main\java\com\zxp\zhongxiangpin\types\enums\TradeOrderStatus.java
  D:\ZXP\zhongxiangpin\zhongxiangpin-types\src\main\java\com\zxp\zhongxiangpin\types\enums\GroupBuyOrderStatus.java
  D:\ZXP\zhongxiangpin\zhongxiangpin-types\src\main\java\com\zxp\zhongxiangpin\types\enums\NotifyTaskStatus.java
  D:\ZXP\zhongxiangpin\zhongxiangpin-types\src\main\java\com\zxp\zhongxiangpin\types\enums\TradeCompensationTaskStatus.java
  D:\ZXP\zhongxiangpin\zhongxiangpin-types\src\main\java\com\zxp\zhongxiangpin\types\enums\UserRole.java
  D:\ZXP\zhongxiangpin\zhongxiangpin-types\src\main\java\com\zxp\zhongxiangpin\types\enums\ResponseCode.java
- 提取行为：
  旧订单状态包含 WAIT_PAY、PAID、CLOSED_TIMEOUT、REFUNDED、CANCELED；新项目 P0 主动收敛为 WAIT_PAY、PAID、REFUNDED、CLOSED_TIMEOUT，不引入 CANCELED。
  旧队伍状态包含 PROGRESS、COMPLETE、EXPIRED_UNFORMED、COMPLETE_AFTER_REFUND；新契约沿用这四个字符串值。
  旧通知和补偿任务状态包含 INIT、SUCCESS、FAILED；新项目统一任务治理需要 INIT、PROCESSING、SUCCESS、FAILED、RETRY_WAIT。
  旧角色包含 USER、ADMIN；新契约沿用。
  旧 ResponseCode 使用 A/E/B 编码和 info 字段；新契约统一使用业务前缀错误码和 message 字段。
- 新实现决策：
  M1 只固定 OpenAPI 枚举、错误码分段、Mock 示例和契约对齐映射；Java/TypeScript 枚举在 M3/M9/M10 再落地。
```

### 任务 1：契约基线审计

**文件：**
- 读取：`docs/plan/openapi.yaml`
- 读取：`docs/plan/06-task-implementation-checklist.md`
- 读取：`docs/plan/08-contract-alignment.md`
- 读取：`docs/sdd/mock/strategy.md`
- 读取：`docs/harness/reference-map.md`

- [ ] **步骤 1：定位任务定义和阶段边界**

运行：

```powershell
rg -n "ZXP-CONTRACT-006|状态枚举|错误码|WAIT_PAY|TeamStatus|TaskStatus|ErrorCode|M1 只" docs\plan\06-task-implementation-checklist.md docs\sdd\tasks.md docs\plan\08-contract-alignment.md docs\sdd\mock\strategy.md
```

预期：输出确认 `ZXP-CONTRACT-006` 的目标落地为 `docs/plan/openapi.yaml`、`docs/plan/08-contract-alignment.md` 和前后端枚举命名约定；M1 不创建后端 Java 代码。

- [ ] **步骤 2：定位当前 OpenAPI 枚举和错误码**

运行：

```powershell
rg -n "ErrorCode:|UserRole:|UserStatus:|ActivityStatus:|OrderStatus:|TeamStatus:|TaskStatus:|RefundSource:|CodePurpose:|EventType:|TagJobType:|AUTH_|MARKET_|TRADE_|ADMIN_|SYSTEM_" docs\plan\openapi.yaml
```

预期：能定位所有集中枚举和错误码；如果某个枚举只在局部 schema 中出现，后续任务要抽出为集中 schema。

- [ ] **步骤 3：定位旧项目枚举证据**

运行：

```powershell
rg --files D:\ZXP\zhongxiangpin | rg "TradeOrderStatus|GroupBuyOrderStatus|NotifyTaskStatus|TradeCompensationTaskStatus|UserRole|ResponseCode"
```

预期：输出位于 `zhongxiangpin-types\src\main\java\com\zxp\zhongxiangpin\types\enums\` 的六个枚举文件；如果路径变化，记录真实路径，不修改旧项目。

- [ ] **步骤 4：记录基线缺口**

在任务日志中记录以下检查表：

```text
ZXP-CONTRACT-006 基线审计：
- ErrorCode 是否集中定义并包含 0000：是/否
- ErrorCode 是否按 AUTH/MARKET/TRADE/ADMIN/SYSTEM 分段：是/否
- OrderStatus 是否只允许 WAIT_PAY/PAID/REFUNDED/CLOSED_TIMEOUT：是/否
- OpenAPI 是否仍出现 CANCELED 订单状态：是/否
- TeamStatus 是否只允许 PROGRESS/COMPLETE/EXPIRED_UNFORMED/COMPLETE_AFTER_REFUND：是/否
- TaskStatus 是否只允许 INIT/PROCESSING/SUCCESS/FAILED/RETRY_WAIT：是/否
- UserRole 是否只允许 USER/ADMIN：是/否
- 错误响应示例是否都使用 code/message/data/traceId：是/否
- 是否存在 info/msg 等旧响应消息别名：是/否
- 幂等重放是否使用 0000 + idempotentReplay=true 而不是伪错误码：是/否
- 契约对齐文档是否包含所有集中枚举映射：是/否
- 是否发现 M1 阶段边界违规：是/否
```

预期：缺口成为任务 2 到任务 5 的输入；不得因为缺口创建后端或前端代码。

### 任务 2：固定错误码分段和错误响应语义

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：收紧 `ErrorCode` 集中枚举**

将 `components.schemas.ErrorCode` 保持或调整为以下值集合：

```yaml
    ErrorCode:
      type: string
      description: >
        项目错误码。成功固定为 0000；失败按认证、会场、交易、后台和系统分段。
        后续实现可以新增错误码，但不得改变已有错误码语义。
      enum:
        - "0000"
        - AUTH_401
        - AUTH_403
        - AUTH_USERNAME_DUPLICATED
        - AUTH_PHONE_DUPLICATED
        - AUTH_PASSWORD_MISMATCH
        - AUTH_PHONE_INVALID
        - AUTH_BAD_CREDENTIALS
        - AUTH_CODE_INVALID
        - AUTH_ACCOUNT_DISABLED
        - AUTH_ADMIN_REQUIRED
        - MARKET_NOT_FOUND
        - MARKET_ACTIVITY_UNAVAILABLE
        - MARKET_TAG_NOT_MATCHED
        - TRADE_TEAM_FULL
        - TRADE_ORDER_STATUS_INVALID
        - TRADE_PRICE_CHANGED
        - TRADE_ORDER_NOT_FOUND
        - TRADE_ORDER_FORBIDDEN
        - ADMIN_INVALID_OPERATION
        - SYSTEM_PARAM_INVALID
        - SYSTEM_ERROR
```

预期：错误码为可读业务前缀，不沿用旧项目 `A0400/E0106/B0001` 数字分段；成功仍固定为字符串 `"0000"`。

- [ ] **步骤 2：确认代表性错误码都有示例**

运行：

```powershell
rg -n "AuthBadCredentials|AuthUsernameDuplicated|AuthPhoneDuplicated|AuthPasswordMismatch|AuthPhoneInvalid|AuthCodeInvalid|AuthAccountDisabled|AuthAdminRequired|MarketActivityUnavailable|MarketTagNotMatched|TradeTeamFull|TradePriceChanged|AdminInvalidOperation|DccInvalidValue|ThreadPoolInvalidConfig|systemError" docs\plan\openapi.yaml
```

预期：输出覆盖认证、会场、交易、后台和系统错误示例；如果缺少某个分段示例，在 `components.examples` 中补齐。

- [ ] **步骤 3：补充错误码分段规则**

在 `docs/plan/08-contract-alignment.md` 的统一响应契约或枚举映射附近确认或新增：

```markdown
- 错误码按业务域分段：认证使用 `AUTH_*`，会场和试算使用 `MARKET_*`，交易、支付、退款和订单使用 `TRADE_*`，后台治理使用 `ADMIN_*`，参数与系统兜底使用 `SYSTEM_*`。
- 旧项目 `ResponseCode` 中的 `info` 文案只作为行为参考；新项目响应消息字段统一为 `message`，不保留 `info`、`msg` 或数字错误码兼容别名。
- 幂等重放不是失败：重复 `clientOrderNo`、`payNo`、`refundNo` 返回既有成功结果时，HTTP 状态为 200，`code` 为 `"0000"`，并通过 `data.idempotentReplay=true` 表达。
```

预期：后续 M3/M9/M10 不会各自发明错误码或把幂等成功做成错误。

### 任务 3：固定订单、队伍、任务和角色枚举

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：收紧核心状态枚举**

将以下 schema 保持或调整为集中定义：

```yaml
    UserRole:
      type: string
      description: 用户角色。P0 只区分普通用户和后台管理员。
      enum:
        - USER
        - ADMIN
    UserStatus:
      type: string
      description: 用户账号状态。禁用账号不能登录。
      enum:
        - ENABLED
        - DISABLED
    OrderStatus:
      type: string
      description: 交易订单状态。P0 不引入 CANCELED，超时关闭统一使用 CLOSED_TIMEOUT。
      enum:
        - WAIT_PAY
        - PAID
        - REFUNDED
        - CLOSED_TIMEOUT
    TeamStatus:
      type: string
      description: 拼团队伍状态。
      enum:
        - PROGRESS
        - COMPLETE
        - EXPIRED_UNFORMED
        - COMPLETE_AFTER_REFUND
    TaskStatus:
      type: string
      description: 可靠事件、通知、补偿和标签任务的通用执行状态。
      enum:
        - INIT
        - PROCESSING
        - SUCCESS
        - FAILED
        - RETRY_WAIT
```

预期：状态值和任务清单完全一致；旧项目 `TradeOrderStatus.CANCELED` 不进入新 P0 契约。

- [ ] **步骤 2：固定辅助枚举**

确认或补齐以下集中 schema：

```yaml
    ActivityStatus:
      type: string
      enum:
        - DRAFT
        - ACTIVE
        - PAUSED
        - ENDED
    RefundSource:
      type: string
      enum:
        - USER
        - ADMIN
        - AUTO_EXPIRED
    CodePurpose:
      type: string
      enum:
        - REGISTER
        - LOGIN
        - BIND_PHONE
    EventType:
      type: string
      enum:
        - TEAM_COMPLETE_NOTIFY
        - REDIS_SLOT_RELEASE
        - TEAM_REBUILD
        - ORDER_TIMEOUT_REPAIR
        - REFUND_REPAIR
    TagJobType:
      type: string
      enum:
        - USERS
        - PARTICIPATE_COUNT
```

预期：会场、退款、验证码、可靠事件和标签任务都不在局部 schema 中重复写字符串集合。

- [ ] **步骤 3：补齐契约对齐枚举映射**

在 `docs/plan/08-contract-alignment.md` 的“枚举和状态映射”表中确认或新增：

```markdown
| `OrderStatus` | `WAIT_PAY`、`PAID`、`REFUNDED`、`CLOSED_TIMEOUT` | `trade_order.order_status` | 订单状态机只允许文档定义的前置状态流转。 |
| `TeamStatus` | `PROGRESS`、`COMPLETE`、`EXPIRED_UNFORMED`、`COMPLETE_AFTER_REFUND` | `team.status` | 队伍人数和退款状态推进必须和订单事务一致。 |
| `TaskStatus` | `INIT`、`PROCESSING`、`SUCCESS`、`FAILED`、`RETRY_WAIT` | `reliable_event.status`、`crowd_tag_job.status` | 可靠事件和标签任务共用任务状态语义。 |
| 支付/退款记录状态 | `SUCCESS`、`FAILED` | `pay_record.status`、`refund_record.status`、`admin_operation_log.result` | 记录结果状态，不等同于订单状态。 |
```

预期：OpenAPI 状态、M2 migration 字段和 M3 后端枚举之间的映射清楚。

### 任务 4：消除局部魔法字符串和旧兼容别名

**文件：**
- 修改：`docs/plan/openapi.yaml`

- [ ] **步骤 1：检查局部状态枚举**

运行：

```powershell
rg -n "enum:|WAIT_PAY|PAID|REFUNDED|CLOSED_TIMEOUT|PROGRESS|COMPLETE|EXPIRED_UNFORMED|COMPLETE_AFTER_REFUND|RETRY_WAIT|CANCELED" docs\plan\openapi.yaml
```

预期：核心业务状态出现在集中 schema、example 值或明确的 `OrderStatus`/`TeamStatus`/`TaskStatus` 引用中；不得出现 `CANCELED`。

- [ ] **步骤 2：将状态字段改为 `$ref`**

对下列 schema 中的状态字段确认使用集中引用：

```yaml
orderStatus:
  $ref: "#/components/schemas/OrderStatus"
teamStatus:
  $ref: "#/components/schemas/TeamStatus"
status:
  $ref: "#/components/schemas/TaskStatus"
refundSource:
  $ref: "#/components/schemas/RefundSource"
role:
  $ref: "#/components/schemas/UserRole"
```

预期：`OrderSummary`、`OrderDetail`、`LockOrderResult`、`PaymentResult`、`RefundResult`、`TeamSummary`、`ReliableEvent*`、`TagJob*` 和认证用户信息都不各自定义同名枚举。

- [ ] **步骤 3：检查响应消息别名**

运行：

```powershell
rg -n "\binfo\b|\bmsg\b|IDEMPOTENT_SUCCESS|CANCELED" docs\plan\openapi.yaml docs\plan\08-contract-alignment.md
```

预期：如果命中 `info` 或 `msg`，只允许出现在“禁止保留旧别名”的中文说明中；不得出现在 OpenAPI schema、example 或响应字段中。不得命中 `IDEMPOTENT_SUCCESS` 和 `CANCELED`，除非是在说明禁止新增或不进入 P0 的文字中。

### 任务 5：补齐 Mock 和前端类型对齐规则

**文件：**
- 修改：`docs/plan/08-contract-alignment.md`
- 读取：`docs/sdd/mock/strategy.md`

- [ ] **步骤 1：补齐前端集中枚举规则**

在前端类型规则中确认或新增：

```markdown
- 用户端和管理端共享同一套枚举值，不各自写魔法字符串。
- 订单、队伍、任务、活动、标签、退款来源等状态只能从集中枚举导出。
- 前端统一从 `message` 展示错误信息，禁止读取或透传 `info`、`msg` 等旧别名；错误处理必须保留 HTTP 状态分支。
```

预期：M9/M10 手写类型或 OpenAPI 生成类型时，都以 `openapi.yaml` 为准。

- [ ] **步骤 2：补齐 Mock 枚举和错误码规则**

在 Mock 检查规则中确认或新增：

```markdown
- Mock 字段名、枚举值、时间格式、金额单位与 OpenAPI 一致。
- Mock 状态字符串必须来自集中枚举或类型定义。
- Mock 失败响应必须同时模拟真实 HTTP 状态和统一 body，例如参数错误为 400、未登录为 401、权限不足为 403、资源不存在为 404、状态冲突为 409、系统异常为 500。
```

预期：前端内存 Mock 不会把失败场景全部包装成 200，也不会引入状态魔法字符串。

- [ ] **步骤 3：运行契约对齐关键字检查**

运行：

```powershell
rg -n "错误码按业务域分段|用户端和管理端共享同一套枚举值|Mock 状态字符串|OrderStatus|TeamStatus|TaskStatus|RefundSource|EventType|TagJobType|IDEMPOTENT_SUCCESS|info|msg" docs\plan\08-contract-alignment.md
```

预期：输出覆盖错误码分段、前端枚举、Mock 枚举、关键状态映射和旧别名禁用规则。

### 任务 6：验证、阶段边界和提交

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`
- 修改：`docs/superpowers/plans/2026-05-30-zxp-contract-006-status-enums-error-codes.md`

- [ ] **步骤 1：运行 OpenAPI lint**

优先运行：

```powershell
npx @redocly/cli lint docs\plan\openapi.yaml
```

预期：OpenAPI lint 通过；如果网络限制导致无法下载 Redocly CLI，按审批流程重跑。若仍无法执行，在任务总结中记录网络限制，并执行步骤 2 到步骤 4 的替代检查。

- [ ] **步骤 2：运行枚举和错误码关键字检查**

运行：

```powershell
rg -n "ErrorCode:|OrderStatus:|TeamStatus:|TaskStatus:|RefundSource:|EventType:|TagJobType:|AUTH_401|MARKET_ACTIVITY_UNAVAILABLE|TRADE_ORDER_STATUS_INVALID|ADMIN_INVALID_OPERATION|SYSTEM_PARAM_INVALID|SYSTEM_ERROR" docs\plan\openapi.yaml docs\plan\08-contract-alignment.md
```

预期：输出覆盖集中枚举、错误码分段和契约对齐映射。

- [ ] **步骤 3：运行旧别名和越界状态检查**

运行：

```powershell
rg -n "\binfo\b|\bmsg\b|IDEMPOTENT_SUCCESS|CANCELED|backend\\|frontend\\|deploy\\migration" docs\plan\openapi.yaml docs\plan\08-contract-alignment.md docs\superpowers\plans\2026-05-30-zxp-contract-006-status-enums-error-codes.md
```

预期：`info/msg/IDEMPOTENT_SUCCESS/CANCELED` 只允许出现在禁止性说明或 Harness 差异说明中；`backend/frontend/deploy/migration` 只允许出现在范围护栏和阶段边界说明中，不代表实际文件被修改。

- [ ] **步骤 4：运行阶段边界检查**

运行：

```powershell
git status --short
```

预期：本任务相关变更只包含：

```text
 M docs/plan/openapi.yaml
 M docs/plan/08-contract-alignment.md
?? docs/superpowers/plans/2026-05-30-zxp-contract-006-status-enums-error-codes.md
```

如果工作区已有用户改动，例如 `deploy/` 下未跟踪文件，保持不处理并在总结中说明它们不是本任务产生的变更。

- [ ] **步骤 5：人工验收记录**

在任务总结中记录：

```text
ZXP-CONTRACT-006 验收：
- OpenAPI 枚举值是否符合任务清单：是/否
- 错误码是否按认证、会场、交易、后台、系统分段：是/否
- 契约对齐映射是否覆盖 OrderStatus/TeamStatus/TaskStatus/UserRole/RefundSource/EventType/TagJobType：是/否
- Mock 示例是否仍使用 code/message/data/traceId：是/否
- 是否存在新增魔法字符串或旧响应别名：否
- 是否创建后端、前端或 migration 文件：否
- Harness 差异：旧项目 CANCELED 与数字错误码只作为参考，新 P0 不沿用。
```

- [ ] **步骤 6：提交**

运行：

```powershell
git add docs/plan/openapi.yaml docs/plan/08-contract-alignment.md docs/superpowers/plans/2026-05-30-zxp-contract-006-status-enums-error-codes.md
git commit -m "docs: define status enums and error codes"
```

预期：提交成功，提交内容只包含 M1 枚举、错误码、契约对齐和计划文档，不包含后端、前端或 migration 改动。

## 自查

- 规格覆盖：本计划覆盖 `ZXP-CONTRACT-006` 的订单状态、队伍状态、任务状态、用户角色、错误码分段、Mock 示例、前后端枚举命名约定和契约对齐映射。
- 占位符扫描：计划不含未决占位标记；每个需要修改契约的步骤都给出具体字段、枚举值、命令和预期结果。
- 类型一致性：计划中的 `OrderStatus`、`TeamStatus`、`TaskStatus`、`RefundSource`、`EventType`、`TagJobType`、`ErrorCode` 名称与现有 `docs/plan/openapi.yaml` 和 `docs/plan/08-contract-alignment.md` 保持一致。
- 阶段边界：所有任务只修改 OpenAPI、契约对齐文档和本计划文件；M1 不创建后端 Java、前端 TypeScript 或 migration。
