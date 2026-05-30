# AGENTS.md

本文是 `D:\JAVA\ZXP\zhongxiangpin` 的开发协作入口，用于管理上下文、约束开发流程和指向具体规范文档。本文保持轻量，不承载任务清单、完整规范或实现细节。

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

## 2. 上下文读取策略

`AGENTS.md` 只负责说明协作边界和入口规则。详细文档职责、阅读流和更新规则统一维护在 `docs/README.md`；任务编号、范围和验收标准统一维护在 `docs/plan/06-task-implementation-checklist.md`。

开始任务前，固定读取：

1. `AGENTS.md`：确认协作规则、工作区边界和停止条件。
2. `README.md`：确认项目定位、当前阶段和目录说明。
3. `docs/README.md`：按文档地图决定后续只读取哪些任务相关文档。

进入具体任务后，按需读取：

- `docs/plan/06-task-implementation-checklist.md`：确认任务编号、范围和验收标准。
- `docs/sdd/tasks.md`：确认就绪门禁、依赖顺序和 Agent 分工。
- 当前任务对应 SDD：后端、用户端、管理端或 Mock。
- `docs/harness/reference-map.md`：确认旧项目只读参考文件。
- `docs/plan/openapi.yaml`：当任务影响接口契约时读取。
- `docs/plan/08-contract-alignment.md`：当任务影响 API、Mock、前端类型或 migration 字段时读取。
- `docs/plan/07-development-standard.md`：后端任务或工程质量任务必须读取。
- 对应专题文档：架构、业务、Git、路线图或验证计划。

如果用户没有给出明确任务编号，Agent 只做定位和建议，不直接选择任务实现；应先说明可能对应的任务编号、假设和验证方式，请用户确认后再推进。不要一次性把所有细节塞进当前上下文，只读取当前任务需要的文档和旧项目代码。

阶段边界必须先确认：

- M1 只固定 OpenAPI、错误码、状态枚举、Mock 示例和字段映射，不创建后端 Java 模块。
- M2 只固定 migration、表结构、唯一键、索引、状态持久化和 MySQL 交易事实源，不创建 Mapper、Repository 或领域代码。
- M3 才开始创建后端 Maven 多模块、统一响应、异常处理、基础配置和健康检查。

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

- 本项目所有文档必须使用中文撰写，包括 `README.md`、`AGENTS.md`、`docs/**/*.md`、OpenAPI 中的 `description` 和示例说明。
- Harness、SDD、OpenAPI、Mock、API、DTO、CI、PR、P0、tag、profile、traceId、clientOrderNo 等行业术语、协议名、字段名、路径、类名、命令和任务编号可以保留英文或原始大小写。
- 新增或修改文档时，如果引用英文资料，必须用中文转述；除接口字段、代码片段、命令输出外，不保留整段英文说明。
- 如果发现已有文档出现英文段落或乱码，优先修复文档可读性，再继续实现任务。

## 5. 开发流程

每个任务按以下摘要流程推进：

1. 确认任务编号、成功标准和验证方式；范围不清时先说明假设并请求确认。
2. 按 `docs/sdd/tasks.md` 检查就绪门禁，按 `docs/harness/reference-map.md` 提取旧项目行为证据。
3. 先确认事实源：任务清单、OpenAPI、migration、SDD、契约对齐规则和开发规范。
4. 在当前阶段边界内实现最小可验证增量，不提前落地后续阶段代码。
5. 补充必要测试、Mock 检查、构建或手工验收记录。
6. 检查 Git diff，确保变更聚焦，并在总结中说明变更内容、验证结果和剩余风险。

详细任务拆解、完整推进顺序和建议提交见 `docs/plan/06-task-implementation-checklist.md`；任务门禁、漂移控制和 PR 检查见 `docs/sdd/tasks.md`。

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

## 11. 停止与升级条件

当出现以下情况时，Agent 应停止自动推进并请求用户澄清或决策：

- 新旧项目行为存在不可兼容差异且无明确优先规则
- 当前任务需要修改 OpenAPI 契约但未在任务描述中授权
- 需要新增 infrastructure 依赖（如新中间件、新第三方SDK）
- 任务涉及金额/支付/退款路径，但测试验证无法通过（需人工复核）

## 12. Agent 工作要求

后续 Agent 参与本项目时：

- 把 `AGENTS.md` 当作协作入口，不把它当作任务手册；任务事实源始终是 `docs/plan/06-task-implementation-checklist.md`。
- 先确认当前任务属于哪个文档和编号。
- 如果用户没有明确任务编号，先给出候选任务编号、判断依据和建议下一步，等用户确认后再实现。
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
