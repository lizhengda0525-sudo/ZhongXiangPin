# AGENT.md

本文是 `D:\JAVA\ZXP\zhongxiangpin` 的开发协作入口，用于管理上下文、约束开发流程和指向具体规范文档。本文保持轻量，不承载所有实现细节。

## 1. 项目边界

新项目工作区：

```text
D:\JAVA\ZXP\zhongxiangpin
```

已有项目参考源：

```text
D:\ZXP\zhongxiangpin
```

开发时必须基于已有项目理解业务行为、接口形态、数据模型和工程经验，但新项目独立开发，不复制旧项目源码，不在旧项目中继续叠加功能。

## 2. 上下文读取顺序

开始任何任务前，按需读取以下文档：

1. `AGENT.md`：确认协作规则和上下文边界。
2. `README.md`：确认项目定位和仓库说明。
3. `docs/README.md`：确认文档地图、各文档职责和开发阅读流。
4. `docs/plan/06-task-implementation-checklist.md`：确认当前任务编号、范围和验收标准。
5. `docs/sdd/tasks.md`：确认任务就绪门禁、依赖顺序和 Agent 分工。
6. 当前任务对应 SDD：后端读 `docs/sdd/backend/spec.md`，用户端读 `docs/sdd/frontend-user/spec.md`，管理端读 `docs/sdd/frontend-admin/spec.md`，Mock 读 `docs/sdd/mock/strategy.md`。
7. `docs/harness/reference-map.md`：确认旧项目参考文件。
8. `docs/plan/openapi.yaml`：确认接口契约。
9. `docs/plan/07-development-standard.md`：确认阿里巴巴 Java 开发手册、注释和质量门禁要求。
10. `docs/plan/08-contract-alignment.md`：当任务影响 OpenAPI、Mock、前端类型或 migration 状态字段时，确认契约对齐规则。
11. 对应专题文档：架构、业务、Git、路线图或验证计划。

不要一次性把所有细节塞进当前上下文。只读取当前任务需要的文档和旧项目代码。

## 3. 旧项目参照规则

旧项目用于参照：

- 认证：注册、密码登录、验证码登录、Redis session、管理员权限。
- 会场：多商品列表、活动试算、折扣策略、标签命中、缓存治理。
- 交易：开团、参团、锁单、支付、退款、订单查询、状态推进。
- 运行态：超时关单、队伍过期、补偿任务、通知任务、Redis/MySQL 巡检。
- 前端：用户端和管理端页面流程、路由结构、接口调用方式。

旧项目不得用于：

- 直接复制源码。
- 替代新项目的 OpenAPI 契约。
- 绕过新项目分层、测试和 Git 规范。

## 4. 文档语言规则

- 本项目所有文档必须使用中文撰写，包括 `README.md`、`AGENT.md`、`docs/**/*.md`、OpenAPI 中的 `description` 和示例说明。
- Harness、SDD、OpenAPI、Mock、API、DTO、CI、PR、P0、tag、profile、traceId、clientOrderNo 等行业术语、协议名、字段名、路径、类名、命令和任务编号可以保留英文或原始大小写。
- 新增或修改文档时，如果引用英文资料，必须用中文转述；除接口字段、代码片段、命令输出外，不保留整段英文说明。
- 如果发现已有文档出现英文段落或乱码，优先修复文档可读性，再继续实现任务。

## 5. 开发流程

每个任务按以下顺序推进：

1. 明确任务编号和验收标准。
2. 写清当前假设；如果存在多个解释或范围不清，先向用户确认。
3. 查阅 SDD 和 Harness 参考索引，确认任务达到就绪门禁。
4. 查阅旧项目对应实现，提取业务行为、隐性约束和风险点。
5. 更新或确认 OpenAPI、错误码、状态枚举和数据模型。
6. 如果任务影响 API、Mock、前端类型或 migration 字段，确认 `docs/plan/08-contract-alignment.md` 的对齐要求。
7. 确认 `docs/plan/07-development-standard.md` 对当前任务的约束，尤其是阿里巴巴 Java 开发手册和关键注释要求。
8. 实现后端、前端或部署文件。
9. 补充必要测试或验收记录。
10. 检查 Git diff，确保变更聚焦。
11. 用清晰提交信息记录增量。

详细任务拆解见：`docs/plan/06-task-implementation-checklist.md`。

## 6. Karpathy 风格开发约束

本项目采用 `multica-ai/andrej-karpathy-skills` 的核心思想作为 Agent 开发约束，但不照搬外部文件。落地规则如下：

1. 先想清楚再编码：不要静默假设；发现歧义、冲突或缺少信息时，先说明并确认。
2. 简单优先：只实现当前任务需要的可验证增量，不为未来可能性提前做抽象、配置或扩展。
3. 精准改动：只修改与任务直接相关的文件和代码；不顺手重构、不格式化无关文件、不删除既有死代码，除非用户明确要求。
4. 目标驱动：把任务转成可验收目标；修 bug 先复现或说明复现路径，新增能力要给出测试、构建、Mock 或手工验收证据。
5. 持续收敛：如果实现过程发现 200 行能写成 50 行，应主动简化；每个 changed line 都应能追溯到任务目标、SDD、OpenAPI 或 Harness 证据。

这些规则的优先级低于项目业务边界和安全约束，但高于个人偏好的代码风格调整。

## 7. Java 开发规范与注释要求

后端 Java 代码必须遵守 `docs/plan/07-development-standard.md`：

- 以阿里巴巴 Java 开发手册和 P3C 规约为基准，覆盖命名、常量、集合、并发、异常、日志、单元测试、安全、MySQL 和工程结构。
- 所有文本文件统一使用 UTF-8；除 `.bat`、`.cmd` 外统一 LF 换行；遵守 `.editorconfig` 和 `.gitattributes`。
- public 边界、Application 用例入口、Domain 规则/状态机、Repository port、Controller endpoint、Scheduler 和任务 handler 必须具备 Javadoc 或必要块注释。
- 状态机、幂等、事务、并发控制、Redis/MySQL 一致性、补偿重试、权限和审计边界必须写清业务意图、前置条件、异常和重复执行行为。
- 普通 DTO、简单 getter/setter、简单字段映射和无业务逻辑配置属性不强制详细注释；不得用逐行翻译代码的空注释充数。
- 每次后端任务总结必须说明开发规范检查结果：阿里巴巴 Java 开发手册是否适用、注释完整性、编码格式、静态扫描或替代验证是否执行。

## 8. 编码模式总则

后端遵循：

```text
trigger -> application -> domain
infrastructure -> domain/application port
app -> trigger + infrastructure
```

核心约束：

- `domain` 不依赖 Web、MyBatis、Redis 或 Controller DTO。
- Controller 只接收请求和返回响应，不写业务规则。
- Application 层负责用例编排和事务边界。
- Infrastructure 层只做技术适配，不承载业务决策。
- 用户身份以后端 session 为准，前端传入的 `userId` 不可信。
- 金额以后端试算为准，订单必须保存价格快照。
- Redis 是缓存和运行态，MySQL 是交易事实源。

更细规范见：`docs/plan/01-architecture.md`、`docs/plan/02-business-design.md`。

## 9. 前端开发总则

- 用户端放在 `frontend/user-web`。
- 管理端放在 `frontend/admin-web`。
- API 类型必须和 OpenAPI 契约一致。
- 不新增隐式 `any`。
- 登录过期、错误提示、加载态、空态和危险操作确认要统一处理。

详细页面和接口任务见：`docs/plan/06-task-implementation-checklist.md`。

## 10. Git 与验证

- `main` 保持可构建、可回滚。
- 每个任务保持小提交，提交信息使用 Conventional Commits。
- 里程碑使用 tag 管理。
- 后端任务至少说明单测、接口测试或手工验证结果。
- 前端任务至少说明类型检查、构建或页面验证结果。

详细规范见：`docs/plan/03-git-versioning.md`、`docs/plan/05-validation.md`。

## 11.停止与升级条件


当出现以下情况时，Agent 应停止自动推进并请求用户澄清或决策：
- 新旧项目行为存在不可兼容差异且无明确优先规则
- 当前任务需要修改 OpenAPI 契约但未在任务描述中授权
- 需要新增 infrastructure 依赖（如新中间件、新第三方SDK）
- 任务涉及金额/支付/退款路径，但测试验证无法通过（需人工复核）


## 12. Agent 工作要求

后续 Agent 参与本项目时：

- 先确认当前任务属于哪个文档和编号。
- 只加载当前任务必要上下文，避免上下文拥挤。
- 不修改 `D:\ZXP\zhongxiangpin`，除非用户明确要求。
- 不复制旧项目源码到新项目。
- 对非平凡任务先列出成功标准和验证方式；简单任务可直接执行。
- 每个实现任务都附上 Harness 证据：旧文件、提取行为、隐性约束、新实现决策和偏差。
- 影响 API、Mock、前端类型或 migration 字段的任务，都附上契约对齐检查结果。
- 每个后端实现任务都附上开发规范检查：阿里巴巴 Java 开发手册适用情况、注释完整性、编码格式、静态扫描或替代验证结果。
- 新增或修改文档时遵守“文档语言规则”，默认使用中文。
- 修改前说明准备改哪些文件。
- 修改后说明变更内容、验证结果和剩余风险。
- 如果发现 README、AGENT 或规划文档职责混乱，优先整理文档边界。
