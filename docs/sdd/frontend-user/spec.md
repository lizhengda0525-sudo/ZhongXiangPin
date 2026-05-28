# 用户端 SDD

## 目标

构建 Vue 3 用户端 Web 应用，用于演示完整 P0 拼团流程：登录、浏览会场、试算、开团/参团、锁单、Mock 支付、订单查询和退款。

## 实现目标

```text
frontend/user-web/
  src/api/
  src/router/
  src/stores/
  src/views/
  src/components/
```

## 必读输入

- `docs/plan/openapi.yaml`
- `docs/plan/02-business-design.md`
- `docs/plan/05-validation.md`
- `docs/harness/reference-map.md`
- `docs/sdd/mock/strategy.md`

## Harness 参考

- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\router.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\api\http.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\api\user.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\stores\auth.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\stores\trade.ts`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\LoginView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\GoodsHomeView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\GoodsDetailView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\CheckoutView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\PayResultView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\OrderListView.vue`
- `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\OrderDetailView.vue`

## UI 原则

- 登录后第一屏就是可用应用，不做营销落地页。
- 只使用类型化 API 调用，不新增隐式 `any`。
- 每个页面都有加载态、空态和错误态。
- 危险操作，尤其退款，必须二次确认。
- 不把 `userId` 当作归属证明发送或使用，后端登录态才可信。
- 请求进行中禁用重复操作按钮。

## 路由

| 路由 | 视图 | 用途 | 鉴权 |
| --- | --- | --- | --- |
| `/login` | `LoginView` | 登录、注册、验证码登录 | 公开 |
| `/` 或 `/goods` | `GoodsHomeView` | 会场列表 | 需要登录 |
| `/goods/:skuId` | `GoodsDetailView` | 试算和可参团队伍 | 需要登录 |
| `/checkout` | `CheckoutView` | 确认开团或参团订单 | 需要登录 |
| `/pay-result/:orderId` | `PayResultView` | Mock 支付和结果展示 | 需要登录 |
| `/orders` | `OrderListView` | 当前用户订单 | 需要登录 |
| `/orders/:orderId` | `OrderDetailView` | 订单详情和退款 | 需要登录 |

## API 客户端

请求封装必须处理：

- 基础 URL 来自环境变量。
- 如果后端使用 token 模式，则注入 token/session header。
- 统一 `ApiResponse<T>` 信封。
- 401 跳转登录。
- 业务错误提示。
- traceId 日志记录，便于调试。

## 状态

| Store | 职责 |
| --- | --- |
| `auth` | 当前用户、token/session 状态、登录/登出/me 动作。 |
| `market` 或页面级状态 | 会场列表、分类/分页筛选、当前试算结果。 |
| `trade` | 结算意图、选中的队伍、生成的 `clientOrderNo`、待支付订单。 |

## 页面要求

### 登录

- 支持密码登录和注册流程。
- 后端契约就绪后可支持验证码登录。
- 登录后加载 `/api/v1/auth/me`，再进入会场。

### 会场首页

- 展示商品/活动列表，包括拼团价、原价、成团人数、进度和状态。
- 根据契约支持分页或加载更多。
- 空态要区分无数据、鉴权失败和网络失败。

### 商品详情

- 进入页面时调用试算 API。
- 展示价格快照、活动快照、标签命中结果、限购结果和可参团队伍。
- 用户可以选择开团或加入已有队伍。

### 确认订单

- 每次结算意图只生成一次 `clientOrderNo`。
- 参团时才发送 `teamId`。
- 不发送可信 `userId`。
- 锁单成功后跳转到支付/结果流程。

### 支付结果

- 为 Mock 支付生成或接收 `payNo`。
- 重复点击要禁用；重复 API 响应要展示为稳定成功或明确的幂等状态。

### 订单

- 后端支持时，列表可按状态筛选。
- 详情展示价格快照、团队进度、支付记录和退款记录。
- 退款需要二次确认，并生成 `refundNo`。

## Mock 模式

后端端点尚未完成前，可以使用 Mock 数据，但必须满足：

- Mock 形状与 `docs/plan/openapi.yaml` 示例一致。
- Mock 数据隔离在 `src/mock` 或 `docs/sdd/mock` 生成物中。
- 每个 Mock API 都有清晰的真实 HTTP 切换路径。

## 验证

| 阶段 | 证据 |
| --- | --- |
| 壳层 | `npm run type-check` 和 `npm run build` 通过。 |
| Mock | 登录、会场、详情、结算、支付、订单和退款页面可以基于契约形状 Mock 数据运行。 |
| 后端联调 | 完整用户流程可以连接本地后端运行。 |
| 回归 | 401 跳登录、重复锁单点击、重复支付点击、退款确认和空态已验证。 |

## 用户端 P0 完成标准

- 用户可以完成登录 -> 会场 -> 详情/试算 -> 开团/参团 -> 锁单 -> Mock 支付 -> 订单详情 -> 退款。
- 所有 API 请求/响应类型与 OpenAPI 对齐。
- 不新增隐式 `any`。
- 构建通过。
