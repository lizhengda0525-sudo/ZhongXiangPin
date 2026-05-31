# ZXP-BE-SKEL-002 统一响应和异常处理 收口记录

本文记录 ZXP-BE-SKEL-002 的实现完成情况、逐项验证结论和偏差说明。任务定义见 `docs/plan/06-task-implementation-checklist.md` 第 278 行。

## 1. 任务目标

在 `common` 和 `trigger` 模块实现统一响应信封和全局异常处理，使 Controller 不手动写 try/catch，错误响应格式稳定，响应自动注入 traceId。

## 2. 实现文件清单

| 模块 | 文件 | 职责 |
| --- | --- | --- |
| common | `common/error/ErrorCode.java` | 统一错误码枚举，每个值内含 code、message、httpStatus |
| common | `common/error/ErrorDetail.java` | 可安全返回前端的错误详情（Builder 模式），禁止承载堆栈、SQL、token 等敏感信息 |
| common | `common/exception/BizException.java` | 业务异常，携带 ErrorCode + 可覆盖的 responseMessage + ErrorDetail |
| common | `common/response/ApiResponse.java` | 统一响应信封 `code/message/data/traceId` |
| common | `common/response/PageResponse.java` | 统一分页结构 `pageNo/pageSize/total/items` |
| common | `common/trace/TraceIdContext.java` | ThreadLocal traceId 上下文，`currentOrCreate()` 懒生成 |
| trigger | `trigger/http/trace/TraceIdFilter.java` | Servlet Filter，从请求头 `X-Trace-Id` 读取或生成，写入 ThreadLocal + 响应头，finally 清理 |
| trigger | `trigger/http/handler/GlobalExceptionHandler.java` | `@RestControllerAdvice`，分发 BizException / 参数校验 / 兜底异常 |
| common (test) | `common/response/ApiResponseTest.java` | ApiResponse 单元测试 |
| trigger (test) | `trigger/http/handler/GlobalExceptionHandlerTest.java` | MockMvc 集成测试，覆盖三种异常场景 |

pom.xml 变更：`trigger/pom.xml` 增加 `spring-boot-starter-validation` 依赖。

## 3. 规格对照验证

| 规格要求 | 实现位置 | 验收结论 |
| --- | --- | --- |
| BizException 携带错误码 | `BizException.java:18-47` | 持有 ErrorCode 枚举，4 个重载构造器覆盖"只传码 / 码+消息 / 码+详情 / 码+消息+详情" |
| 全局异常处理返回统一 ApiResponse | `GlobalExceptionHandler.java:28-88` | `@RestControllerAdvice` 统一拦截，所有分支走 `buildErrorResponse()` → `ApiResponse.failure()`，信封四字段固定 |
| 参数校验错误返回字段级提示 | `GlobalExceptionHandler.java:47-70` | 两个 handler 分别处理 `MethodArgumentNotValidException`（@RequestBody）和 `ConstraintViolationException`（query/path），ErrorDetail 带 field + reason |
| 响应注入 traceId | `TraceIdFilter.java` + `TraceIdContext.java` + `ApiResponse.java` | Filter 设置 → Context 持有 → Response 构造时自动取值；Filter finally 清理 ThreadLocal |
| 日志不输出密码/验证码/token | `GlobalExceptionHandler.java:79-83` | 兜底 handler 只记录 `traceId` + `exceptionType`，不记录 `exception.getMessage()`；响应 data 返回 null |

## 4. 测试覆盖

| 测试方法 | 覆盖场景 | 关键断言 |
| --- | --- | --- |
| `handlesBizExceptionWithUnifiedBodyAndHttpStatus` | 业务异常 | HTTP 409、错误码 `TRADE_ORDER_STATUS_INVALID`、ErrorDetail 资源定位、traceId 回传、旧字段 info/msg 不存在 |
| `handlesBodyValidationExceptionWithFieldDetail` | @RequestBody 校验 | HTTP 400、`data.field` = `"name"`、`data.reason` = 校验消息 |
| `handlesSystemExceptionWithSafeBody` | 系统兜底 | HTTP 500、data 为 null、含 `password=secret` 的异常消息不泄漏到响应 |
| `successUsesMessageAndTraceId` (ApiResponseTest) | 响应信封构造 | code/message/data/traceId 字段值正确 |

## 5. 偏差与说明

| 项目 | 规格原文 | 实际实现 | 判断 |
| --- | --- | --- | --- |
| 目标模块 | `common`, `trigger/handler` | 实际在 `trigger/http/handler` 和 `trigger/http/trace` | 包路径微调，语义一致 |
| 参数校验多字段 | "字段级提示" | 当前只取第一条 fieldError / violation | 当前满足验收标准；如需一次性返回所有失败字段，后续可扩展为列表 |

## 6. 执行结果

| 项目 | 结论 |
| --- | --- |
| 统一响应信封 | 已完成，`code/message/data/traceId` 四字段固定，与 OpenAPI 契约一致 |
| 全局异常处理 | 已完成，Controller 零 try/catch，三种异常场景全部覆盖 |
| traceId 贯穿 | 已完成，Filter → ThreadLocal → 响应头 → 响应体全链路，finally 清理防串线 |
| 敏感信息防护 | 已完成，兜底 handler 不输出异常消息，响应 data 为 null |
| MockMvc 测试 | 已完成，覆盖业务错误 / 参数错误 / 系统错误三个场景 |
| 阶段边界 | 未修改 OpenAPI 契约，未创建领域层或应用层代码 |
