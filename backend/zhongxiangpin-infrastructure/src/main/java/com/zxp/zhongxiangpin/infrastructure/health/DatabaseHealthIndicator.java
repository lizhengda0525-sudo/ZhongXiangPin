package com.zxp.zhongxiangpin.infrastructure.health;

import com.zxp.zhongxiangpin.application.health.HealthComponent;
import com.zxp.zhongxiangpin.application.health.HealthComponentStatus;
import com.zxp.zhongxiangpin.application.health.HealthIndicatorPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MySQL 健康检查探针。
 *
 * <p>探针只执行 {@code SELECT 1}，用于确认连接可用；它不读取或修改业务表，也不参与交易
 * 事务，避免健康检查影响后续订单、支付和退款链路的事务边界。</p>
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicatorPort {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public HealthComponent component() {
        return HealthComponent.DB;
    }

    @Override
    public HealthComponentStatus check() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return Integer.valueOf(1).equals(result) ? HealthComponentStatus.UP : HealthComponentStatus.DOWN;
    }
}
