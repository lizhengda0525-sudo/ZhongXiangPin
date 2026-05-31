# 众享拼 Zhongxiangpin

众享拼是一个面向电商拼团场景的高质量工程项目，目标是构建一个具备真实业务闭环、交易可靠性、后台治理能力和工程化交付规范的前后端一体系统。

本仓库是全新的项目工作区：

```text
D:\JAVA\ZXP\zhongxiangpin
```

已有项目参考源：

```text
D:\ZXP\zhongxiangpin
```

已有项目用于业务调研和实现参照，新项目独立开发，不复制旧项目源码。

## 项目亮点

- 拼团核心链路：登录、会场、试算、开团、参团、锁单、支付、退款、订单查询。
- 交易可靠性：状态机、幂等键、Redis Lua 占位、MySQL 条件兜底、补偿任务和运行态巡检。
- 后台治理：活动、标签、订单、任务、DCC、线程池、审计和运行态指标。
- 前后端契约：以 OpenAPI 作为统一接口契约，降低字段漂移和联调成本。
- 工程规范：多模块分层、测试矩阵、CI、版本 tag、发布和回滚文档。

## 目录结构

```text
zhongxiangpin/
  AGENTS.md             # 开发协作入口和上下文管理规则
  README.md             # Git 仓库项目介绍
  docs/README.md        # 文档地图和开发阅读流
  docs/plan/            # 规划、架构、业务、任务、验证、版本管理文档
  docs/harness/         # 旧项目 Harness 参考索引和执行规则
  docs/sdd/             # 后端、前端、Mock 和任务门禁规则
  docs/deploy/          # 部署方案、云服务器环境和发布演练文档
  backend/              # 后端工程，后续放置 Maven 多模块
  frontend/user-web/    # 用户端前端工程
  frontend/admin-web/   # 管理端前端工程
  deploy/               # migration、部署脚本和环境变量模板，不放说明文档
```

## 文档导航

- [开发协作入口](AGENTS.md)
- [文档地图](docs/README.md)
- [项目概览](docs/plan/00-overview.md)
- [架构设计](docs/plan/01-architecture.md)
- [业务设计](docs/plan/02-business-design.md)
- [Git 版本管理](docs/plan/03-git-versioning.md)
- [开发路线图](docs/plan/04-roadmap.md)
- [验证计划](docs/plan/05-validation.md)
- [任务实施清单](docs/plan/06-task-implementation-checklist.md)
- [项目开发规范](docs/plan/07-development-standard.md)
- [契约对齐规范](docs/plan/08-contract-alignment.md)
- [Redis 运行态规范](docs/plan/09-redis-runtime-standard.md)
- [执行计划归档](docs/superpowers/plans/)
- [Harness 执行说明](docs/harness/README.md)
- [Harness 参考索引](docs/harness/reference-map.md)
- [SDD 索引](docs/sdd/README.md)
- [SDD 任务门禁与依赖规则](docs/sdd/tasks.md)
- [后端 SDD](docs/sdd/backend/spec.md)
- [用户端 SDD](docs/sdd/frontend-user/spec.md)
- [管理端 SDD](docs/sdd/frontend-admin/spec.md)
- [Mock 策略](docs/sdd/mock/strategy.md)
- [云服务器 Docker 环境配置方案](docs/deploy/server-docker.md)
- [OpenAPI 契约](docs/plan/openapi.yaml)

## P0 主链路

```text
注册/登录 -> 多商品会场 -> 商品详情 -> 活动试算
-> 开团/参团 -> 锁单 -> Mock 支付 -> 订单查询
-> 退款/自动退款 -> 管理后台治理
```

## 技术方向

- 后端：Java 17、Spring Boot、Maven、MyBatis、Redis、JUnit 5。
- 前端：Vue 3、Vite、TypeScript、Pinia、Axios、Element Plus。
- 契约：OpenAPI 3.0.3。
- 工程治理：Conventional Commits、CI、版本 tag、发布说明、回滚方案。
- 开发规范：后端遵守阿里巴巴 Java 开发手册，关键业务边界必须具备完整、可维护的注释。
- 对齐规范：OpenAPI lint、Mock 示例、前端类型和 migration 字段映射统一校验。

## 当前阶段

当前阶段已完成 M1 契约与模型收口，以及 M2 数据库与 migration 收口。P0 OpenAPI 已覆盖用户端、管理端和运行态治理端点；统一响应固定为 `code/message/data/traceId`，分页固定为 `pageNo/pageSize/total/items`；状态枚举和错误码已定义，并与契约对齐文档中的字段映射保持一致。

M2 状态：`deploy/migration/V1__init_schema.sql` 已纳入 Git，已固定用户、商品、活动、交易、支付、退款、可靠事件、标签、DCC 和审计表；关键唯一键、索引、状态字段、来源渠道快照、标签批次字段、可靠事件执行时间和审计 `traceId` 已与 `docs/plan/08-contract-alignment.md` 对齐。云服务器 Docker MySQL 8.4 已完成临时空库导入、关键索引检查、种子数据计数和唯一键/非空约束负向验证，临时验证库已删除。

仓库仍未落地后端 Maven 工程、前端 Vue 工程和 CI 自动化验证。下一步主线应进入 M3 后端骨架：创建后端 Maven 多模块、统一响应、异常处理、基础配置和健康检查；M9 用户端和 M10 管理端仍可基于 M1 OpenAPI 与 Mock 并行启动，真实联调依赖后续后端能力。

后续进入编码前，以 `docs/README.md` 判断文档阅读路径，以 `docs/plan/06-task-implementation-checklist.md` 作为项目推进主线，按 `docs/sdd/tasks.md` 检查任务就绪门禁，并按 `docs/harness/reference-map.md` 读取旧项目对应行为证据。

阶段边界：M1 已固定 OpenAPI、错误码、状态枚举、Mock 示例和字段映射；M2 已固定 migration、唯一键、索引和 MySQL 交易事实源；M3 才开始后端 Maven 多模块和基础代码落地。

P0 要完成用户端、后端、管理端、运行态治理、验证和发布所需的全部基础功能；P1 只用于承接后续含金量优化、工程增强和生产化提升。
