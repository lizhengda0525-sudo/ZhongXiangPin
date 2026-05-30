# ZXP-CONTRACT-003 会场与试算接口契约实施计划

> **给 agentic 执行者：** 必须使用子技能：推荐 `superpowers:subagent-driven-development`，也可以使用 `superpowers:executing-plans`。请按任务逐项执行，步骤使用复选框 `- [ ]` 追踪进度。

**目标：** 固定 M1 阶段的会场分页、活动详情和试算接口契约，让后续用户端 Mock、M5 会场后端能力、M6 交易锁单和 M2 migration 字段映射都复用同一份 OpenAPI 事实源。

**架构：** 本任务只修改契约文档，不创建后端 Java 模块、DTO、Controller、Service、Mapper 或 Repository。`docs/plan/openapi.yaml` 是接口路径、schema、错误响应和 examples 的单一事实源；`docs/plan/08-contract-alignment.md` 仅在字段映射或枚举说明需要补齐时更新。

**技术栈：** OpenAPI 3.0.3 YAML、Redocly CLI lint、PowerShell、仓库文档门禁。

---

## 文件边界

- 修改：`docs/plan/openapi.yaml`
  - 负责 `/api/v1/market/activities`、`/api/v1/market/activities/{activityId}`、`/api/v1/market/trials`。
  - 负责会场相关 schema：`MarketActivitySummary`、`MarketActivityDetail`、`TrialRequest`、`TrialResult`、`TeamSummary`、`ActivityConfig`、`GoodsSnapshot`、`PriceSnapshot`、`ActivitySnapshot`、`DiscountSnapshot`、`TagHitResult`。
  - 负责会场 examples 和可复用响应：`MarketActivityPageSuccess`、`TrialSuccess`、`MarketNotFound`，以及活动不可用、标签未命中、活动过期、无可参团队等失败或边界示例。
- 按需修改：`docs/plan/08-contract-alignment.md`
  - 只有 OpenAPI 字段或枚举映射需要补充 M2 migration、M9 前端类型说明时才修改。
- 只读：`docs/sdd/tasks.md`
  - 确认 M1 门禁：只做契约和 Mock 示例，不落地后端 Java。
- 只读：`docs/sdd/mock/strategy.md`
  - 确认会场必备 Mock 场景：普通活动、活动未开始、活动已结束、活动禁用、标签命中、标签未命中、无可参团队、缓存未命中后回填。
- 只读：`docs/plan/06-task-implementation-checklist.md`
  - 确认 ZXP-CONTRACT-003 的验收标准和建议提交信息。

## 范围护栏

- 不创建或修改 `backend/**`。
- 不创建 Java DTO、枚举、Controller、Service、Mapper、Repository 或后端测试。
- 不创建 `frontend/user-web` 或 `frontend/admin-web` 文件；本任务只准备前端后续所需的契约和 Mock 来源材料。
- 不修改 M2 migration 文件；如果发现字段映射缺口，只在任务总结中记录后续项，M1 不定义数据库 DDL。

### 任务 1：契约基线审计

**文件：**
- 读取：`docs/plan/openapi.yaml`
- 读取：`docs/plan/06-task-implementation-checklist.md`
- 读取：`docs/sdd/mock/strategy.md`

- [ ] **步骤 1：定位现有会场契约片段**

运行：

```powershell
rg -n "^  /api/v1/market|MarketActivity|Trial|MarketNotFound|MARKET_|ActivityStatus|TagHitResult|availableTeams" docs\plan\openapi.yaml
```

预期：输出包含三个会场路径、`MarketActivityPageApiResponse`、`MarketActivityDetailApiResponse`、`TrialApiResponse`、可复用会场示例、会场错误码，以及列表/详情/试算响应使用的 schema。

- [ ] **步骤 2：核对任务定义和 Mock 要求**

运行：

```powershell
rg -n "ZXP-CONTRACT-003|会场|试算|Mock 场景|标签未命中|无可参团队|活动过期" docs\plan\06-task-implementation-checklist.md docs\sdd\mock\strategy.md
```

预期：输出能确认 ZXP-CONTRACT-003 要求覆盖会场分页、商品详情或试算；列表返回商品、活动、拼团价、统计和 top teams；试算返回价格快照、活动快照、命中状态和可参团队；Mock 示例覆盖代表性成功和失败场景。

- [ ] **步骤 3：编辑前记录基线缺口**

在任务日志中记录以下检查表，不提交为仓库文件：

```text
ZXP-CONTRACT-003 基线审计：
- /api/v1/market/activities 是否存在：是/否
- /api/v1/market/activities/{activityId} 是否存在：是/否
- /api/v1/market/trials 是否存在：是/否
- 列表成功示例是否包含 goods/activity/price/statistics/topTeams：是/否
- 详情成功示例是否包含 goods/activity/price/statistics/topTeams：是/否
- 试算成功示例是否包含 trialNo/activityId/skuId/eligible/price snapshots/tagHit/availableTeams/calculatedAt：是/否
- 失败示例是否覆盖 MARKET_NOT_FOUND：是/否
- 失败示例是否覆盖 MARKET_TAG_NOT_MATCHED：是/否
- 失败示例是否覆盖 MARKET_ACTIVITY_UNAVAILABLE 或活动过期/暂停：是/否
- 示例是否覆盖无可参团队：是/否
- 是否发现 M1 阶段边界违规：是/否
```

预期：所有缺失项都成为任务 2 和任务 3 的输入。

### 任务 2：收紧会场路径和响应

**文件：**
- 修改：`docs/plan/openapi.yaml`

- [ ] **步骤 1：确保会场列表路径具备稳定查询参数和统一响应信封**

在 `docs/plan/openapi.yaml` 中，确保 `/api/v1/market/activities` 保持或调整为以下形状：

```yaml
  /api/v1/market/activities:
    get:
      tags:
        - 会场
      summary: 查询会场活动列表
      description: 一次返回商品、活动、拼团价、统计和可参团队摘要，避免前端自行拼装复杂业务数据。
      parameters:
        - $ref: "#/components/parameters/PageNo"
        - $ref: "#/components/parameters/PageSize"
        - $ref: "#/components/parameters/Source"
        - $ref: "#/components/parameters/Channel"
        - name: category
          in: query
          required: false
          description: 商品分类
          schema:
            type: string
            example: food
      responses:
        "200":
          description: 会场分页
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/MarketActivityPageApiResponse"
              examples:
                success:
                  $ref: "#/components/examples/MarketActivityPageSuccess"
        "401":
          $ref: "#/components/responses/Unauthorized"
```

- [ ] **步骤 2：确保活动详情路径返回详情和可参团队**

在 `docs/plan/openapi.yaml` 中，确保 `/api/v1/market/activities/{activityId}` 的响应结构保持或调整为：

```yaml
      responses:
        "200":
          description: 活动详情
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/MarketActivityDetailApiResponse"
              examples:
                success:
                  $ref: "#/components/examples/MarketActivityDetailSuccess"
        "404":
          $ref: "#/components/responses/MarketNotFound"
        "409":
          description: 活动不可用
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
              examples:
                activityUnavailable:
                  $ref: "#/components/examples/MarketActivityUnavailable"
```

如果 `MarketActivityDetailSuccess` 尚不存在，在 `components.examples` 下新增它，字段沿用当前详情成功示例中的完整 `data` 结构。

- [ ] **步骤 3：确保试算路径具备成功、标签未命中、活动不可用、无可参团队响应**

在 `docs/plan/openapi.yaml` 中，确保 `/api/v1/market/trials` 拥有以下响应集合：

```yaml
      responses:
        "200":
          description: 试算成功
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/TrialApiResponse"
              examples:
                success:
                  $ref: "#/components/examples/TrialSuccess"
                noAvailableTeams:
                  $ref: "#/components/examples/TrialNoAvailableTeams"
        "403":
          description: 当前用户不满足活动人群要求
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
              examples:
                tagNotMatched:
                  $ref: "#/components/examples/MarketTagNotMatched"
        "404":
          $ref: "#/components/responses/MarketNotFound"
        "409":
          description: 活动不可用或状态冲突
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
              examples:
                activityUnavailable:
                  $ref: "#/components/examples/MarketActivityUnavailable"
```

预期：客户端可以基于契约表达正常试算、符合资格但暂无队伍、标签未命中、活动不存在和活动不可用，不需要在本地发明响应形状。

### 任务 3：补齐会场 schema 和示例

**文件：**
- 修改：`docs/plan/openapi.yaml`

- [ ] **步骤 1：确保列表和详情 schema 暴露前端所需字段**

确保 `MarketActivitySummary` 至少包含以下要求，或已有等价结构：

```yaml
    MarketActivitySummary:
      type: object
      required:
        - activity
        - goods
        - price
        - statistics
        - topTeams
      properties:
        activity:
          $ref: "#/components/schemas/ActivityConfig"
        goods:
          $ref: "#/components/schemas/GoodsSnapshot"
        price:
          $ref: "#/components/schemas/PriceSnapshot"
        statistics:
          $ref: "#/components/schemas/MarketStatistics"
        topTeams:
          type: array
          items:
            $ref: "#/components/schemas/TeamSummary"
```

预期：用户端可以从单个列表项渲染活动卡片和详情入口，不需要自行推导价格、队伍和统计字段。

- [ ] **步骤 2：确保试算 schema 捕获快照和资格结果**

确保 `TrialResult` 包含以下字段：

```yaml
    TrialResult:
      type: object
      required:
        - trialNo
        - activityId
        - skuId
        - eligible
        - originalPriceCent
        - discountCent
        - payPriceCent
        - activitySnapshot
        - discountSnapshot
        - availableTeams
        - calculatedAt
      properties:
        trialNo:
          type: string
        activityId:
          type: integer
          format: int64
        skuId:
          type: integer
          format: int64
        eligible:
          type: boolean
        originalPriceCent:
          type: integer
          format: int64
        discountCent:
          type: integer
          format: int64
        payPriceCent:
          type: integer
          format: int64
        activitySnapshot:
          $ref: "#/components/schemas/ActivitySnapshot"
        discountSnapshot:
          $ref: "#/components/schemas/DiscountSnapshot"
        tagHit:
          $ref: "#/components/schemas/TagHitResult"
        availableTeams:
          type: array
          items:
            $ref: "#/components/schemas/TeamSummary"
        calculatedAt:
          type: string
          format: date-time
```

预期：M6 锁单可以保存试算证据和价格快照，不信任前端提交的金额字段。

- [ ] **步骤 3：新增或规范可复用会场 examples**

如缺失，在 `components.examples` 下按以下形状补齐稳定 ID 示例：

```yaml
    MarketActivityUnavailable:
      summary: 活动不可用
      value:
        code: MARKET_ACTIVITY_UNAVAILABLE
        message: 当前活动不可用
        traceId: trace-market-err-002
        data:
          activityId: 20001
          status: ENDED
    MarketTagNotMatched:
      summary: 标签未命中
      value:
        code: MARKET_TAG_NOT_MATCHED
        message: 当前用户不满足活动人群要求
        traceId: trace-market-err-001
        data:
          activityId: 20001
          tagId: TAG-VIP-001
    TrialNoAvailableTeams:
      summary: 试算成功但暂无可参团队
      value:
        code: "0000"
        message: 成功
        traceId: trace-market-trial-002
        data:
          trialNo: TRIAL-20260530-0002
          activityId: 20001
          skuId: 30001
          eligible: true
          originalPriceCent: 9900
          discountCent: 2000
          payPriceCent: 7900
          activitySnapshot:
            activityId: 20001
            skuId: 30001
            title: 端午粽子双人拼团
            source: APP
            channel: H5
            status: ACTIVE
            startTime: "2026-05-25T09:00:00+08:00"
            endTime: "2026-06-01T23:59:59+08:00"
            tagId: TAG-VIP-001
          discountSnapshot:
            discountType: DIRECT
            rule: 满团立减 20 元
          tagHit:
            tagId: TAG-VIP-001
            hit: true
            reason: 命中 VIP 活动人群
          availableTeams: []
          calculatedAt: "2026-05-30T10:00:00+08:00"
```

预期：所有必备 Mock 场景都能从 OpenAPI examples 复制，或至少能按同一 schema 手工校验。

- [ ] **步骤 4：确认错误码包含会场失败码**

确保 `ErrorCode` enum 包含：

```yaml
        - MARKET_NOT_FOUND
        - MARKET_ACTIVITY_UNAVAILABLE
        - MARKET_TAG_NOT_MATCHED
```

预期：会场失败使用稳定字符串错误码，HTTP 状态仍表达协议层语义。

### 任务 4：契约对齐检查

**文件：**
- 按需修改：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：检查字段映射覆盖度**

运行：

```powershell
rg -n "activityId|skuId|teamId|originalPriceCent|discountCent|payPriceCent|activitySnapshot|discountSnapshot|trialNo|trialTime" docs\plan\08-contract-alignment.md
```

预期：输出包含所有字段的数据库映射说明，或明确说明映射等待 M2 固定。

- [ ] **步骤 2：仅在缺失时补充试算字段映射说明**

如果映射表缺少 `trialNo` 或 `trialTime`，在关键字段映射章节加入：

```markdown
| `trialNo`, `trialTime` | `trade_order.trial_no`, `trade_order.trial_time` | 锁单时保存试算证据；M1 只固定契约字段，M2/M6 再落地持久化和业务校验。 |
```

预期：M2 和 M6 可以稳定地从试算契约衔接到数据库与订单实现。

### 任务 5：验证

**文件：**
- 读取：`docs/plan/openapi.yaml`
- 读取：`docs/plan/08-contract-alignment.md`

- [ ] **步骤 1：运行 OpenAPI lint**

运行：

```powershell
npx --yes @redocly/cli lint docs/plan/openapi.yaml
```

预期：通过，或仅存在与 ZXP-CONTRACT-003 无关的既有 warning。如果网络限制导致无法安装 Redocly CLI，按审批流程重跑；仍无法执行时，在任务总结中记录网络限制，并继续做手工 YAML 检查。

- [ ] **步骤 2：运行结构 grep 检查**

运行：

```powershell
rg -n "MarketActivityPageSuccess|MarketActivityDetailSuccess|TrialSuccess|TrialNoAvailableTeams|MarketActivityUnavailable|MarketTagNotMatched|MARKET_ACTIVITY_UNAVAILABLE|MARKET_TAG_NOT_MATCHED" docs\plan\openapi.yaml
```

预期：每个 example 和错误码至少出现一次。

- [ ] **步骤 3：确认 M1 边界**

运行：

```powershell
git status --short
```

预期：变更文件仅限 `docs/plan/openapi.yaml`、可选的 `docs/plan/08-contract-alignment.md`，以及本计划文件。不得出现新的后端 Java 文件或前端实现文件。

- [ ] **步骤 4：记录人工验收检查**

在最终任务总结中记录：

```text
契约对齐检查：
- OpenAPI lint：已执行/暂未执行，原因：...
- Mock 示例：已检查，覆盖：普通活动、标签未命中、活动不可用/过期、无可参团队
- 前端类型：M1 不生成，后续 M9/M10 根据 OpenAPI 生成或手写对齐
- migration 映射：已检查，涉及字段：activityId、skuId、teamId、price snapshots、trialNo/trialTime
- M1 边界：未创建后端 Java/Mapper/Repository/Service/Controller
```

### 任务 6：提交

**文件：**
- 暂存：`docs/plan/openapi.yaml`
- 如有修改则暂存：`docs/plan/08-contract-alignment.md`
- 暂存：`docs/superpowers/plans/2026-05-30-zxp-contract-003-market-trial.md`

- [ ] **步骤 1：审查 diff**

运行：

```powershell
git diff -- docs\plan\openapi.yaml docs\plan\08-contract-alignment.md docs\superpowers\plans\2026-05-30-zxp-contract-003-market-trial.md
```

预期：diff 只包含 ZXP-CONTRACT-003 的会场/试算契约变更和本实施计划。

- [ ] **步骤 2：暂存文件**

运行：

```powershell
git add docs/plan/openapi.yaml docs/plan/08-contract-alignment.md docs/superpowers/plans/2026-05-30-zxp-contract-003-market-trial.md
```

预期：文件已暂存。如果 `docs/plan/08-contract-alignment.md` 没有修改，改用：

```powershell
git add docs/plan/openapi.yaml docs/superpowers/plans/2026-05-30-zxp-contract-003-market-trial.md
```

- [ ] **步骤 3：提交**

运行：

```powershell
git commit -m "docs: add market and trial api contract"
```

预期：形成一个只服务于 ZXP-CONTRACT-003 的小提交。

## 自检结果

- 规格覆盖：ZXP-CONTRACT-003 的列表、详情、试算端点，成功示例，代表性失败示例，后端 session 用户身份边界，以及 M1 不创建 Java 的阶段边界，均已由任务 1 到任务 5 覆盖。
- 占位检查：未保留未完成标记或无边界的实现指令。
- 类型一致性：路径统一使用 `/api/v1/market/activities`、`/api/v1/market/activities/{activityId}`、`/api/v1/market/trials`；schema 统一使用 `MarketActivity*`、`Trial*`、`TeamSummary`、`TagHitResult` 和会场错误码。
