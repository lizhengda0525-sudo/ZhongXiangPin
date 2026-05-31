package com.zxp.zhongxiangpin.application.health;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * 运行时健康检查用例。
 *
 * <p>本用例只编排只读探针，不开启业务事务：DB 探针使用独立轻量查询，Redis 探针使用独立
 * ping。这样可以避免健康检查把不同基础设施检查绑定到同一个事务边界，也不会影响后续业务
 * Application 服务的事务设计。</p>
 */
@Service
public class HealthCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);
    private static final String DEFAULT_PROFILE = "default";

    private final List<HealthIndicatorPort> healthIndicatorPorts;
    private final Environment environment;

    public HealthCheckService(List<HealthIndicatorPort> healthIndicatorPorts, Environment environment) {
        this.healthIndicatorPorts = healthIndicatorPorts;
        this.environment = environment;
    }

    /**
     * 查询应用、数据库和 Redis 的当前健康状态。
     *
     * <p>单个探针失败只标记该组件为 {@code DOWN}，不让健康检查接口整体抛出异常。
     * 该方法不修改任何业务数据，不应包裹在交易事务中。</p>
     *
     * @return 健康检查结果
     */
    public HealthCheckResult check() {
        Map<HealthComponent, HealthComponentStatus> statuses = new EnumMap<>(HealthComponent.class);
        statuses.put(HealthComponent.DB, HealthComponentStatus.DOWN);
        statuses.put(HealthComponent.REDIS, HealthComponentStatus.DOWN);

        for (HealthIndicatorPort healthIndicatorPort : healthIndicatorPorts) {
            HealthComponent component = healthIndicatorPort.component();
            try {
                statuses.put(component, healthIndicatorPort.check());
            } catch (RuntimeException exception) {
                LOGGER.warn("Health probe failed. component={}, exceptionType={}",
                        component, exception.getClass().getName());
                statuses.put(component, HealthComponentStatus.DOWN);
            }
        }

        return new HealthCheckResult(
                HealthComponentStatus.UP,
                statuses.get(HealthComponent.DB),
                statuses.get(HealthComponent.REDIS),
                currentProfile()
        );
    }

    private String currentProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return DEFAULT_PROFILE;
        }
        return String.join(",", activeProfiles);
    }
}
