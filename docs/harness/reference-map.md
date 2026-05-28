# Harness 参考索引

使用本索引快速定位旧项目中的行为依据。下列路径只用于参考，新实现只落在 `D:\JAVA\ZXP\zhongxiangpin`。

## 后端 API 与领域行为

| 新任务范围 | 旧项目参考文件 | 重点提取行为 |
| --- | --- | --- |
| 认证契约与实现 | `D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\auth\AuthController.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\auth\service\impl\AuthServiceImpl.java` | 注册、密码登录、验证码登录、当前用户、登出、角色/状态校验、session 过期时间。 |
| 认证拦截器 | `D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\interceptor\RefreshTokenInterceptor.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\interceptor\LoginInterceptor.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\interceptor\AdminInterceptor.java` | token 刷新、强制登录、管理员角色校验、ThreadLocal 清理。 |
| 会场与试算 | `D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\market\MarketController.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\market\service\impl\MarketServiceImpl.java` | 活动列表、商品详情/试算、折扣策略、标签命中、缓存行为、可参团队伍。 |
| 标签治理 | `D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\tag\service\impl\AdminTagServiceImpl.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-infrastructure\src\main\java\com\zxp\zhongxiangpin\infrastructure\tag\dao\mapper\TagMapper.java` | 标签规则、批次执行、命中检查、重试、currentBatchId 语义。 |
| 交易锁单/支付/退款 | `D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\trade\TradeController.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\trade\service\impl\TradeServiceImpl.java` | 开团/参团、锁单、支付结算、退款、幂等、状态流转。 |
| 运行态修复 | `D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\job\TradeRuntimeScheduler.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\trade\service\impl\TradeRuntimeServiceImpl.java` | 超时关单、队伍过期、自动退款、通知任务、补偿任务、运行态巡检。 |
| 订单查询 | `D:\ZXP\zhongxiangpin\zhongxiangpin-trigger\src\main\java\com\zxp\zhongxiangpin\trigger\http\order\OrderController.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\order\service\impl\OrderServiceImpl.java` | 当前用户订单列表/详情、详情字段形态、支付/退款/队伍展示字段。 |
| 后台活动/订单/任务 | `D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\admin\service\impl\AdminActivityServiceImpl.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\admin\service\impl\AdminOrderServiceImpl.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\admin\service\impl\AdminTaskGovernanceServiceImpl.java` | 后台查询、编辑、退款、任务执行/重试、操作者和审计要求。 |
| DCC 与线程池 | `D:\ZXP\zhongxiangpin\zhongxiangpin-domain\src\main\java\com\zxp\zhongxiangpin\domain\dcc\service\impl\DccGovernanceServiceImpl.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-infrastructure\src\main\java\com\zxp\zhongxiangpin\infrastructure\admin\service\AdminThreadPoolGovernanceServiceImpl.java` | 运行时配置读取/更新、参数校验、线程池当前指标。 |

## 持久化与运行态

| 范围 | 旧项目参考文件 | 重点提取行为 |
| --- | --- | --- |
| 用户持久化 | `D:\ZXP\zhongxiangpin\zhongxiangpin-infrastructure\src\main\java\com\zxp\zhongxiangpin\infrastructure\auth\dao\mapper\UserAccountMapper.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-app\src\main\resources\mybatis\mapper\UserAccountMapper.xml` | 唯一键、账号状态、角色、密码哈希字段。 |
| 会场持久化 | `D:\ZXP\zhongxiangpin\zhongxiangpin-infrastructure\src\main\java\com\zxp\zhongxiangpin\infrastructure\market\dao\mapper\MarketMapper.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-app\src\main\resources\mybatis\mapper\MarketMapper.xml` | 商品/活动查询、统计、队伍查询、缓存回填字段。 |
| 标签持久化 | `D:\ZXP\zhongxiangpin\zhongxiangpin-infrastructure\src\main\java\com\zxp\zhongxiangpin\infrastructure\tag\dao\mapper\TagMapper.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-app\src\main\resources\mybatis\mapper\TagMapper.xml` | 批次明细、标签命中查询、任务状态更新。 |
| 交易持久化 | `D:\ZXP\zhongxiangpin\zhongxiangpin-infrastructure\src\main\java\com\zxp\zhongxiangpin\infrastructure\trade\dao\mapper\TradeMapper.java`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-app\src\main\resources\mybatis\mapper\TradeMapper.xml` | 条件更新、幂等唯一键、状态更新、运行态修复查询。 |

## 前端 Harness

| 新任务范围 | 旧项目参考文件 | 重点提取行为 |
| --- | --- | --- |
| 用户端壳层 | `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\router.ts`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\api\http.ts`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\stores\auth.ts` | 路由守卫、登录过期、请求封装、token 存储。 |
| 用户端会场流程 | `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\GoodsHomeView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\GoodsDetailView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\api\user.ts` | 会场列表、详情/试算、可参团队伍、空态/错误态/加载态。 |
| 用户端结算/支付/订单 | `D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\CheckoutView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\PayResultView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\OrderListView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-user-web\src\views\OrderDetailView.vue` | clientOrderNo 生成、重复点击保护、支付/退款动作、订单状态展示。 |
| 管理端壳层 | `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\router.ts`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\api\http.ts`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\stores\auth.ts` | 管理员登录守卫、请求封装、权限失败处理。 |
| 管理端治理页面 | `D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\ActivityView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\TagView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\OrderView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\TaskView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\DccView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\ThreadPoolView.vue`<br>`D:\ZXP\zhongxiangpin\zhongxiangpin-admin-web\src\views\DashboardView.vue` | 表单字段、类型化 API 需求、危险操作确认、治理仪表盘。 |

## 可复用的测试证据思路

旧项目的测试报告位于各模块 `target\surefire-reports` 下。可以用它们识别已覆盖行为，再在新项目中重新编写测试。不要复制生成的报告文件。

重点参考旧测试范围：

- 认证 Controller 和拦截器测试。
- 会场 Service 和 Controller 测试。
- 交易 Service、运行态 Service 和 Mapper 契约测试。
- 后台活动、订单、标签、任务 Controller 和 Service 测试。
- 动态线程池和 DCC 测试。
- 交易状态和 Redis 常量契约测试。
