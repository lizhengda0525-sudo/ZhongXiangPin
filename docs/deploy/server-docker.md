# 云服务器 Docker 环境配置方案

本文用于规划众享拼在 Ubuntu 云服务器上的 Docker 化部署环境。当前文档只定义服务器基础环境、目录、端口、安全边界、部署流程和验收方式，不直接执行远程服务器变更。

## 1. 目标与范围

目标：

- 使用 Docker Engine 和 Docker Compose 管理应用、Nginx、MySQL、Redis 等运行单元。
- 让本地、测试服和云服务器尽量使用一致的启动方式，降低环境差异。
- 固定数据持久化、配置注入、日志、备份和回滚边界。
- 为后续 M12 发布和回滚文档提供服务器侧基础。

本次范围：

- 规划 Ubuntu 云服务器基础配置。
- 规划 Docker、Compose、Nginx、MySQL、Redis、后端和前端容器关系。
- 规划宿主机目录、端口、安全组、环境变量和验收清单。

不在本次范围：

- 不修改 OpenAPI、Mock、前端类型或 migration 字段。
- 不创建后端 Java 模块、前端 Vue 工程或真实 `docker-compose.yml`。
- 不把 SSH 密码、数据库密码、Redis 密码、token secret 等敏感信息写入 Git。
- 不直接连接或修改远程服务器；真实执行前必须再次确认操作窗口和回滚方式。

## 2. Docker 的作用

Docker 在本项目中的作用不是“必须上生产的形式感”，而是把运行环境收束成可复现的交付单元：

- 应用隔离：后端、前端、Nginx、MySQL、Redis 各自运行在独立容器中，版本和依赖互不污染。
- 环境一致：本地、测试和云服务器使用相同镜像、环境变量和 Compose 网络。
- 发布可控：应用镜像按 tag 发布，失败时优先回滚到上一个镜像版本。
- 数据清晰：MySQL、Redis、日志和备份挂载到宿主机目录，容器重建不丢数据。
- 交付可复盘：每次发布可以记录镜像 tag、migration 范围、配置变更、验证结果和回滚步骤。

## 3. 目标拓扑

```text
公网用户
  |
云厂商安全组：开发期开放 22、80、443、3306、6379；生产发布前收回 3306、6379
  |
Ubuntu 宿主机
  |
Docker Compose 网络 zxp-net
  |
+----------------+       +----------------+
| nginx          | ----> | backend        |
| 80/443 对外    |       | 仅容器内访问   |
+----------------+       +----------------+
        |                         |
        v                         v
+----------------+       +----------------+
| user-web       |       | mysql          |
| 静态资源       |       | 开发期公网可连 |
+----------------+       +----------------+
        |                         |
        v                         v
+----------------+       +----------------+
| admin-web      |       | redis          |
| 静态资源       |       | 开发期公网可连 |
+----------------+       +----------------+
```

说明：

- 对外入口统一走 Nginx 的 `80/443`。
- 当前开发期为了方便本地工具读取数据，MySQL `3306` 和 Redis `6379` 直接开放公网访问。
- 生产发布前必须收回 MySQL `3306` 和 Redis `6379` 的公网访问；后端应用端口仍不直接暴露到公网。
- 当前阶段尚未落地后端和前端工程，拓扑是后续 Compose 模板的目标形态。

## 4. 服务器基础假设

| 项目 | 建议 |
| --- | --- |
| 操作系统 | Ubuntu LTS，部署前用 `lsb_release -a` 确认版本。 |
| 登录用户 | 使用普通用户登录，例如 `ubuntu`，避免直接使用 root。 |
| SSH | 优先改为 SSH key 登录；密码登录只作为临时过渡。 |
| 时区 | `Asia/Shanghai`，避免订单、任务和审计时间漂移。 |
| 磁盘 | 至少为 `/data` 和 `/backup` 预留独立可监控空间。 |
| 内存 | MySQL、Redis、后端同机运行时建议至少 2C4G；更小规格只适合开发验收。 |

## 5. 端口规划

| 端口 | 用途 | 暴露范围 | 说明 |
| --- | --- | --- | --- |
| `22` | SSH | 指定 IP 或临时公网 | 建议安全组限制来源 IP。 |
| `80` | HTTP | 公网 | 后续可自动跳转 HTTPS。 |
| `443` | HTTPS | 公网 | 正式环境必须启用证书。 |
| `8080` | 后端应用 | 容器内网 | 不在安全组开放；调试时也优先走 Nginx。 |
| `3306` | MySQL | 开发期公网 | 使用强密码；禁止使用 root 账号作为应用账号；功能闭环后关闭公网访问。 |
| `6379` | Redis | 开发期公网 | Redis 风险高于 MySQL，必须设置强密码；功能闭环后关闭公网访问。 |

注意：Docker 发布端口可能绕过宿主机 `ufw` 规则，端口暴露必须以 Compose 的 `ports` 配置和云厂商安全组共同控制。当前开发期允许 MySQL 和 Redis 直连公网；进入发布演练或生产前，MySQL、Redis 和后端都应收回到 Compose 内部网络或 SSH tunnel。

本地读取数据当前采用直接公网方式：

```text
MySQL: <server_ip>:3306
Redis: <server_ip>:6379
```

对应 Compose 映射：

```yaml
services:
  mysql:
    ports:
      - "3306:3306"

  redis:
    ports:
      - "6379:6379"
```

当前开发期直连必须同时满足：

- MySQL 使用独立应用账号和强密码，不使用 root 账号作为应用连接账号。
- Redis 必须设置强密码，不能允许匿名访问。
- `.env` 不提交 Git，不在日志或命令记录中输出密码。
- 功能闭环、发布演练或接近生产前，删除 Compose 中的 `3306/6379` 端口映射，并关闭安全组对应规则。

后续收紧时优先改为 SSH tunnel。本地数据库工具连接 `127.0.0.1:3307`，Redis 工具连接 `127.0.0.1:6380`，服务器安全组不需要开放 `3306/6379`。

```bash
ssh -L 3307:127.0.0.1:3306 -L 6380:127.0.0.1:6379 ubuntu@<server_ip>
```

对应 Compose 只绑定宿主机本地回环地址：

```yaml
services:
  mysql:
    ports:
      - "127.0.0.1:3306:3306"

  redis:
    ports:
      - "127.0.0.1:6379:6379"
```

## 6. 宿主机目录规划

```text
/opt/zhongxiangpin/
  docker-compose.yml          # 后续落地，当前文档不创建
  .env                        # 服务器私有配置，不提交 Git
  nginx/
    conf.d/
  app/
    backend/
    user-web/
    admin-web/

/data/zhongxiangpin/
  mysql/
  redis/
  logs/
    nginx/
    backend/
  uploads/

/backup/zhongxiangpin/
  mysql/
  release/
```

目录原则：

- `/opt/zhongxiangpin` 保存部署编排和非敏感模板。
- `/data/zhongxiangpin` 保存运行态数据和日志，容器重建不得清空。
- `/backup/zhongxiangpin` 保存数据库备份和发布前快照。
- `.env` 只能存在服务器，不进入 Git；仓库如需示例，使用 `.env.example`。

## 7. Docker 安装步骤

以下命令以 Docker 官方 Ubuntu apt 仓库为基准。执行前先确认服务器系统版本和维护窗口。

```bash
ssh ubuntu@<server_ip>

lsb_release -a
nproc
free -h
df -h

sudo timedatectl set-timezone Asia/Shanghai
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

sudo systemctl enable --now docker
sudo docker --version
sudo docker compose version
```

是否把用户加入 `docker` 组需要谨慎决定：

```bash
sudo usermod -aG docker ubuntu
```

`docker` 组几乎等同于 root 权限。生产服务器可以继续使用 `sudo docker ...`，减少误操作面。

## 8. 基础安全配置

### 8.1 SSH

- 优先配置 SSH key 登录。
- 部署完成后轮换临时密码。
- 禁止把 SSH 密码写入文档、脚本、Git、CI 日志或聊天记录归档。
- 如启用 `PasswordAuthentication no`，必须先确认 key 登录可用，避免锁死服务器。

### 8.2 安全组和防火墙

云厂商安全组建议：

| 规则 | 建议 |
| --- | --- |
| SSH `22` | 只允许可信来源 IP；无法固定 IP 时至少降低暴露时间。 |
| HTTP `80` | 允许公网。 |
| HTTPS `443` | 允许公网。 |
| MySQL `3306` | 当前开发期允许公网访问；生产发布前关闭。 |
| Redis `6379` | 当前开发期允许公网访问；必须设置强密码；生产发布前关闭。 |
| 后端端口 | 禁止公网，统一经 Nginx 转发。 |

宿主机可启用 `ufw`，但不能只依赖 `ufw` 保护容器端口：

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status verbose
```

### 8.3 敏感配置

服务器 `.env` 至少包含：

```dotenv
ZXP_PROFILE=prod

MYSQL_DATABASE=zhongxiangpin
MYSQL_USER=zxp_app
MYSQL_PASSWORD=<replace_with_strong_password>
MYSQL_ROOT_PASSWORD=<replace_with_strong_password>

REDIS_PASSWORD=<replace_with_strong_password>

SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/zhongxiangpin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME=zxp_app
SPRING_DATASOURCE_PASSWORD=<replace_with_strong_password>

SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=<replace_with_strong_password>

APP_SECRET=<replace_with_strong_secret>
```

约束：

- `.env` 不提交 Git。
- 生产密码不少于 16 位，避免复用 SSH 密码。
- 日志不得输出密码、验证码、token、session 或完整用户敏感信息。

## 9. Compose 服务规划

后续真实 `docker-compose.yml` 建议只在应用工程具备 Dockerfile、构建命令和健康检查后落地。第一版服务职责如下：

| 服务 | 职责 | 持久化 | 对外暴露 |
| --- | --- | --- | --- |
| `nginx` | 静态资源、反向代理、HTTPS 终止 | `logs/nginx`、`nginx/conf.d` | `80/443` |
| `backend` | Spring Boot API、任务调度、健康检查 | `logs/backend` | 不直接暴露 |
| `mysql` | 交易事实源 | `data/mysql` | 开发期公网访问；生产发布前关闭 |
| `redis` | session、缓存、运行态占位 | `data/redis` | 开发期公网访问；生产发布前关闭 |
| `user-web` | 用户端静态资源构建产物 | 无状态 | 由 Nginx 暴露 |
| `admin-web` | 管理端静态资源构建产物 | 无状态 | 由 Nginx 暴露 |

目标 Compose 片段示意：

```yaml
services:
  nginx:
    image: nginx:<fixed-version>
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend
    networks:
      - zxp-net

  backend:
    image: zhongxiangpin/backend:<release-tag>
    env_file:
      - .env
    depends_on:
      - mysql
      - redis
    networks:
      - zxp-net

  mysql:
    image: mysql:<fixed-version>
    env_file:
      - .env
    ports:
      - "3306:3306"
    volumes:
      - /data/zhongxiangpin/mysql:/var/lib/mysql
    networks:
      - zxp-net

  redis:
    image: redis:<fixed-version>
    command: ["redis-server", "--requirepass", "${REDIS_PASSWORD}"]
    ports:
      - "6379:6379"
    volumes:
      - /data/zhongxiangpin/redis:/data
    networks:
      - zxp-net

networks:
  zxp-net:
    driver: bridge
```

要求：

- 镜像必须固定版本或发布 tag，避免使用浮动 `latest`。
- 当前开发期 Compose 映射 MySQL、Redis 到宿主机公网；生产发布前必须删除这两个端口映射，并关闭安全组 `3306/6379`。
- `backend` 必须有健康检查端点，并能反映 app、db、redis 状态。
- 前端 API 基础地址通过环境变量或构建配置注入，不在页面中硬编码公网 IP。

## 10. 数据库初始化与 migration

当前仓库已有 `deploy/migration/V1__init_schema.sql`。首次部署建议：

1. 创建 MySQL 容器和业务库。
2. 在空库执行 V1 migration。
3. 记录执行时间、执行人、commit、SQL 文件校验信息和执行结果。
4. 应用启动后通过健康检查确认数据库连接可用。

约束：

- 已合并的 migration 不回改，只新增下一版 migration。
- 发布前必须备份数据库。
- 涉及金额、订单、支付、退款和状态流转的 migration 失败时停止发布，人工复核。

## 11. 发布流程草案

后续具备应用镜像后，一次发布建议按以下顺序执行：

1. 从 Git tag 构建后端、用户端、管理端镜像。
2. 上传或拉取镜像到服务器。
3. 备份 MySQL。
4. 执行尚未应用的 migration。
5. 更新 `.env` 或 Nginx 配置，记录配置差异。
6. 执行 `sudo docker compose pull`。
7. 执行 `sudo docker compose up -d`。
8. 检查容器状态、健康检查、Nginx 访问、主链路和后台治理入口。
9. 记录发布说明、验证结果、风险和回滚点。

回滚原则：

- 应用问题优先回滚镜像 tag。
- 配置问题优先回滚 `.env` 和 Nginx 配置。
- 数据库回滚不能依赖删除字段或反向 SQL；高风险变更必须先备份，并优先采用向前修复。

## 12. 日志与备份

日志：

- Nginx 日志挂载到 `/data/zhongxiangpin/logs/nginx`。
- 后端业务日志挂载到 `/data/zhongxiangpin/logs/backend`。
- 日志保留周期后续由 logrotate 或容器日志策略统一治理。

备份：

- MySQL 备份目录为 `/backup/zhongxiangpin/mysql`。
- 发布前必须做一次手工备份。
- 后续可增加每日定时备份和异地备份。

备份命名建议：

```text
zxp-mysql-<env>-<yyyyMMddHHmmss>-<git-tag>.sql.gz
```

## 13. 验收清单

服务器基础验收：

- `lsb_release -a`、`free -h`、`df -h` 已记录。
- 时区为 `Asia/Shanghai`。
- `sudo docker --version` 可执行。
- `sudo docker compose version` 可执行。
- Docker 服务已设置开机启动。

安全验收：

- SSH 不再依赖长期明文密码。
- 云厂商安全组只开放必要端口。
- 当前开发期 MySQL、Redis 暴露公网；必须确认强密码、非 root 应用账号和 Redis 鉴权均已生效。
- 生产发布前 MySQL、Redis 必须关闭公网访问，只保留 Nginx 对外入口。
- `.env` 未提交 Git。
- 生产密码和应用 secret 已替换为强随机值。

应用验收：

- `sudo docker compose ps` 中关键容器为 healthy 或 running。
- Nginx 可访问用户端和管理端入口。
- `/api/v1/health` 能返回 app、db、redis 状态。
- 主链路验收按 `docs/plan/05-validation.md` 执行。

发布验收：

- 发布版本来自 Git tag。
- migration 范围已记录。
- 发布前数据库备份存在。
- 验证结果、已知风险和回滚方式已记录。

## 14. 当前决策与偏差

- 当前只形成 Docker 云服务器环境配置方案，不提前创建后端、前端或 Compose 实现文件。
- Docker 安装命令参考官方 Ubuntu 安装路径；实际执行前仍以当日官方文档为准。
- 当前项目阶段尚未具备完整应用镜像，Compose 片段只作为后续落地模板，不作为可直接运行文件。
- 为了方便本地读取数据，当前开发期直接公网暴露 MySQL 和 Redis；生产发布前必须收回公网端口，只保留 Nginx 对外入口。
- 本文不记录服务器密码；已暴露过的临时密码建议部署完成后轮换。

## 15. 执行记录

### 2026-05-31 云服务器基础环境

已完成：

- 服务器巡检：Ubuntu 24.04.4 LTS，2 核，约 3.6GiB 内存，根盘约 59G，时区 `Asia/Shanghai`。
- Docker 安装：Docker Engine `29.5.2`，Docker Compose `v5.1.4`。
- Docker daemon：已启用开机启动，并配置腾讯云 Docker 镜像加速。
- 目录初始化：已创建 `/opt/zhongxiangpin`、`/data/zhongxiangpin`、`/backup/zhongxiangpin` 及其子目录。
- 私有配置：服务器已生成 `/opt/zhongxiangpin/.env`，权限为 `600`，不进入 Git。
- Compose 编排：服务器已生成 `/opt/zhongxiangpin/docker-compose.yml`，当前只包含 MySQL 和 Redis。
- MySQL：容器 `zxp-mysql` 使用 `mysql:8.4`，健康检查通过，版本 `8.4.9`，业务库 `zhongxiangpin` 已创建，端口映射 `0.0.0.0:3306->3306`。
- Redis：容器 `zxp-redis` 使用 `redis:7.4-alpine`，健康检查通过，鉴权 `PONG` 通过，端口映射 `0.0.0.0:6379->6379`。
- 公网连通性：本地执行 `Test-NetConnection 124.220.231.51 -Port 3306` 和 `Test-NetConnection 124.220.231.51 -Port 6379` 均返回 `TcpTestSucceeded: True`。

复查结果：

- Docker 服务为 `enabled` 和 `active`，Docker Root 为 `/var/lib/docker`，日志驱动为 `json-file`。
- Docker daemon 已配置 `https://mirror.ccs.tencentyun.com` 镜像加速和 `100m * 3` 日志轮转。
- `/opt/zhongxiangpin/.env` 权限为 `600`，`docker-compose.yml` 权限为 `640`。
- `docker compose config --quiet` 通过；`zxp-mysql` 和 `zxp-redis` 均为 `running`、`healthy`、`restart=unless-stopped`。
- MySQL 数据挂载到 `/data/zhongxiangpin/mysql`，Redis 数据挂载到 `/data/zhongxiangpin/redis`。
- 根盘约 59G，已用约 7.1G；MySQL 数据目录约 209M，Redis 数据目录约 16K。
- 服务器已有宿主机 Nginx 监听 `80` 端口；后续如果启用 Docker Nginx，需要先决定复用宿主机 Nginx 还是迁移到容器内。

待处理：

- 尚未执行 `deploy/migration/V1__init_schema.sql`；等 M2 migration 收口确认后再执行空库初始化。
- 生产发布前必须关闭 MySQL 和 Redis 公网端口，改为 Compose 内网或 SSH tunnel。
- 如果后续由 Docker 管理 Nginx，需处理宿主机 `80` 端口占用，避免 Compose 启动冲突。

## 16. 参考资料

- [Docker Engine Ubuntu 安装文档](https://docs.docker.com/engine/install/ubuntu/)
- [Docker Linux 安装后配置文档](https://docs.docker.com/engine/install/linux-postinstall/)
