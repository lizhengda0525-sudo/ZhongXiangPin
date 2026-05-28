# 管理端 SDD

## 目标

构建 Vue 3 管理端 Web 应用，用于支撑 P0 后台治理：活动配置、标签任务、订单查询与退款、可靠事件治理、DCC 配置、线程池运行态、审计日志和治理仪表盘。

管理端是操作台，不做营销落地页，不新增复杂前端架构。页面优先围绕查询、表单、表格、抽屉、弹窗和明确反馈组织。

## 实现目标

```text
frontend/admin-web/
  src/api/
  src/router/
  src/stores/
  src/views/
  src/components/
```

必要能力：

- 管理员登录态和路由守卫。
- 类型化 API 客户端，类型与 `docs/plan/openapi.yaml` 对齐。
- 后台治理页面的加载态、空态、错误态和写操作反馈。
- Mock 与真实 HTTP 可切换，页面逻辑不因切换重写。

## 必读输入

- `docs/plan/openapi.yaml`
- `docs/plan/02-business-design.md`
- `docs/plan/05-validation.md`
- `docs/plan/06-task-implementation-checklist.md`
- `docs/plan/07-development-standard.md`
- `docs/harness/reference-map.md`
- `docs/sdd/mock/strategy.md`

## Harness 参考

- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\router.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\api\http.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\api\admin.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\stores\auth.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\ActivityView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\TagView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\OrderView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\TaskView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\DccView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\ThreadPoolView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\DashboardView.vue`

Harness 只用于提取路由、表单字段、接口调用形态、错误处理和危险操作确认方式，不复制旧组件源码。

## UI 原则

- 管理端第一屏进入治理仪表盘或订单/任务概览，避免首页空转。
- 页面密度适中，优先使用表格、筛选区、分页、抽屉详情和确认弹窗。
- 不新增隐式 `any`，请求和响应类型集中在 API 层或生成类型中。
- 所有写操作必须展示成功或失败结果；失败提示包含后端 `message`，调试时保留 `traceId`。
- 危险操作必须二次确认；退款、任务执行、任务重试、DCC 更新、线程池参数更新必须填写或展示操作原因。
- 管理端只提交业务操作意图，不在前端绕过后端权限、状态机、幂等或审计。

## 路由

| 路由 | 视图 | 用途 | 鉴权 |
| --- | --- | --- | --- |
| `/login` | `LoginView` | 管理员登录 | 公开 |
| `/dashboard` 或 `/` | `DashboardView` | 治理指标和快捷入口 | 管理员 |
| `/activities` | `ActivityView` | 活动列表、保存、启停、SKU 绑定 | 管理员 |
| `/tags` | `TagView` | 标签任务、执行、命中检查 | 管理员 |
| `/orders` | `OrderView` | 订单查询、详情、管理员退款 | 管理员 |
| `/tasks` | `TaskView` | 可靠事件概览、列表、详情、执行、重试 | 管理员 |
| `/dcc` | `DccView` | DCC 配置读取和更新 | 管理员 |
| `/thread-pools` | `ThreadPoolView` | 线程池指标和参数调整 | 管理员 |
| `/audit-logs` | `AuditLogView` | 审计日志查询 | 管理员 |

路由守卫必须在进入受保护页面前确认当前用户存在且具备 `ADMIN` 角色。401 跳转登录，403 保留当前页面并提示权限不足。

## API 客户端

请求封装必须处理：

- 基础 URL 来自环境变量。
- 统一 `ApiResponse<T>` 信封和分页结构。
- token/session header 注入。
- 401、403、业务错误和网络错误的统一处理。
- 写操作防重复提交。
- traceId 记录，便于和后端日志、审计记录对齐。

管理端 API 至少覆盖：

| 能力 | OpenAPI 端点 |
| --- | --- |
| 订单治理 | `/api/v1/admin/orders`、`/api/v1/admin/orders/{orderId}`、`/api/v1/admin/orders/{orderId}/refunds` |
| 任务治理 | `/api/v1/admin/events/overview`、`/api/v1/admin/events`、`/api/v1/admin/events/{eventId}`、执行、重试端点 |
| 活动治理 | `/api/v1/admin/activities`、`/api/v1/admin/activities/{activityId}`、`/api/v1/admin/activities/{activityId}/sku-bindings` |
| 标签治理 | `/api/v1/admin/tag-jobs`、`/api/v1/admin/tag-jobs/{tagId}/{batchId}`、执行、命中检查端点 |
| 配置治理 | `/api/v1/admin/dcc/configs`、`/api/v1/admin/thread-pools` |
| 审计查询 | `/api/v1/admin/audit-logs` |

## 状态

| Store | 职责 |
| --- | --- |
| `auth` | 当前管理员、token/session、登录/登出/me 动作和角色校验。 |
| 页面级状态 | 筛选条件、分页、表单、详情抽屉和当前操作状态。 |
| 可选 `adminShell` | 菜单折叠、当前导航、全局错误提示等轻量壳层状态。 |

除登录态和壳层状态外，不提前引入复杂全局状态。列表筛选、详情和表单状态优先放在页面内。

## 页面要求

### 登录

- 复用认证契约，登录后调用当前用户接口确认角色。
- 非管理员登录成功也不能进入后台，应提示权限不足并清理后台会话状态。

### 仪表盘

- 展示今日订单、已支付订单、退款数量、待处理事件、失败事件等 P0 指标。
- 如果后端提供运行态不一致数量、缓存命中率等指标，可作为 P1 展示，不阻塞 P0。
- 指标卡片可以跳转到订单或任务页面，并带入筛选条件。

### 活动治理

- 支持分页查询、创建、更新、启停和 SKU 绑定。
- 表单字段与 OpenAPI 的 `AdminActivitySaveRequest` 对齐。
- 时间范围、成团人数、有效期、限购次数、折扣和标签绑定需要前端基础校验，后端仍是最终校验者。
- 保存成功后刷新列表或当前详情；失败时保留用户输入。

### 标签治理

- 支持标签任务列表、创建、更新、执行和指定用户命中检查。
- 规则类型至少覆盖 `USERS` 和 `PARTICIPATE_COUNT`。
- 执行结果展示 `batchId`、命中人数和失败原因。
- 重复执行或失败重试以后台响应为准，不在前端假定任务一定成功。

### 订单治理

- 支持按订单号、队伍号、用户、状态和时间筛选。
- 详情展示订单、团队、商品、价格快照、支付记录和退款记录。
- 管理员退款必须填写原因并二次确认；重复退款展示稳定的幂等结果。
- 普通用户越权或权限过期时必须按 401/403 统一处理。

### 任务治理

- 展示事件概览、事件列表、详情、手动执行和重试。
- 详情展示 `payload`、`retryCount`、`nextExecuteTime`、`lastError`、锁信息和状态。
- 手动执行和重试都必须二次确认；执行中禁用重复点击。
- 任务状态文案集中映射，不在页面散落魔法字符串。

### DCC 与线程池

- DCC 支持配置列表、单项更新、描述和最近更新时间展示。
- 线程池展示 active count、queue size、core/max size、rejected count 等运行态字段。
- 参数更新必须做基础范围校验，后端仍是最终校验者。
- 更新失败时不修改页面中的已生效值。

### 审计日志

- 支持按操作者、目标类型、目标 ID、操作结果和时间查询。
- 审计详情至少能看到 action、before/after 摘要、reason、traceId 和创建时间。
- 审计只读，不提供前端删除入口。

## Mock 模式

后端端点尚未完成前，可以使用前端内存 Mock，但必须满足：

- Mock 数据形状与 `docs/plan/openapi.yaml` 中的 schema 和 examples 一致。
- Mock API 与真实 HTTP API 使用同一套 TypeScript 类型。
- Mock 写操作需要更新内存状态，以验证列表刷新、详情变更和错误提示。
- Mock 场景至少覆盖：活动保存失败、标签执行失败、订单退款重复、任务重试失败、DCC 非法值、线程池非法值、401、403。
- 从 Mock 切换到真实 HTTP 只改数据源或环境变量，不改页面业务逻辑。

## 验证

| 阶段 | 证据 |
| --- | --- |
| 壳层 | `npm run type-check` 和 `npm run build` 通过。 |
| Mock | 活动、标签、订单、任务、DCC、线程池、审计和仪表盘页面可基于 Mock 数据完成主要操作。 |
| 后端联调 | 管理端连接本地后端后，写操作产生审计记录，列表和详情能刷新到真实结果。 |
| 回归 | 401、403、空列表、接口失败、重复点击、危险操作取消和确认均已验证。 |

## 管理端 P0 完成标准

- 管理员可以登录并访问后台受保护页面。
- 管理员可以配置活动、执行标签任务、查询订单、发起管理员退款、查看和重试任务。
- 管理员可以读取和更新 DCC、查看和调整线程池参数、查询审计日志。
- 治理仪表盘可以展示 P0 所需订单和任务概览，并能跳转到对应页面。
- 所有 API 请求/响应类型与 OpenAPI 对齐，不新增隐式 `any`。
- 构建通过，主要 Mock 场景和后端联调路径有验证记录。
