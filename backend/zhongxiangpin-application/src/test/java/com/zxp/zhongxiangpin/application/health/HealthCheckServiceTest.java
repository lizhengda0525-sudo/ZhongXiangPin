package com.zxp.zhongxiangpin.application.health;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthCheckServiceTest {

    @Test
    void marksSingleFailedProbeDownWithoutFailingWholeCheck() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        HealthCheckService service = new HealthCheckService(List.of(
                new FixedHealthIndicatorPort(HealthComponent.DB, HealthComponentStatus.UP),
                new ThrowingHealthIndicatorPort(HealthComponent.REDIS)
        ), environment);

        HealthCheckResult result = service.check();

        assertEquals(HealthComponentStatus.UP, result.getApp());
        assertEquals(HealthComponentStatus.UP, result.getDb());
        assertEquals(HealthComponentStatus.DOWN, result.getRedis());
        assertEquals("local", result.getProfile());
    }

    @Test
    void usesDefaultProfileWhenSpringHasNoActiveProfile() {
        HealthCheckService service = new HealthCheckService(List.of(), new MockEnvironment());

        HealthCheckResult result = service.check();

        assertEquals(HealthComponentStatus.UP, result.getApp());
        assertEquals(HealthComponentStatus.DOWN, result.getDb());
        assertEquals(HealthComponentStatus.DOWN, result.getRedis());
        assertEquals("default", result.getProfile());
    }

    private record FixedHealthIndicatorPort(HealthComponent component,
                                            HealthComponentStatus status) implements HealthIndicatorPort {

        @Override
        public HealthComponentStatus check() {
            return status;
        }
    }

    private record ThrowingHealthIndicatorPort(HealthComponent component) implements HealthIndicatorPort {

        @Override
        public HealthComponentStatus check() {
            throw new IllegalStateException("connection failed");
        }
    }
}
