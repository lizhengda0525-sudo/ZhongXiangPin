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
  AGENT.md              # 开发协作入口和上下文管理规则
  README.md             # Git 仓库项目介绍
  docs/README.md        # 文档地图和开发阅读流
  docs/plan/            # 规划、架构、业务、任务、验证、版本管理文档
  docs/harness/         # 旧项目 Harness 参考索引和执行规则
  docs/sdd/             # 后端、前端、Mock 和任务执行 SDD
  backend/              # 后端工程，后续放置 Maven 多模块
  frontend/user-web/    # 用户端前端工程
  frontend/admin-web/   # 管理端前端工程
  deploy/               # migration、部署、环境变量和发布资料
```

## 文档导航

- [开发协作入口](AGENT.md)
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
- [Harness 执行说明](docs/harness/README.md)
- [Harness 参考索引](docs/harness/reference-map.md)
- [SDD 索引](docs/sdd/README.md)
- [SDD 任务执行计划](docs/sdd/tasks.md)
- [后端 SDD](docs/sdd/backend/spec.md)
- [用户端 SDD](docs/sdd/frontend-user/spec.md)
- [管理端 SDD](docs/sdd/frontend-admin/spec.md)
- [Mock 策略](docs/sdd/mock/strategy.md)
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

当前阶段正在完成文档基线统一。仓库已具备 Harness + SDD 文档骨架、P0 OpenAPI 契约、V1 migration 初稿、开发规范、契约对齐规范、文档地图和路线图；后端 Maven 工程、前端 Vue 工程和自动化验证尚未落地。

后续进入编码前，以 `docs/README.md` 判断文档阅读路径，以 `docs/plan/06-task-implementation-checklist.md` 作为项目推进主线，按 `docs/sdd/tasks.md` 检查任务就绪门禁，并按 `docs/harness/reference-map.md` 读取旧项目对应行为证据。

P0 要完成用户端、后端、管理端、运行态治理、验证和发布所需的全部基础功能；P1 只用于承接后续含金量优化、工程增强和生产化提升。
