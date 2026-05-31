package com.zxp.zhongxiangpin.infrastructure.health;

import com.zxp.zhongxiangpin.application.health.HealthComponent;
import com.zxp.zhongxiangpin.application.health.HealthComponentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisHealthIndicatorTest {

    @Test
    void returnsUpWhenPingReturnsPongAndClosesConnection() {
        RedisConnection connection = mock(RedisConnection.class);
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        when(stringRedisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");
        RedisHealthIndicator indicator = new RedisHealthIndicator(stringRedisTemplate);

        assertEquals(HealthComponent.REDIS, indicator.component());
        assertEquals(HealthComponentStatus.UP, indicator.check());
        verify(connection).close();
    }

    @Test
    void returnsDownWhenPingReturnsUnexpectedValue() {
        RedisConnection connection = mock(RedisConnection.class);
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
        when(stringRedisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("NOPE");
        RedisHealthIndicator indicator = new RedisHealthIndicator(stringRedisTemplate);

        assertEquals(HealthComponentStatus.DOWN, indicator.check());
        verify(connection).close();
    }
}
