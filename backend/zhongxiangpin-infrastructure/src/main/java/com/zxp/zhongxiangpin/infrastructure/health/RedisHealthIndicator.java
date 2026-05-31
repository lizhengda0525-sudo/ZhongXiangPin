package com.zxp.zhongxiangpin.infrastructure.health;

import com.zxp.zhongxiangpin.application.health.HealthComponent;
import com.zxp.zhongxiangpin.application.health.HealthComponentStatus;
import com.zxp.zhongxiangpin.application.health.HealthIndicatorPort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 健康检查探针。
 *
 * <p>探针只发送 ping，不读写业务 key，不触发 TTL 续期、占位释放或缓存重建。Redis 业务
 * 一致性边界由后续认证、会场、交易和运行态任务分别实现。</p>
 */
@Component
public class RedisHealthIndicator implements HealthIndicatorPort {

    private static final String PONG = "PONG";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisHealthIndicator(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public HealthComponent component() {
        return HealthComponent.REDIS;
    }

    @Override
    public HealthComponentStatus check() {
        RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
        try {
            String result = connection.ping();
            return PONG.equalsIgnoreCase(result) ? HealthComponentStatus.UP : HealthComponentStatus.DOWN;
        } finally {
            connection.close();
        }
    }
}
