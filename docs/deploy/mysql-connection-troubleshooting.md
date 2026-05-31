# MySQL 连接故障复盘与排查记录

本文记录 2026-05-31 本地访问 `/api/v1/health` 返回 `db: DOWN` 的排查过程、原始项目配置证据、新项目修正方式和后续排查准则。本文只记录配置形态和技术结论，不记录真实密码、token 或云服务器私钥。

## 1. 故障现象

访问健康检查接口：

```text
http://localhost:8080/api/v1/health
```

响应中应用和 Redis 正常，但数据库不可用：

```json
{
  "code": "0000",
  "message": "成功",
  "data": {
    "app": "UP",
    "db": "DOWN",
    "redis": "UP",
    "profile": "dev"
  },
  "traceId": "trace-..."
}
```

后端日志中的直接表现是健康探针失败：

```text
Health probe failed. component=DB, exceptionType=org.springframework.jdbc.CannotGetJdbcConnectionException
```

健康检查探针本身只执行 `SELECT 1`，单个组件失败会被 `HealthCheckService` 捕获并降级为 `DOWN`，因此接口响应不会直接暴露数据库连接根异常。排查时必须看应用日志或单独执行 JDBC 最小验证。

## 2. 原始项目 MySQL 配置

旧项目只读参考文件：

```text
D:\ZXP\zhongxiangpin\zhongxiangpin-app\src\main\resources\application-dev.yml
```

旧项目 dev 环境的 MySQL 连接形态如下，密码通过环境变量或默认值注入，本文不记录真实密码：

```yaml
spring:
  datasource:
    username: ${ZXP_DB_USERNAME:root}
    password: ${ZXP_DB_PASSWORD:******}
    url: ${ZXP_DB_URL:jdbc:mysql://124.220.231.51:13306/group_buy_market?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&useSSL=true&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES'}
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      pool-name: Retail_HikariCP
      minimum-idle: 15
      idle-timeout: 180000
      maximum-pool-size: 25
      auto-commit: true
      max-lifetime: 1800000
      connection-timeout: 30000
      connection-test-query: SELECT 1
      initialization-fail-timeout: -1
```

从旧项目提取出的关键行为和隐性约束：

- JDBC URL 明确设置字符集、时区、自动重连、零时间处理和 session 级 SQL mode。
- 使用 `com.mysql.cj.jdbc.Driver`。
- 数据源类型显式为 `com.zaxxer.hikari.HikariDataSource`。
- Hikari 使用较长连接超时 `30000ms`，并用 `SELECT 1` 做连接测试。
- 旧项目库名是 `group_buy_market`，新项目不能直接复用旧库名；新项目 MySQL 事实源应为 `zhongxiangpin`。

## 3. 新项目初始差异

新项目文件：

```text
D:\JAVA\ZXP\zhongxiangpin\backend\zhongxiangpin-app\src\main\resources\application-dev.yml
D:\JAVA\ZXP\zhongxiangpin\.env
```

本次排查前，新项目 dev 默认配置已经包含部分基础参数：

```yaml
spring:
  datasource:
    url: ${ZXP_DB_URL:${SPRING_DATASOURCE_URL:jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:zhongxiangpin_dev}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}}
    username: ${ZXP_DB_USERNAME:${SPRING_DATASOURCE_USERNAME:${MYSQL_USER:root}}}
    password: ${ZXP_DB_PASSWORD:${SPRING_DATASOURCE_PASSWORD:${MYSQL_PASSWORD:}}}
```

但实际运行时 `.env` 中的 `SPRING_DATASOURCE_URL` 会覆盖 `application-dev.yml` 里的默认 JDBC URL。也就是说，即使 `application-dev.yml` 默认值后面补了参数，只要 `.env` 的 `SPRING_DATASOURCE_URL` 缺少这些参数，运行时仍然使用 `.env` 的版本。

这是本次坑点之一：**Spring Boot 配置覆盖顺序导致我们看到的 yml 默认值不一定是最终生效值**。

## 4. 本次修正后的配置

新项目仍然使用新库名 `zhongxiangpin`，不复制旧项目库名和旧项目源码，只迁移旧项目可验证的连接参数和连接池行为。

当前 dev 配置：

```yaml
spring:
  datasource:
    url: "${ZXP_DB_URL:${SPRING_DATASOURCE_URL:jdbc:mysql://${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:zhongxiangpin_dev}?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&useSSL=false&sslMode=DISABLED&allowPublicKeyRetrieval=true&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES'}}"
    username: ${ZXP_DB_USERNAME:${SPRING_DATASOURCE_USERNAME:${MYSQL_USER:root}}}
    password: ${ZXP_DB_PASSWORD:${SPRING_DATASOURCE_PASSWORD:${MYSQL_PASSWORD:}}}
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      pool-name: ZXP_DEV_HIKARI
      minimum-idle: 15
      idle-timeout: 180000
      maximum-pool-size: 25
      auto-commit: true
      max-lifetime: 1800000
      connection-timeout: 30000
      connection-test-query: SELECT 1
      initialization-fail-timeout: -1
```

`.env` 中的 `SPRING_DATASOURCE_URL` 也必须同步为同一组参数，否则 `.env` 会继续覆盖 yml 默认值：

```text
SPRING_DATASOURCE_URL=jdbc:mysql://124.220.231.51:3306/zhongxiangpin?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&useSSL=false&sslMode=DISABLED&allowPublicKeyRetrieval=true&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES'
```

`.env.example` 已同步相同参数形态，便于后续环境配置时复制。

## 5. 根因分析

### 5.1 不是 MySQL 8.4 版本不兼容

本地使用项目同版本 Java 17 和 MySQL Connector/J 9.1.0 执行最小 JDBC 验证：

```text
读取 .env -> DriverManager.getConnection(...) -> SELECT DATABASE(), VERSION(), 1
```

结果：

```text
db=zhongxiangpin, version=8.4.9, ok=1
```

这证明 Java JDBC 能连接云端 MySQL 8.4.9，并能执行 `SELECT 1`。因此不能把问题归因于“MySQL 版本太高”。在没有备份、回滚方案和明确服务端兼容性错误前，不应删除或重建云端 MySQL。

### 5.2 系统默认 Java 曾经指向 Java 8

排查中直接运行 fat jar 时出现：

```text
UnsupportedClassVersionError:
JarLauncher has been compiled by a more recent version of the Java Runtime
class file version 61.0, this version only recognizes up to 52.0
```

这说明当时系统默认 `java` 是 Java 8，而项目要求 Java 17。原因是 `Path` 中 Oracle Java shim 排在 JDK 17 前面：

```text
C:\Program Files (x86)\Common Files\Oracle\Java\javapath\java.exe
E:\JDK\JDK17\bin\java.exe
```

修正方式：

- `JAVA_HOME` 指向 `E:\JDK\JDK17`。
- `Path` 中 `E:\JDK\JDK17\bin` 排在 Oracle `javapath` 前面。
- 新终端中执行 `java -version` 必须显示 Java 17。

如果默认 Java 仍是 Java 8，服务可能根本没有正常启动，健康检查结果和数据库状态都会失去判断意义。

### 5.3 `.env` 覆盖了 `application-dev.yml` 默认值

新项目 `application.yml` 使用：

```yaml
spring:
  config:
    import:
      - optional:file:.env[.properties]
      - optional:file:../.env[.properties]
      - optional:file:../../.env[.properties]
```

因此从项目根目录启动时，根目录 `.env` 会进入 Spring 配置环境。`application-dev.yml` 中的 datasource URL 写法是：

```yaml
url: ${ZXP_DB_URL:${SPRING_DATASOURCE_URL:默认值}}
```

实际优先级是：

```text
ZXP_DB_URL > SPRING_DATASOURCE_URL > yml 默认 JDBC URL
```

所以修复 JDBC URL 不能只改 yml 默认值，还要确认 `.env` 中 `SPRING_DATASOURCE_URL` 或 `ZXP_DB_URL` 是否同步。

### 5.4 CLI 能连不等于 Java 运行态配置一致

手工命令能够连接：

```bash
mysql -h 124.220.231.51 -P 3306 -u zxp_app -p --ssl-mode=DISABLED
```

这条命令验证的是：

- 本机到云端 `3306` 可以连通。
- 账号密码可以通过 MySQL 客户端认证。
- CLI 显式禁用了 SSL。

但 Java 服务是否能连，还取决于：

- Spring 最终生效的 JDBC URL。
- JDBC URL 是否显式关闭 SSL 或声明 `sslMode=DISABLED`。
- Connector/J 版本。
- 运行进程是否真的使用 Java 17。
- 应用是否从包含 `.env` 的工作目录启动。

因此 CLI 成功后，下一步应使用 Java Connector/J 最小验证，而不是直接判断健康检查代码有问题。

### 5.5 沙箱网络造成过误导

排查中在受限执行环境里出现过：

```text
Permission denied: connect
Can't connect to MySQL server
```

但同一份 JDBC 验证在沙箱外执行成功。这说明某些网络检查结果受到执行环境限制影响，不能直接等同于用户本机真实网络状态。以后判断云端连通性时，应优先使用用户终端或已获授权的沙箱外验证。

### 5.6 Tomcat loopback 失败不是数据库失败

直接启动 Spring Boot 时曾出现：

```text
Unable to establish loopback connection
```

这是 Tomcat/NIO 在当前 Windows 执行环境中启动 HTTP 端口时的 loopback 问题，不是 MySQL 连接问题。出现该错误时，应用没有稳定监听 `8080`，因此不能用 `/api/v1/health` 判断数据库是否修复。

## 6. 验证方式

### 6.1 Java 版本验证

```powershell
where java
java -version
mvn -v
```

期望：

```text
where java 的第一项为 E:\JDK\JDK17\bin\java.exe
java -version 显示 17.x
mvn -v 显示 Java version: 17.x
```

### 6.2 JDBC 最小验证

用项目 JDK 17 和项目 MySQL Connector/J 执行最小验证，读取 `.env`，然后执行：

```sql
SELECT DATABASE(), VERSION(), 1;
```

期望结果：

```text
db=zhongxiangpin, version=8.4.9, ok=1
```

该验证绕过 Tomcat 和健康检查封装，能直接判断 JDBC URL、账号、密码、库名和驱动是否可用。

### 6.3 后端测试验证

```powershell
mvn -pl zhongxiangpin-app -am test
```

已验证结果：

```text
BUILD SUCCESS
```

注意：测试 profile 中 DB 可以为 `DOWN`，因为当前集成测试只验证健康接口统一响应结构，不要求测试环境真实连接云端 MySQL。

### 6.4 健康接口验证

当本地 Java 17、Tomcat loopback 和 8080 端口均正常后，执行：

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/api/v1/health
```

期望：

```json
{
  "data": {
    "app": "UP",
    "db": "UP",
    "redis": "UP",
    "profile": "dev"
  }
}
```

如果仍为 `db: DOWN`，应查看日志中的根异常，而不是直接修改数据库版本。

## 7. 后续排查 SOP

遇到 `db: DOWN` 时，按以下顺序收敛：

1. 确认 Java 版本：`java -version` 和 `mvn -v` 必须是 Java 17。
2. 确认最终配置源：检查 `ZXP_DB_URL`、`SPRING_DATASOURCE_URL`、`MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USER`。
3. 确认 `.env` 的 JDBC URL 参数完整，尤其是 `serverTimezone`、`useSSL`、`sslMode`、`allowPublicKeyRetrieval` 和 `sessionVariables`。
4. 用 MySQL CLI 验证账号和库名，但不要只依赖 CLI 结论。
5. 用 Java Connector/J 执行最小 `SELECT DATABASE(), VERSION(), 1` 验证。
6. 启动应用后看日志中的根异常：认证失败、库不存在、网络不通、SSL 错误、驱动错误、Tomcat 启动失败分别处理。
7. 只有在 Java JDBC 最小验证报出明确的服务端兼容性问题，并且已有备份和回滚方案时，才讨论重建或降级云端 MySQL。

## 8. 本次结论

本次问题的真实组合是：

```text
系统默认 Java 曾指向 Java 8
+ 新项目 JDBC URL 未完整对齐旧项目连接参数
+ .env 覆盖 yml 默认值
+ 沙箱网络检查产生误导
+ Tomcat loopback 问题干扰了 /health 验证
```

最终确认：

```text
Java Connector/J 9.1.0 可以连接云端 MySQL 8.4.9 的 zhongxiangpin 库并执行 SELECT 1。
```

因此当前不应删除云端 MySQL，也不应把问题归因于 MySQL 8.4 本身。后续重点是保证运行时 Java 17、JDBC URL 参数完整、`.env` 与 yml 默认配置一致，并在健康检查失败时优先读取根异常。
