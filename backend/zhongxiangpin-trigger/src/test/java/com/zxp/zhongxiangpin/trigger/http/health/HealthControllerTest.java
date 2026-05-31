package com.zxp.zhongxiangpin.trigger.http.health;

import com.zxp.zhongxiangpin.application.health.HealthCheckResult;
import com.zxp.zhongxiangpin.application.health.HealthCheckService;
import com.zxp.zhongxiangpin.application.health.HealthComponentStatus;
import com.zxp.zhongxiangpin.trigger.http.trace.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthControllerTest {

    @Test
    void returnsUnifiedHealthResponse() throws Exception {
        HealthCheckService healthCheckService = mock(HealthCheckService.class);
        when(healthCheckService.check()).thenReturn(new HealthCheckResult(
                HealthComponentStatus.UP,
                HealthComponentStatus.UP,
                HealthComponentStatus.DOWN,
                "local"
        ));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new HealthController(healthCheckService))
                .addFilters(new TraceIdFilter())
                .build();

        mockMvc.perform(get("/api/v1/health").header(TraceIdFilter.TRACE_ID_HEADER, "trace-health"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "trace-health"))
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.traceId").value("trace-health"))
                .andExpect(jsonPath("$.data.app").value("UP"))
                .andExpect(jsonPath("$.data.db").value("UP"))
                .andExpect(jsonPath("$.data.redis").value("DOWN"))
                .andExpect(jsonPath("$.data.profile").value("local"))
                .andExpect(jsonPath("$.info").doesNotExist())
                .andExpect(jsonPath("$.msg").doesNotExist());
    }
}
