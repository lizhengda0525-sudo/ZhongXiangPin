# M1 契约冻结收口实施计划

> **给 Agent 执行者：** 必须使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 按任务逐项执行本计划。步骤使用复选框（`- [ ]`）跟踪。

**目标：** 将 M1 契约与模型从“初稿可用”收口到“可作为 M2、M3、M9、M10 输入的冻结基线”。

**架构：** M1 只修改契约和文档事实源，不创建后端 Java、前端工程、Mapper、Repository 或业务实现。OpenAPI 继续作为 API 单一事实源，`docs/plan/08-contract-alignment.md` 继续作为枚举和字段映射说明，路线图和任务清单只记录阶段状态与验证证据。

**技术栈：** OpenAPI 3.0.3、Redocly CLI、Markdown、PowerShell、Git。

---

### 任务 1：OpenAPI lint 收口

**文件：**
- 修改：`docs/plan/openapi.yaml`

- [ ] **步骤 1：为所有 HTTP operation 增加稳定 `operationId`**

使用 lowerCamelCase 命名，保持业务语义稳定，例如：

```yaml
/api/v1/orders/{orderId}:
  get:
    operationId: getUserOrderDetail
```

- [ ] **步骤 2：处理安全的 Redocly warning**

为 `info` 增加 `license`；`localhost` server 如果保留为本地开发地址，需要在最终总结中说明该 warning 可接受；健康检查如果不适合 4XX，可保留并说明，适合则补充统一错误响应。

- [ ] **步骤 3：运行 OpenAPI lint**

运行：

```powershell
npx.cmd --yes @redocly/cli lint docs/plan/openapi.yaml
```

预期：YAML 有效；若仍有 warning，必须明确是否为可接受 warning。

### 任务 2：M1 阶段文档收口

**文件：**
- 修改：`README.md`
- 修改：`docs/README.md`
- 修改：`docs/plan/04-roadmap.md`
- 修改：`docs/plan/06-task-implementation-checklist.md`
- 修改：`docs/sdd/tasks.md`

- [ ] **步骤 1：更新阶段状态**

把 M1 从“已有初稿/待验证”更新为“已验证，待最终冻结”或“已冻结”，用实际 lint 结果决定措辞。

- [ ] **步骤 2：增加 M1 收口记录**

记录以下事实：

```text
M1 收口记录：
- OpenAPI 覆盖 P0 用户端、管理端和运行态治理端点。
- 统一响应固定为 code/message/data/traceId。
- 分页结构固定为 pageNo/pageSize/total/items。
- 状态枚举、错误码和字段映射已在 OpenAPI 与契约对齐文档中维护。
- 本阶段未创建后端 Java 模块，符合阶段边界。
- M2 可继续验证 migration；M9/M10 可基于 Mock 并行启动。
```

- [ ] **步骤 3：不虚构验证结果**

只有主控实际运行过的命令才写成“已执行”；未执行的验证写成“待执行”。

### 任务 3：数据映射和 Git 风险核对

**文件：**
- 读取：`docs/plan/08-contract-alignment.md`
- 读取：`docs/sdd/mock/strategy.md`
- 读取：`deploy/migration/V1__init_schema.sql`
- 读取：Git 状态和已跟踪文件

- [ ] **步骤 1：检查枚举与 migration 字段**

核对 `OrderStatus`、`TeamStatus`、`TaskStatus`、`RefundSource`、`EventType`、`UserRole` 是否在 migration 中有对应字段或说明。

- [ ] **步骤 2：检查 Git 跟踪状态**

运行：

```powershell
git status --short
git ls-files deploy
```

预期：如果 `deploy/migration/V1__init_schema.sql` 未被跟踪，最终总结必须说明 M2 不能忽略该风险。

### 任务 4：整体验证和完成判定

**文件：**
- 读取：所有已修改文件

- [ ] **步骤 1：查看 diff**

运行：

```powershell
git diff -- docs/plan/openapi.yaml README.md docs/README.md docs/plan/04-roadmap.md docs/plan/06-task-implementation-checklist.md docs/sdd/tasks.md
```

预期：变更只围绕 M1 收口，不提前实现 M2/M3。

- [ ] **步骤 2：运行最终 lint**

运行：

```powershell
npx.cmd --yes @redocly/cli lint docs/plan/openapi.yaml
```

预期：无错误；warning 如果存在，已在总结中解释。

- [ ] **步骤 3：给出 M1 判定**

输出必须包含：

```text
M1 判定：已完成/基本完成但待某项收口。
验证证据：列出命令和结果。
剩余风险：列出未跟踪 migration、可接受 warning 或后续 M2/M3 事项。
```
