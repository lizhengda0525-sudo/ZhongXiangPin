# ZXP-CONTRACT-004 交易与订单接口契约实施计划

> **给 agentic 执行者：** 必须使用子技能：推荐 `superpowers:subagent-driven-development`，也可以使用 `superpowers:executing-plans`。请按任务逐项执行，步骤使用复选框 `- [ ]` 追踪进度。

**目标：** 固定 M1 阶段的锁单、Mock 支付、退款、订单查询和本地 Debug 契约，让交易链路每个外部请求都有幂等键、可回放响应、归属边界和 Mock 示例。

**架构：** 本任务只修改契约和对齐文档，不创建后端 Java 模块、DTO、Controller、Service、Mapper、Repository 或前端源码。`docs/plan/openapi.yaml` 是交易/订单路径、schema、响应示例和错误响应的单一事实源；`docs/plan/08-contract-alignment.md` 只补充字段、枚举、幂等和 local-only Debug 对齐规则。

**技术栈：** OpenAPI 3.0.3 YAML、Redocly CLI lint、PowerShell、中文文档规范、M1 契约门禁。

---

## 文件边界

- 修改：`docs/plan/openapi.yaml`
  - 负责 `/api/v1/trade/orders/lock`、`/api/v1/trade/payments/mock`、`/api/v1/trade/refunds`。
  - 负责 `/api/v1/orders`、`/api/v1/orders/{orderId}`。
  - 新增或收紧 `/api/v1/debug/trade/orders/{orderId}/timeout-close`，该端点只允许 local profile。
  - 负责交易/订单 schema：`LockOrderRequest`、`LockOrderResult`、`MockPayRequest`、`PaymentResult`、`UserRefundRequest`、`RefundResult`、`OrderSummary`、`OrderDetail`、`TradeDebugTimeoutCloseRequest`、`TradeDebugTimeoutCloseResult`。
  - 负责 examples：重复 `clientOrderNo`、重复 `payNo`、重复 `refundNo`、队伍已满、订单状态冲突、订单不存在、订单归属禁止访问、local Debug 成功和非 local 禁止。
- 修改：`docs/plan/08-contract-alignment.md`
  - 增补交易幂等、订单归属、local-only Debug、字段到 migration 的映射约束。
- 只读：`AGENT.md`
  - 确认 M1 阶段边界和文档语言规则。
- 只读：`README.md`
  - 确认当前阶段和 P0 主链路。
- 只读：`docs/README.md`
  - 确认 OpenAPI、Mock、migration 和契约对齐的职责。
- 只读：`docs/plan/06-task-implementation-checklist.md`
  - 确认 `ZXP-CONTRACT-004` 的验收标准。
- 只读：`docs/sdd/tasks.md`
  - 确认 M1 就绪门禁：只做契约和 Mock 示例。
- 只读：`docs/sdd/mock/strategy.md`
  - 确认交易 Mock 必要场景和 Debug local-only 规则。
- 只读：`docs/plan/02-business-design.md`
  - 确认状态机、幂等和支付退款规则。
- 只读：`docs/plan/05-validation.md`
  - 确认高风险用例：重复锁单、重复支付、重复退款、前端伪造身份、并发满员。
- 只读：`docs/harness/reference-map.md`
  - 确认旧项目参考路径，不复制旧项目源码。

## 范围护栏

- 不创建或修改 `backend/**`。
- 不创建 Java DTO、枚举、Controller、Service、Mapper、Repository 或后端测试。
- 不创建或修改 `frontend/user-web/**`、`frontend/admin-web/**`。
- 不修改 `deploy/migration/**`；如果发现字段缺口，只在 `docs/plan/08-contract-alignment.md` 记录 M2/M3 需要对齐的约束。
- 不复制 `D:\ZXP\zhongxiangpin` 的旧项目源码，只提取行为证据：锁单/支付/退款幂等、订单归属、状态流转和 Debug local-only 边界。

## Harness 证据记录

执行前在任务日志中记录以下证据，不提交为仓库文件：

```text
ZXP-CONTRACT-004 Harness 证据：
- 交易锁单/支付/退款参考：
  D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\trade\TradeController.java
  D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\trade\service\impl\TradeServiceImpl.java
- 订单查询参考：
  D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\order\OrderController.java
  D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\order\service\impl\OrderServiceImpl.java
- 前端结算/支付/订单参考：
  D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\CheckoutView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\PayResultView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\OrderListView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\OrderDetailView.vue
- 提取行为：
  clientOrderNo 用于锁单幂等；payNo 用于支付幂等；refundNo 用于退款幂等；订单查询只使用当前登录用户；详情必须校验归属；Debug 端点只能 local profile 启用。
- 新实现决策：
  M1 只固定 OpenAPI 和字段映射，不落地后端 Java 或前端 TypeScript。
```

### 任务 1：契约基线审计

**文件：**
- 读取：`docs/plan/openapi.yaml`
- 读取：`docs/plan/06-task-implementation-checklist.md`
- 读取：`docs/sdd/mock/strategy.md`
- 读取：`docs/plan/02-business-design.md`
- 读取：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：定位 ZXP-CONTRACT-004 任务定义**

运行：

```powershell
rg -n "ZXP-CONTRACT-004|锁单接口|支付接口|退款接口|Debug 接口|重复锁单|重复支付|重复退款" docs\plan\06-task-implementation-checklist.md docs\sdd\mock\strategy.md
```

预期：输出确认 `clientOrderNo`、`payNo`、`refundNo`、`refundSource`、`reason`、当前用户订单查询、归属校验、Debug local-only 和三类重复幂等示例要求。

- [ ] **步骤 2：定位当前交易与订单 OpenAPI 片段**

运行：

```powershell
rg -n "^  /api/v1/trade|^  /api/v1/orders|^  /api/v1/debug/trade|LockOrderRequest|LockOrderResult|MockPayRequest|PaymentResult|UserRefundRequest|RefundResult|OrderSummary|OrderDetail|TradeDebugTimeoutClose|LockOrderSuccess|PaymentSuccess|RefundSuccess|OrderPageSuccess|OrderDetailSuccess|TRADE_" docs\plan\openapi.yaml
```

预期：输出包含三个交易端点、两个订单端点、现有交易 schema、成功示例和交易错误码；如果没有 `/api/v1/debug/trade/orders/{orderId}/timeout-close` 或 `TradeDebugTimeoutClose*`，在任务 4 新增。

- [ ] **步骤 3：记录基线缺口**

在任务日志中记录以下检查表：

```text
ZXP-CONTRACT-004 基线审计：
- /api/v1/trade/orders/lock 是否存在：是/否
- 锁单请求是否包含 clientOrderNo/activityId/skuId/source/channel/teamId：是/否
- 锁单成功示例是否包含 idempotentReplay=false 或缺省 false：是/否
- 锁单重复 clientOrderNo 示例是否包含 idempotentReplay=true：是/否
- 队伍已满或名额冲突是否返回 HTTP 409 + TRADE_TEAM_FULL：是/否
- /api/v1/trade/payments/mock 是否存在：是/否
- 支付请求是否包含 orderId/payNo：是/否
- 重复 payNo 示例是否包含 idempotentReplay=true：是/否
- /api/v1/trade/refunds 是否存在：是/否
- 退款请求是否包含 orderId/refundNo/reason：是/否
- 退款成功结果是否包含 refundSource=USER：是/否
- 重复 refundNo 示例是否包含 idempotentReplay=true：是/否
- /api/v1/orders 是否只按当前用户查询且不接受 userId 参数：是/否
- /api/v1/orders/{orderId} 是否包含 403 归属失败和 404 不存在：是/否
- Debug local-only 端点是否存在：是/否
- 是否发现 M1 阶段边界违规：是/否
```

预期：缺口成为任务 2 到任务 5 的输入；不得因为缺口创建后端或前端代码。

### 任务 2：收紧锁单契约和幂等示例

**文件：**
- 修改：`docs/plan/openapi.yaml`

- [ ] **步骤 1：确认锁单路径响应覆盖成功、重复请求、队伍满员和未登录**

将 `/api/v1/trade/orders/lock` 保持或调整为以下响应形状：

```yaml
  /api/v1/trade/orders/lock:
    post:
      tags:
        - 交易
      summary: 锁单
      description: 开团或参团创建 WAIT_PAY 订单。后端以登录态 userId 为准，并使用 userId + clientOrderNo 幂等。
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/LockOrderRequest"
            examples:
              createTeam:
                summary: 开团锁单
                value:
                  clientOrderNo: CLIENT-20260525-0001
                  activityId: 20001
                  skuId: 30001
                  source: APP
                  channel: H5
              joinTeam:
                summary: 参团锁单
                value:
                  clientOrderNo: CLIENT-20260525-0002
                  activityId: 20001
                  skuId: 30001
                  teamId: TEAM-20260525-001
                  source: APP
                  channel: H5
      responses:
        "200":
          description: 锁单成功或幂等返回已有订单
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/LockOrderApiResponse"
              examples:
                success:
                  $ref: "#/components/examples/LockOrderSuccess"
                replay:
                  $ref: "#/components/examples/LockOrderReplay"
        "409":
          description: 锁单资源状态冲突
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
              examples:
                teamFull:
                  $ref: "#/components/examples/TradeTeamFull"
                priceChanged:
                  $ref: "#/components/examples/TradePriceChanged"
        "401":
          $ref: "#/components/responses/Unauthorized"
```

- [ ] **步骤 2：确认 `LockOrderRequest` 和 `LockOrderResult` 字段**

将相关 schema 保持或调整为：

```yaml
    LockOrderRequest:
      type: object
      required:
        - clientOrderNo
        - activityId
        - skuId
        - source
        - channel
      properties:
        clientOrderNo:
          type: string
          minLength: 8
          maxLength: 64
          description: 客户端锁单幂等号，与当前登录用户组成唯一约束。
        activityId:
          type: integer
          format: int64
        skuId:
          type: integer
          format: int64
        teamId:
          type: string
          nullable: true
          description: 参团时传入；开团时为空。
        source:
          type: string
        channel:
          type: string
    LockOrderResult:
      type: object
      required:
        - orderId
        - clientOrderNo
        - teamId
        - orderStatus
        - teamStatus
        - payPriceCent
        - payDeadline
        - canPay
      properties:
        orderId:
          type: string
        clientOrderNo:
          type: string
        teamId:
          type: string
        orderStatus:
          $ref: "#/components/schemas/OrderStatus"
        teamStatus:
          $ref: "#/components/schemas/TeamStatus"
        payPriceCent:
          type: integer
          format: int64
        payDeadline:
          type: string
          format: date-time
        canPay:
          type: boolean
        idempotentReplay:
          type: boolean
          default: false
          description: 是否为同一幂等键的重复请求重放结果。成功响应 code 仍固定为 0000。
```

- [ ] **步骤 3：补齐可复用锁单 examples**

在 `components.examples` 中新增或调整：

```yaml
    LockOrderSuccess:
      value:
        code: "0000"
        message: 成功
        traceId: trace-trade-lock-001
        data:
          orderId: ORD-20260525-0001
          clientOrderNo: CLIENT-20260525-0001
          teamId: TEAM-20260525-001
          orderStatus: WAIT_PAY
          teamStatus: PROGRESS
          payPriceCent: 7900
          payDeadline: "2026-05-25T10:15:00+08:00"
          canPay: true
          idempotentReplay: false
    LockOrderReplay:
      summary: 重复 clientOrderNo 幂等返回
      value:
        code: "0000"
        message: 成功
        traceId: trace-trade-lock-002
        data:
          orderId: ORD-20260525-0001
          clientOrderNo: CLIENT-20260525-0001
          teamId: TEAM-20260525-001
          orderStatus: WAIT_PAY
          teamStatus: PROGRESS
          payPriceCent: 7900
          payDeadline: "2026-05-25T10:15:00+08:00"
          canPay: true
          idempotentReplay: true
    TradeTeamFull:
      value:
        code: TRADE_TEAM_FULL
        message: 当前队伍已满
        data:
          teamId: TEAM-20260525-001
        traceId: trace-trade-team-full-001
    TradePriceChanged:
      value:
        code: TRADE_PRICE_CHANGED
        message: 价格已变化，请重新试算
        data:
          activityId: 20001
          reason: 活动或折扣版本已变化
        traceId: trace-trade-price-001
```

- [ ] **步骤 4：运行锁单片段检查**

运行：

```powershell
rg -n "LockOrderReplay|TradeTeamFull|TradePriceChanged|idempotentReplay: false|idempotentReplay: true|TRADE_TEAM_FULL|TRADE_PRICE_CHANGED" docs\plan\openapi.yaml
```

预期：能看到锁单成功、重复、队伍已满和价格变化示例；没有新增后端或前端文件。

### 任务 3：收紧 Mock 支付与用户退款契约

**文件：**
- 修改：`docs/plan/openapi.yaml`

- [ ] **步骤 1：确认 Mock 支付路径使用可复用重复支付示例**

将 `/api/v1/trade/payments/mock` 的 `200` examples 保持或调整为：

```yaml
              examples:
                success:
                  $ref: "#/components/examples/PaymentSuccess"
                replay:
                  $ref: "#/components/examples/PaymentReplay"
```

- [ ] **步骤 2：确认用户退款路径使用可复用重复退款示例**

将 `/api/v1/trade/refunds` 的 `200` examples 保持或调整为：

```yaml
              examples:
                success:
                  $ref: "#/components/examples/RefundSuccess"
                replay:
                  $ref: "#/components/examples/RefundReplay"
```

- [ ] **步骤 3：补齐支付和退款 examples**

在 `components.examples` 中新增或调整：

```yaml
    PaymentSuccess:
      value:
        code: "0000"
        message: 成功
        traceId: trace-pay-001
        data:
          orderId: ORD-20260525-0001
          payNo: PAY-20260525-0001
          orderStatus: PAID
          teamStatus: COMPLETE
          paidAt: "2026-05-25T10:02:00+08:00"
          completed: true
          idempotentReplay: false
    PaymentReplay:
      summary: 重复 payNo 幂等返回
      value:
        code: "0000"
        message: 成功
        traceId: trace-pay-002
        data:
          orderId: ORD-20260525-0001
          payNo: PAY-20260525-0001
          orderStatus: PAID
          teamStatus: COMPLETE
          paidAt: "2026-05-25T10:02:00+08:00"
          completed: true
          idempotentReplay: true
    RefundSuccess:
      value:
        code: "0000"
        message: 成功
        traceId: trace-refund-001
        data:
          orderId: ORD-20260525-0001
          refundNo: RF-20260525-0001
          refundSource: USER
          orderStatus: REFUNDED
          teamStatus: COMPLETE_AFTER_REFUND
          refundedAt: "2026-05-25T11:00:00+08:00"
          idempotentReplay: false
    RefundReplay:
      summary: 重复 refundNo 幂等返回
      value:
        code: "0000"
        message: 成功
        traceId: trace-refund-002
        data:
          orderId: ORD-20260525-0001
          refundNo: RF-20260525-0001
          refundSource: USER
          orderStatus: REFUNDED
          teamStatus: COMPLETE_AFTER_REFUND
          refundedAt: "2026-05-25T11:00:00+08:00"
          idempotentReplay: true
```

- [ ] **步骤 4：确认状态冲突错误能表达当前状态和期望状态**

将 `components.responses.TradeInvalidStatus` 保持或调整为：

```yaml
    TradeInvalidStatus:
      description: 订单状态冲突，不允许当前操作
      content:
        application/json:
          schema:
            $ref: "#/components/schemas/ErrorResponse"
          examples:
            invalidStatus:
              value:
                code: TRADE_ORDER_STATUS_INVALID
                message: 当前订单状态不允许操作
                data:
                  orderId: ORD-20260525-0001
                  currentStatus: REFUNDED
                  expectedStatus: PAID
                traceId: trace-trade-status-001
```

- [ ] **步骤 5：运行支付退款片段检查**

运行：

```powershell
rg -n "PaymentReplay|RefundReplay|refundSource: USER|expectedStatus: PAID|重复 payNo|重复 refundNo" docs\plan\openapi.yaml
```

预期：支付和退款均有成功示例、重复幂等示例、状态冲突错误上下文。

### 任务 4：收紧订单查询和本地 Debug 契约

**文件：**
- 修改：`docs/plan/openapi.yaml`

- [ ] **步骤 1：确认订单列表不接受可信 userId**

将 `/api/v1/orders` 的参数保持为只包含分页和状态筛选：

```yaml
      parameters:
        - $ref: "#/components/parameters/PageNo"
        - $ref: "#/components/parameters/PageSize"
        - name: status
          in: query
          required: false
          description: 订单状态筛选
          schema:
            $ref: "#/components/schemas/OrderStatus"
```

运行：

```powershell
rg -n "/api/v1/orders:|name: userId|只查询当前登录用户|不能通过请求参数传入可信 userId" docs\plan\openapi.yaml
```

预期：`/api/v1/orders` 描述明确只查当前登录用户；该端点参数下没有 `name: userId`。

- [ ] **步骤 2：确认订单详情具备归属失败和不存在响应**

将 `/api/v1/orders/{orderId}` 的响应保持或调整为：

```yaml
      responses:
        "200":
          description: 订单详情
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/OrderDetailApiResponse"
              examples:
                success:
                  $ref: "#/components/examples/OrderDetailSuccess"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "403":
          $ref: "#/components/responses/Forbidden"
        "404":
          $ref: "#/components/responses/OrderNotFound"
```

- [ ] **步骤 3：新增 local-only Debug 路径**

在 `paths` 中新增以下端点，放在用户订单路径之后、后台订单路径之前：

```yaml
  /api/v1/debug/trade/orders/{orderId}/timeout-close:
    post:
      tags:
        - 交易
      summary: 本地调试关闭超时未支付订单
      description: 仅 local profile 注册，用于本地演示 WAIT_PAY 订单超时关闭。非 local profile 必须返回 403，不能绕过真实状态机、归属和幂等校验。
      parameters:
        - $ref: "#/components/parameters/OrderId"
      requestBody:
        required: false
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/TradeDebugTimeoutCloseRequest"
            examples:
              normal:
                value:
                  now: "2026-05-25T10:16:00+08:00"
                  reason: 本地演示未支付订单超时关闭
      responses:
        "200":
          description: 本地关闭成功
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TradeDebugTimeoutCloseApiResponse"
              examples:
                success:
                  $ref: "#/components/examples/TradeDebugTimeoutCloseSuccess"
        "403":
          description: 非 local profile 禁止访问
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
              examples:
                nonLocal:
                  $ref: "#/components/examples/TradeDebugNonLocalForbidden"
        "404":
          $ref: "#/components/responses/OrderNotFound"
        "409":
          $ref: "#/components/responses/TradeInvalidStatus"
```

- [ ] **步骤 4：新增 Debug schema**

在交易 schema 附近新增：

```yaml
    TradeDebugTimeoutCloseRequest:
      type: object
      properties:
        now:
          type: string
          format: date-time
          description: 本地演示使用的当前时间；为空时由后端取当前时间。
        reason:
          type: string
          minLength: 2
          maxLength: 200
          description: 本地调试原因。
    TradeDebugTimeoutCloseResult:
      type: object
      required:
        - orderId
        - beforeStatus
        - afterStatus
        - closedAt
        - profile
      properties:
        orderId:
          type: string
        beforeStatus:
          $ref: "#/components/schemas/OrderStatus"
        afterStatus:
          $ref: "#/components/schemas/OrderStatus"
        closedAt:
          type: string
          format: date-time
        profile:
          type: string
          enum:
            - local
          description: 该响应只允许 local profile 产生。
```

- [ ] **步骤 5：新增 Debug response schema**

在 `components.schemas` 的响应派生 schema 区域新增：

```yaml
    TradeDebugTimeoutCloseApiResponse:
      allOf:
        - $ref: "#/components/schemas/ApiResponse"
        - type: object
          properties:
            data:
              $ref: "#/components/schemas/TradeDebugTimeoutCloseResult"
```

- [ ] **步骤 6：新增 Debug examples**

在 `components.examples` 中新增：

```yaml
    TradeDebugTimeoutCloseSuccess:
      value:
        code: "0000"
        message: 成功
        traceId: trace-debug-timeout-close-001
        data:
          orderId: ORD-20260525-0002
          beforeStatus: WAIT_PAY
          afterStatus: CLOSED_TIMEOUT
          closedAt: "2026-05-25T10:16:00+08:00"
          profile: local
    TradeDebugNonLocalForbidden:
      value:
        code: AUTH_403
        message: 权限不足
        data:
          resourceType: DEBUG_ENDPOINT
          resourceId: /api/v1/debug/trade/orders/{orderId}/timeout-close
          reason: 该端点只允许 local profile
        traceId: trace-debug-forbidden-001
```

- [ ] **步骤 7：运行订单和 Debug 片段检查**

运行：

```powershell
rg -n "/api/v1/debug/trade/orders|TradeDebugTimeoutClose|TradeDebugNonLocalForbidden|CLOSED_TIMEOUT|只允许 local profile|禁止水平越权|OrderNotFound" docs\plan\openapi.yaml
```

预期：输出能证明本地 Debug 契约存在，非 local profile 403 明确，订单详情仍具备归属失败和不存在响应。

### 任务 5：补充契约对齐规则

**文件：**
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：补充交易幂等和 Debug 规则**

在 `docs/plan/08-contract-alignment.md` 的“统一响应和错误语义”或相邻规则处加入：

```markdown
- 交易链路幂等键固定为：锁单 `clientOrderNo`、Mock 支付 `payNo`、退款 `refundNo`。重复请求返回已有成功结果时，HTTP 状态为 200，`code` 固定为 `"0000"`，`message` 固定为 `成功`，并通过 `data.idempotentReplay=true` 标记重放；不得新增 `IDEMPOTENT_SUCCESS` 等伪错误码。
- 用户端订单列表和订单详情归属以后端登录态为准，用户端请求不得传入可信 `userId`。订单详情访问他人订单时必须返回 HTTP 403 + `AUTH_403`，订单不存在时返回 HTTP 404 + `TRADE_ORDER_NOT_FOUND`。
- `/api/v1/debug/**` 只允许 local profile 注册和访问。非 local profile 访问必须返回 HTTP 403 + `AUTH_403`；Debug 端点不得绕过状态机、归属校验、幂等检查或统一响应信封。
```

- [ ] **步骤 2：补充 Debug 字段映射**

在关键字段映射表中加入：

```markdown
| `beforeStatus`、`afterStatus` | `trade_order.order_status` | 仅用于 local Debug 或运行态治理结果展示，不作为新的订单状态来源。 |
| `closedAt` | `trade_order.closed_at` | 超时关闭时间；M1 只固定契约字段，M2/M7 再确认持久化和任务推进细节。 |
```

- [ ] **步骤 3：运行契约对齐检查**

运行：

```powershell
rg -n "clientOrderNo|payNo|refundNo|idempotentReplay|/api/v1/debug|beforeStatus|afterStatus|closedAt|TRADE_ORDER_NOT_FOUND|AUTH_403" docs\plan\08-contract-alignment.md
```

预期：输出能证明交易幂等、订单归属、local-only Debug 和 Debug 字段映射已被契约对齐文档覆盖。

### 任务 6：验证、边界检查和提交

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`
- 修改：`docs/superpowers/plans/2026-05-30-zxp-contract-004-trade-order.md`

- [ ] **步骤 1：运行 OpenAPI 关键字检查**

运行：

```powershell
rg -n "LockOrderReplay|PaymentReplay|RefundReplay|TradeTeamFull|TradePriceChanged|TradeDebugTimeoutClose|TradeDebugNonLocalForbidden|idempotentReplay|clientOrderNo|payNo|refundNo" docs\plan\openapi.yaml
```

预期：所有关键契约元素都能被定位。

- [ ] **步骤 2：运行阶段边界检查**

运行：

```powershell
git status --short
```

预期：本任务相关变更只包含：

```text
 M docs/plan/openapi.yaml
 M docs/plan/08-contract-alignment.md
?? docs/superpowers/plans/2026-05-30-zxp-contract-004-trade-order.md
```

如果工作区已有用户改动，例如 `deploy/` 下未跟踪文件，保持不处理并在总结中说明它们不是本任务产生的变更。

- [ ] **步骤 3：运行 OpenAPI lint**

优先运行：

```powershell
npx @redocly/cli lint docs\plan\openapi.yaml
```

如果项目尚未安装 Node 依赖且网络受限，记录替代验证：

```powershell
rg -n "^\s{2}/api/v1/trade|^\s{2}/api/v1/orders|^\s{2}/api/v1/debug/trade|^\s{4}TradeDebugTimeoutClose|^\s{4}LockOrderReplay|^\s{4}PaymentReplay|^\s{4}RefundReplay" docs\plan\openapi.yaml
```

预期：OpenAPI lint 通过；如果无法运行 lint，替代检查能定位新增或收紧的所有路径、schema 和 examples，并在最终总结中说明 lint 未运行的原因。

- [ ] **步骤 4：检查禁止字段和旧响应别名**

运行：

```powershell
rg -n "info:|msg:|records:|list:|name: userId" docs\plan\openapi.yaml docs\plan\08-contract-alignment.md
```

预期：没有新增旧响应消息别名 `info/msg`；分页响应不新增 `records/list` 并行字段；用户订单查询不新增可信 `userId` 请求参数。若命中的是文档中“禁止使用”的说明文字，记录为可接受。

- [ ] **步骤 5：提交**

运行：

```powershell
git add docs/plan/openapi.yaml docs/plan/08-contract-alignment.md docs/superpowers/plans/2026-05-30-zxp-contract-004-trade-order.md
git commit -m "docs: add trade and order api contract"
```

预期：提交成功，提交内容只包含 M1 契约和计划文档，不包含后端、前端或 migration 改动。

## 自检结果

- 规格覆盖：本计划覆盖 `ZXP-CONTRACT-004` 的锁单、Mock 支付、用户退款、订单列表、订单详情、Debug local-only、幂等失败 Mock 示例、订单归属和当前用户边界。
- 占位符扫描：计划正文不含待补细节、稍后实现、复用上一任务等占位式表达。
- 类型一致性：`LockOrderResult`、`PaymentResult`、`RefundResult`、`TradeDebugTimeoutCloseResult` 都通过对应 `*ApiResponse` 派生；`OrderStatus`、`TeamStatus`、`RefundSource` 复用集中枚举；`idempotentReplay` 在三类幂等结果中语义一致。
- 阶段边界：所有任务只修改 OpenAPI、契约对齐文档和本计划文件；M1 不创建后端 Java、前端 TypeScript 或 migration。
