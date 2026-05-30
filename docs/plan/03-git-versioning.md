# Git 与版本管理

## 分支模型

采用 trunk-based development：

- `main`：始终保持可构建、可测试。
- `feat/ZXP-xxx-name`：功能开发。
- `fix/ZXP-xxx-name`：缺陷修复。
- `refactor/ZXP-xxx-name`：无行为变化重构。
- `docs/ZXP-xxx-name`：文档变更。
- `chore/ZXP-xxx-name`：构建、配置、依赖、CI。
- `spike/ZXP-xxx-name`：实验分支，不直接合并。
- `release/1.0.0`：发布冻结分支。
- `hotfix/1.0.1-name`：紧急修复。

禁止直接向 `main` 提交业务代码。

## 提交规范

使用 Conventional Commits：

```text
feat(trade): add lock order use case
fix(auth): reject mismatched session user
refactor(market): extract trial service
docs(plan): add business state machine
test(trade): cover repeated payment callback
chore(ci): add backend build gate
```

## 版本规划

建议 tag：

- `planning-v0.1.0`：规划文档和目录完成。
- `skeleton-v0.2.0`：后端和前端骨架完成。
- `backend-auth-v0.3.0`：认证模块完成。
- `market-trade-v0.5.0`：会场和交易核心完成。
- `admin-web-v0.8.0`：管理端完成。
- `release-v1.0.0`：第一版完整闭环。

数据库 migration 使用不可变版本：

```text
V1__init_schema.sql
V2__add_runtime_metric_table.sql
V3__add_order_search_index.sql
```

已经合并的 migration 不修改，只新增下一版。

## PR 要求

每个 PR 必须说明：

- 业务目标。
- 是否影响 OpenAPI。
- 是否影响数据库 migration。
- 是否影响前端字段。
- 测试结果。
- 回滚方式。

## CI 要求

后续 CI 至少包含：

- OpenAPI lint 和 mock 数据校验。
- 后端 `mvn test`。
- 分层依赖检查。
- Mapper XML 契约测试。
- 前端类型检查和构建。
- secret scan。

## 发布和回滚

发布只从 tag 进行，不从任意 feature 分支发布。

Release note 需要记录：

- 应用版本。
- 契约版本。
- 数据库 migration 范围。
- 主要变更。
- 已知风险。
- 回滚步骤。
