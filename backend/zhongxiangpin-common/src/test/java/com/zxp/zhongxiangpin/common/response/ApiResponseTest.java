package com.zxp.zhongxiangpin.common.response;

import com.zxp.zhongxiangpin.common.trace.TraceIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiResponseTest {

    @AfterEach
    void tearDown() {
        TraceIdContext.clear();
    }

    @Test
    void successUsesMessageAndTraceId() {
        TraceIdContext.set("trace-unit-001");

        ApiResponse<String> response = ApiResponse.success("ok");

        assertEquals("0000", response.getCode());
        assertEquals("成功", response.getMessage());
        assertEquals("ok", response.getData());
        assertEquals("trace-unit-001", response.getTraceId());
    }
}
