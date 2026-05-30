# ZXP-CONTRACT-005 后台治理接口契约实施计划

> **给 agentic 执行者：** 必须使用子技能：推荐 `superpowers:subagent-driven-development`，也可以使用 `superpowers:executing-plans`。请按任务逐项执行，步骤使用复选框 `- [ ]` 追踪进度。

**目标：** 固定 M1 阶段的后台治理 OpenAPI 契约、管理员权限边界、写操作审计字段和 Mock 示例，让 M8 后台 API 与 M10 管理端都能基于同一份类型依据推进。

**架构：** 本任务只修改契约和对齐文档，不创建后端 Java 模块、DTO、Controller、Service、Mapper、Repository 或前端源码。`docs/plan/openapi.yaml` 是后台治理路径、schema、响应示例和错误响应的单一事实源；`docs/plan/08-contract-alignment.md` 记录管理员权限、operator、traceId、审计和字段映射约束。

**技术栈：** OpenAPI 3.0.3 YAML、Redocly CLI lint、PowerShell、中文文档规范、M1 契约门禁。

---

## 文件边界

- 修改：`docs/plan/openapi.yaml`
  - 负责 `/api/v1/admin/orders`、`/api/v1/admin/orders/{orderId}`、`/api/v1/admin/orders/{orderId}/refunds`。
  - 负责 `/api/v1/admin/events/overview`、`/api/v1/admin/events`、`/api/v1/admin/events/{eventId}`、`/api/v1/admin/events/{eventId}/execute`、`/api/v1/admin/events/{eventId}/retry`。
  - 负责 `/api/v1/admin/activities`、`/api/v1/admin/activities/{activityId}`、`/api/v1/admin/activities/{activityId}/sku-bindings`。
  - 负责 `/api/v1/admin/tag-jobs`、`/api/v1/admin/tag-jobs/{tagId}/{batchId}`、`/api/v1/admin/tag-jobs/{tagId}/{batchId}/execute`、`/api/v1/admin/tag-hit-check`。
  - 负责 `/api/v1/admin/dcc/configs`、`/api/v1/admin/dcc/configs/{configKey}`、`/api/v1/admin/thread-pools`、`/api/v1/admin/thread-pools/{threadPoolName}`、`/api/v1/admin/audit-logs`。
  - 负责后台治理 schema：`AdminOperationRequest`、`AdminRefundRequest`、`AdminOrderSummary`、`AdminOrderPage`、`EventOverview`、`ReliableEventSummary`、`ReliableEventDetail`、`EventPage`、`AdminActivity*`、`TagJob*`、`DccConfig*`、`ThreadPool*`、`AuditLog*`。
  - 负责 examples：后台分页成功、活动保存失败、标签执行失败、订单重复退款、任务执行冲突、DCC 非法值、线程池非法值、非管理员访问、审计日志分页。
- 修改：`docs/plan/08-contract-alignment.md`
  - 增补后台治理字段映射、管理员写操作审计约束、`traceId` 串联 API 响应和审计记录、后台 Mock 和管理端类型化边界。
- 只读：`AGENT.md`
  - 确认 M1 阶段边界和文档语言规则。
- 只读：`README.md`
  - 确认当前阶段和 P0 主链路。
- 只读：`docs/README.md`
  - 确认 OpenAPI、Mock、migration 和契约对齐的职责。
- 只读：`docs/plan/06-task-implementation-checklist.md`
  - 确认 `ZXP-CONTRACT-005` 的验收标准。
- 只读：`docs/sdd/mock/strategy.md`
  - 确认后台 Mock 必要场景和 HTTP 状态规则。
- 只读：`docs/sdd/frontend-admin/spec.md`
  - 确认管理端页面、端点和类型需求。
- 只读：`docs/sdd/backend/spec.md`
  - 确认后台 API 与审计上下文后续实现边界。
- 只读：`docs/plan/02-business-design.md`
  - 确认后台治理 P0 能力范围。
- 只读：`docs/harness/reference-map.md`
  - 确认旧项目参考路径，不复制旧项目源码。

## 范围护栏

- 不创建或修改 `backend/**`。
- 不创建 Java DTO、枚举、Controller、Service、Mapper、Repository 或后端测试。
- 不创建或修改 `frontend/user-web/**`、`frontend/admin-web/**`。
- 不修改 `deploy/migration/**`；如果发现字段缺口，只在 `docs/plan/08-contract-alignment.md` 记录 M2/M3/M8 需要对齐的约束。
- 不复制 `D:\ZXP\zhongxiangpin` 的旧项目源码，只提取行为证据：管理端端点、操作参数、审计字段、任务治理、DCC 和线程池配置边界。

## Harness 证据记录

执行前在任务日志中记录以下证据，不提交为仓库文件：

```text
ZXP-CONTRACT-005 Harness 证据：
- 管理端接口参考：
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\api\admin.ts
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\router.ts
- 管理端页面参考：
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\ActivityView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\TagView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\OrderView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\TaskView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\DccView.vue
  D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\ThreadPoolView.vue
- 提取行为：
  后台端点只允许 ADMIN；活动、标签、订单退款、任务执行、任务重试、DCC 更新和线程池更新都必须提交 reason/operatorConfirm 并产生审计；列表统一使用 pageNo/pageSize/total/items；失败响应必须同时模拟 HTTP 状态和 code/message/data/traceId。
- 新实现决策：
  M1 只固定 OpenAPI、Mock 示例和字段映射，不落地后端 Java 或前端 TypeScript。
```

### 任务 1：契约基线审计

**文件：**
- 读取：`docs/plan/openapi.yaml`
- 读取：`docs/plan/06-task-implementation-checklist.md`
- 读取：`docs/sdd/mock/strategy.md`
- 读取：`docs/sdd/frontend-admin/spec.md`
- 读取：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：定位 ZXP-CONTRACT-005 任务定义**

运行：

```powershell
rg -n "ZXP-CONTRACT-005|后台治理接口|活动配置|SKU 绑定|标签任务|订单列表|订单详情|后台订单退款|通知任务|补偿任务|DCC|线程池|审计日志" docs\plan\06-task-implementation-checklist.md docs\sdd\mock\strategy.md docs\sdd\frontend-admin\spec.md
```

预期：输出确认后台治理覆盖活动、SKU 绑定、标签任务保存/更新/执行/命中检查、订单列表/详情/退款、任务列表/详情/执行/重试、DCC 配置、线程池配置、审计日志查询，以及所有写操作记录 operator 的要求。

- [ ] **步骤 2：定位当前后台 OpenAPI 片段**

运行：

```powershell
rg -n "^  /api/v1/admin|AdminOperationRequest|AdminRefundRequest|AdminOrderSummary|EventOverview|ReliableEvent|AdminActivity|TagJob|DccConfig|ThreadPool|AuditLog|AuthAdminRequired|ADMIN_" docs\plan\openapi.yaml
```

预期：输出包含后台订单、任务、活动、标签、配置、线程池和审计端点；输出包含后台 schema、成功示例、`AUTH_ADMIN_REQUIRED` 和 `ADMIN_INVALID_OPERATION`。

- [ ] **步骤 3：记录基线缺口**

在任务日志中记录以下检查表：

```text
ZXP-CONTRACT-005 基线审计：
- 所有 /api/v1/admin/** 端点是否同时声明 401 未登录和 403 非管理员：是/否
- 403 非管理员示例是否使用 AUTH_ADMIN_REQUIRED：是/否
- 后台列表响应是否统一 pageNo/pageSize/total/items：是/否
- 后台写操作请求是否统一包含 reason/operatorConfirm：是/否
- 活动保存是否覆盖缓存版本或精确失效说明：是/否
- SKU 绑定是否覆盖 source/channel/skuId：是/否
- 标签任务是否覆盖保存、更新、执行和命中检查：是/否
- 订单治理是否覆盖列表、详情、管理员退款和重复退款：是/否
- 任务治理是否覆盖概览、列表、详情、执行、重试和状态冲突：是/否
- DCC 更新是否覆盖 key 白名单和值范围失败：是/否
- 线程池更新是否覆盖 corePoolSize <= maximumPoolSize 失败：是/否
- 审计日志是否覆盖 operator/action/target/result/traceId：是/否
- 是否发现 M1 阶段边界违规：是/否
```

预期：缺口成为任务 2 到任务 6 的输入；不得因为缺口创建后端或前端代码。

### 任务 2：统一后台权限和错误响应契约

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：收紧非管理员错误示例**

将 `Forbidden` 保持为通用 403，同时确保后台端点使用或引用 `AuthAdminRequired` 示例。示例形状如下：

```yaml
    AuthAdminRequired:
      summary: 管理端登录角色不足
      value:
        code: AUTH_ADMIN_REQUIRED
        message: 当前账号不是管理员
        data:
          currentStatus: USER
          expectedStatus: ADMIN
        traceId: trace-auth-admin-403
```

预期：普通用户访问后台端点有稳定 code，管理端 Mock 不需要猜测 403 文案。

- [ ] **步骤 2：为所有后台端点补齐 401 与 403**

运行：

```powershell
rg -n "^  /api/v1/admin|\"401\"|\"403\"" docs\plan\openapi.yaml
```

对每个 `/api/v1/admin/**` 操作确认响应至少包含：

```yaml
        "401":
          $ref: "#/components/responses/Unauthorized"
        "403":
          $ref: "#/components/responses/Forbidden"
```

预期：未登录和非管理员边界都可被前端 Mock、M8 后端和 M10 管理端一致处理。

- [ ] **步骤 3：补齐后台通用操作失败示例**

在 `components/examples` 中确认或新增以下错误示例：

```yaml
    AdminInvalidOperation:
      summary: 后台操作不满足业务状态
      value:
        code: ADMIN_INVALID_OPERATION
        message: 当前状态不允许执行该后台操作
        data:
          targetType: EVENT
          targetId: EVT-20260525-0001
          currentStatus: PROCESSING
          expectedStatus: FAILED
        traceId: trace-admin-op-409
```

预期：任务执行冲突、任务重试冲突、DCC 非法值、线程池非法值可以共用或按目标类型扩展同一错误形状。

### 任务 3：固定活动和标签治理契约

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：确认活动治理端点和 schema**

运行：

```powershell
rg -n "/api/v1/admin/activities|AdminActivity|AdminActivitySaveRequest|AdminActivityBindSkuRequest|AdminActivityPageSuccess|AdminActivitySuccess" docs\plan\openapi.yaml
```

确保活动保存请求包含以下字段：

```yaml
    AdminActivitySaveRequest:
      allOf:
        - $ref: "#/components/schemas/AdminOperationRequest"
        - type: object
          required:
            - title
            - discountType
            - discountValueCent
            - targetCount
            - validMinutes
            - takeLimitCount
            - startTime
            - endTime
            - status
          properties:
            activityId:
              type: integer
              format: int64
            title:
              type: string
            discountType:
              type: string
            discountValueCent:
              type: integer
              format: int64
            targetCount:
              type: integer
            validMinutes:
              type: integer
            takeLimitCount:
              type: integer
            status:
              $ref: "#/components/schemas/ActivityStatus"
```

预期：管理端活动表单不需要 `any`，后续 M8 能明确校验时间窗、成团人数、限购、折扣和状态。

- [ ] **步骤 2：确认 SKU 绑定请求**

确保 SKU 绑定请求继承 `AdminOperationRequest`，并包含 `source/channel/skuId`：

```yaml
    AdminActivityBindSkuRequest:
      allOf:
        - $ref: "#/components/schemas/AdminOperationRequest"
        - type: object
          required:
            - source
            - channel
            - skuId
          properties:
            source:
              type: string
              example: APP
            channel:
              type: string
              example: H5
            skuId:
              type: integer
              format: int64
```

预期：活动 SKU 绑定会影响用户端会场、试算和锁单，契约明确这不是前端本地配置。

- [ ] **步骤 3：确认标签治理端点和失败示例**

运行：

```powershell
rg -n "/api/v1/admin/tag-jobs|/api/v1/admin/tag-hit-check|TagJobSaveRequest|TagJobUpdateRequest|TagJobExecuteResult|TagHitResult|TagJobExecuteSuccess" docs\plan\openapi.yaml
```

补齐标签执行失败示例：

```yaml
    TagJobExecuteFailed:
      summary: 标签任务执行失败
      value:
        code: ADMIN_INVALID_OPERATION
        message: 标签任务执行失败
        data:
          tagId: TAG-VIP-001
          batchId: BATCH-20260525-001
          status: FAILED
          failReason: 规则表达式不合法
        traceId: trace-tag-execute-409
```

预期：后台 Mock 覆盖“标签任务执行成功/失败”，管理端可以展示失败原因。

### 任务 4：固定订单和任务治理契约

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：确认后台订单列表和详情**

运行：

```powershell
rg -n "/api/v1/admin/orders|AdminOrderSummary|AdminOrderPage|OrderDetailApiResponse|AdminRefundRequest|RefundApiResponse" docs\plan\openapi.yaml
```

确保后台订单列表筛选至少包含 `orderId/teamId/userId/status/startTime/endTime`，分页响应使用：

```yaml
    AdminOrderPage:
      allOf:
        - $ref: "#/components/schemas/PageResponse"
        - type: object
          properties:
            items:
              type: array
              items:
                $ref: "#/components/schemas/AdminOrderSummary"
```

预期：管理端订单页可以按订单号、队伍号、用户、状态和时间筛选，并且列表类型和用户端订单列表区分。

- [ ] **步骤 2：补齐管理员退款幂等示例**

确认 `AdminRefundRequest` 包含 `refundNo/reason/operatorConfirm`，并在退款响应中补齐重复退款示例：

```yaml
                replay:
                  value:
                    code: "0000"
                    message: 成功
                    traceId: trace-admin-refund-replay-001
                    data:
                      orderId: ORD-20260525-0001
                      refundNo: RF-ADMIN-20260525-0001
                      refundSource: ADMIN
                      orderStatus: REFUNDED
                      teamStatus: COMPLETE_AFTER_REFUND
                      refundedAt: "2026-05-25T11:00:00+08:00"
                      idempotentReplay: true
```

预期：后台订单退款成功/重复都可被 Mock 和管理端弹窗稳定展示。

- [ ] **步骤 3：确认任务治理端点和操作冲突**

运行：

```powershell
rg -n "/api/v1/admin/events|EventOverview|ReliableEventSummary|ReliableEventDetail|EventPage|EventDetailSuccess|EventPageSuccess|AdminOperationRequest" docs\plan\openapi.yaml
```

为执行和重试端点补齐 409：

```yaml
        "409":
          description: 任务当前状态不允许执行该操作
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
              examples:
                invalidOperation:
                  $ref: "#/components/examples/AdminInvalidOperation"
```

预期：任务重试成功/失败和执行成功/失败都可被契约表达，后续 M8 不把状态冲突包装成 200。

### 任务 5：固定 DCC、线程池和审计查询契约

**文件：**
- 修改：`docs/plan/openapi.yaml`
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：确认 DCC 配置契约**

运行：

```powershell
rg -n "/api/v1/admin/dcc/configs|DccConfig|DccConfigUpdateRequest|DccConfigListApiResponse|DccConfigApiResponse" docs\plan\openapi.yaml
```

确保 `DccConfigUpdateRequest` 继承 `AdminOperationRequest` 并包含 `configValue`：

```yaml
    DccConfigUpdateRequest:
      allOf:
        - $ref: "#/components/schemas/AdminOperationRequest"
        - type: object
          required:
            - configValue
          properties:
            configValue:
              type: string
```

预期：DCC 更新必须可审计，非法 key 或非法值返回 400，不在 Mock 中静默成功。

- [ ] **步骤 2：确认线程池配置契约**

运行：

```powershell
rg -n "/api/v1/admin/thread-pools|ThreadPoolRuntime|ThreadPoolUpdateRequest|ThreadPoolListSuccess|ThreadPoolRuntimeSuccess" docs\plan\openapi.yaml
```

确保更新请求包含数值约束和审计字段：

```yaml
    ThreadPoolUpdateRequest:
      allOf:
        - $ref: "#/components/schemas/AdminOperationRequest"
        - type: object
          required:
            - corePoolSize
            - maximumPoolSize
          properties:
            corePoolSize:
              type: integer
              minimum: 1
            maximumPoolSize:
              type: integer
              minimum: 1
```

预期：`corePoolSize > maximumPoolSize` 必须有 400 示例，管理端可以把错误展示在操作弹窗。

- [ ] **步骤 3：确认审计日志查询字段**

运行：

```powershell
rg -n "/api/v1/admin/audit-logs|AuditLog|AuditLogPage|AuditLogPageSuccess|operatorId|operatorName|traceId|targetType|targetId" docs\plan\openapi.yaml docs\plan\08-contract-alignment.md
```

确保审计记录包含：

```yaml
    AuditLog:
      type: object
      required:
        - auditId
        - operatorId
        - operatorName
        - action
        - targetType
        - targetId
        - result
        - traceId
        - createdAt
```

预期：活动、标签、订单、任务、配置和线程池写操作都能被审计页追踪。

### 任务 6：补齐契约对齐文档

**文件：**
- 修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：补齐后台治理映射**

在“关键字段映射”中确认或新增以下行：

```markdown
| `operatorId`、`operatorName` | `admin_operation_log.operator_id`、`admin_operation_log.operator_name` | 后台写操作必须记录。 |
| `traceId` | `admin_operation_log.trace_id`，API 响应信封字段 | 用于联动接口响应、日志和审计。 |
| `configKey`、`configValue` | `dcc_config.config_key`、`dcc_config.config_value` | 更新必须校验 key 白名单和值范围。 |
| `threadPoolName`、`corePoolSize`、`maximumPoolSize` | 运行态线程池配置或后续治理表 | M1 只固定契约字段，M7/M8 再落地动态治理实现。 |
```

预期：OpenAPI 字段与 M2/M8 的数据事实源边界清晰，不提前要求 migration。

- [ ] **步骤 2：补齐管理端类型约束**

在契约对齐文档中确认或新增规则：

```markdown
- 管理端不得为后台响应使用隐式 `any`；列表统一从 `data.pageNo/pageSize/total/items` 读取。
- 后台写操作统一提交 `reason` 和 `operatorConfirm`；后端从管理员登录态生成 `operatorId/operatorName`，前端不得伪造操作者。
- 后台 Mock 必须覆盖成功、非管理员 403、参数错误 400、状态冲突 409 和系统异常 500 中与页面相关的场景。
```

预期：`ZXP-FE-ADMIN-001` 到 `ZXP-FE-ADMIN-004` 可以基于契约生成或手写类型，不再依赖隐式 `any`。

### 任务 7：验证、阶段边界和提交

**文件：**
- 读取：`docs/plan/openapi.yaml`
- 读取：`docs/plan/08-contract-alignment.md`
- 读取：`docs/superpowers/plans/2026-05-30-zxp-contract-005-admin-governance.md`

- [ ] **步骤 1：运行 OpenAPI 结构检查**

优先运行：

```powershell
npx @redocly/cli lint docs/plan/openapi.yaml
```

预期：通过，或仅存在与 `ZXP-CONTRACT-005` 无关的既有 warning。如果网络限制导致无法下载 Redocly CLI，按审批流程重跑；仍无法执行时，在任务总结中记录网络限制，并继续做手工 YAML 检查。

- [ ] **步骤 2：运行后台契约关键字检查**

运行：

```powershell
rg -n "^  /api/v1/admin|AUTH_ADMIN_REQUIRED|ADMIN_INVALID_OPERATION|AdminOperationRequest|AdminRefundRequest|DccConfigUpdateRequest|ThreadPoolUpdateRequest|AuditLogPageApiResponse|TagJobExecuteFailed|idempotentReplay" docs\plan\openapi.yaml
```

预期：输出覆盖后台端点、管理员权限错误、后台操作错误、通用操作请求、管理员退款、配置更新、线程池更新、审计分页、标签执行失败和退款幂等示例。

- [ ] **步骤 3：确认 M1 边界**

运行：

```powershell
git diff --name-only
```

预期：只包含 `docs/plan/openapi.yaml`、`docs/plan/08-contract-alignment.md` 和本计划文件；不得包含 `backend/**`、`frontend/**` 或 `deploy/migration/**`。

- [ ] **步骤 4：人工验收记录**

在任务总结中记录：

```text
ZXP-CONTRACT-005 验收：
- 后台订单、任务、活动、标签、DCC、线程池、审计端点是否齐全：是/否
- 所有后台写操作是否有 reason/operatorConfirm：是/否
- 所有后台写操作是否有审计字段映射：是/否
- 非管理员访问是否返回 403 + AUTH_ADMIN_REQUIRED：是/否
- 未登录访问是否返回 401 + AUTH_401：是/否
- Mock 示例是否覆盖分页和操作失败：是/否
- 管理端类型是否可由 OpenAPI schema 推导且不依赖隐式 any：是/否
- 是否创建后端、前端或 migration 文件：否
```

预期：验收记录能证明 `ZXP-CONTRACT-005` 只完成 M1 契约冻结，不越过阶段边界。

- [ ] **步骤 5：提交**

运行：

```powershell
git add docs/plan/openapi.yaml docs/plan/08-contract-alignment.md docs/superpowers/plans/2026-05-30-zxp-contract-005-admin-governance.md
git commit -m "docs: add admin governance api contract"
```

预期：提交成功，提交内容只包含 M1 后台治理契约和计划文档，不包含后端、前端或 migration 改动。

## 自查

- 规格覆盖：本计划覆盖 `ZXP-CONTRACT-005` 的活动配置、SKU 绑定、标签任务保存/更新/执行/命中检查、订单列表/详情/退款、任务概览/列表/详情/执行/重试、DCC 配置、线程池配置、审计日志查询、管理员权限、写操作审计和 Mock 失败示例。
- 占位符扫描：计划未使用未决占位标记或“照抄上一任务”式表达；每个需要修改契约的步骤都给出具体字段或 YAML 形状。
- 类型一致性：计划中的 schema 名称与现有 `docs/plan/openapi.yaml` 保持一致；新增示例只使用现有 `ErrorResponse`、`ApiResponse`、`TaskStatus`、`OrderStatus`、`RefundSource` 等契约。
- 阶段边界：所有任务只修改 OpenAPI、契约对齐文档和本计划文件；M1 不创建后端 Java、前端 TypeScript 或 migration。
