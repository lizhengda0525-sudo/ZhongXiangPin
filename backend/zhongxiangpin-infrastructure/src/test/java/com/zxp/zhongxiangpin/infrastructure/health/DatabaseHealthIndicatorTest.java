package com.zxp.zhongxiangpin.infrastructure.health;

import com.zxp.zhongxiangpin.application.health.HealthComponent;
import com.zxp.zhongxiangpin.application.health.HealthComponentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseHealthIndicatorTest {

    @Test
    void returnsUpWhenSelectOneSucceeds() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(jdbcTemplate);

        assertEquals(HealthComponent.DB, indicator.component());
        assertEquals(HealthComponentStatus.UP, indicator.check());
    }

    @Test
    void returnsDownWhenSelectOneReturnsUnexpectedValue() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(0);
        DatabaseHealthIndicator indicator = new DatabaseHealthIndicator(jdbcTemplate);

        assertEquals(HealthComponentStatus.DOWN, indicator.check());
    }
}
