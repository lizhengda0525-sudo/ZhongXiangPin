package com.zxp.zhongxiangpin.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 基础设施配置。
 *
 * <p>只负责注册 infrastructure 模块下的 Mapper 扫描范围。具体 Mapper 和 SQL 会在后续
 * 仓储任务中按端口逐步落地，本配置不改变 domain 的纯净依赖边界。</p>
 */
@Configuration
@MapperScan("com.zxp.zhongxiangpin.infrastructure")
public class MybatisInfrastructureConfiguration {
}
