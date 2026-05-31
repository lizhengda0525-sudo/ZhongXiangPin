package com.zxp.zhongxiangpin.infrastructure.config;

import com.zxp.zhongxiangpin.infrastructure.config.properties.RedisRuntimeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 基础设施配置。
 *
 * <p>连接工厂和 {@code StringRedisTemplate} 由 Spring Boot Data Redis 自动配置创建。
 * 本配置只启用项目 Redis 运行态属性，业务 key 与 TTL 会在后续认证、会场和交易任务中落地。</p>
 */
@Configuration
@EnableConfigurationProperties(RedisRuntimeProperties.class)
public class RedisInfrastructureConfiguration {
}
