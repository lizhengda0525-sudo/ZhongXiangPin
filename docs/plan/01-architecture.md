# 架构设计

## 总体原则

架构保持简洁有序。模块拆分服务于边界清晰和可测试性，不为了形式感增加复杂度。

核心原则：

- 契约先行，前后端以 OpenAPI 为准。
- 数据先行，表结构、唯一键、索引和状态持久化以 migration 为准。
- 领域层保持纯粹，不依赖 Web、MyBatis、Redis 或前端 DTO。
- 用例编排和领域规则分离。
- 基础设施只负责技术实现，不承载业务决策。
- Debug 和 Mock 能力只在本地开发环境启用。
- MySQL 是交易事实源，Redis 只承载缓存、占位和运行态。

## 目标目录

```text
zhongxiangpin/
  AGENT.md
  README.md
  .editorconfig
  .gitattributes
  .gitignore
  docs/
    README.md
    plan/
      00-overview.md
      01-architecture.md
      02-business-design.md
      03-git-versioning.md
      04-roadmap.md
      05-validation.md
      06-task-implementation-checklist.md
      07-development-standard.md
      08-contract-alignment.md
      openapi.yaml
    harness/
      README.md
      reference-map.md
    sdd/
      README.md
      tasks.md
      backend/spec.md
      frontend-user/spec.md
      frontend-admin/spec.md
      mock/strategy.md
    adr/
  backend/
  frontend/
    user-web/
    admin-web/
  deploy/
    migration/
    release-notes/
```

当前阶段只要求文档、契约和 migration 先统一。M1 不创建后端 Java 模块，M2 不创建 Mapper、Repository 或领域代码。`backend/`、`frontend/user-web/` 和 `frontend/admin-web/` 可以在对应骨架任务开始前保持为空目录。

目录职责：

| 目录 | 职责 | 进入实现前的状态 |
| --- | --- | --- |
| `docs/` | 保存规划、SDD、Harness、OpenAPI、验证和后续 ADR，是开发前的规划和契约来源。 | 必须先统一。 |
| `deploy/` | 保存 migration、环境模板、发布说明和回滚资料；migration 是数据事实源。 | 已有 V1 migration 初稿。 |
| `backend/` | 后续保存 Java 17 Spring Boot Maven 多模块。 | 等 `ZXP-BE-SKEL-001` 开始后落地。 |
| `frontend/user-web/` | 后续保存 Vue 3 用户端工程。 | 等 `ZXP-FE-USER-001` 开始后落地。 |
| `frontend/admin-web/` | 后续保存 Vue 3 管理端工程。 | 等 `ZXP-FE-ADMIN-001` 开始后落地。 |

## 后端模块

M3 开始后，后端建议按 Maven 多模块组织：

```text
backend/
  zhongxiangpin-contract
  zhongxiangpin-common
  zhongxiangpin-domain
  zhongxiangpin-application
  zhongxiangpin-infrastructure
  zhongxiangpin-trigger
  zhongxiangpin-app
```

模块职责：

- `contract`：根据 OpenAPI 落地 DTO、接口错误码和状态枚举。
- `common`：统一响应、分页、异常、审计上下文和基础工具。
- `domain`：业务模型、状态机、领域规则和领域服务接口。
- `application`：用例编排，组合领域服务和仓储端口完成业务流程。
- `infrastructure`：MyBatis、Redis、Lua、缓存、任务锁、Mock 支付和外部适配。
- `trigger`：Controller、Scheduler、Interceptor、ExceptionHandler。
- `app`：启动类、配置和 profile 装配。

## 前端模块

```text
frontend/
  user-web/
  admin-web/
```

用户端覆盖登录、会场、详情、锁单、支付结果、订单列表和订单详情。

管理端覆盖活动配置、标签任务、订单治理、任务治理、DCC、线程池和审计日志。

## 部署模块

`deploy` 用于保存：

- 数据库 migration。
- Docker Compose 或本地启动模板。
- 环境变量示例。
- 发布说明和回滚说明。
