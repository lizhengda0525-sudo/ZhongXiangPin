package com.zxp.zhongxiangpin.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 运行态属性。
 *
 * <p>当前 M3 只固定统一 key 前缀和环境片段，后续认证、会场、交易和运行态任务会在该前缀下
 * 按 Redis 运行态规范扩展具体 key。</p>
 */
@ConfigurationProperties(prefix = "zxp.redis")
public class RedisRuntimeProperties {

    private String keyPrefix = "zxp";
    private String environment = "local";

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * 构建当前环境的 Redis key 前缀。
     *
     * @return 形如 {@code zxp:local} 的前缀
     */
    public String buildEnvironmentPrefix() {
        return keyPrefix + ":" + environment;
    }
}
