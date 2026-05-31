package com.zxp.zhongxiangpin.trigger.http.handler;

import com.zxp.zhongxiangpin.common.error.ErrorCode;
import com.zxp.zhongxiangpin.common.error.ErrorDetail;
import com.zxp.zhongxiangpin.common.exception.BizException;
import com.zxp.zhongxiangpin.trigger.http.trace.TraceIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new TraceIdFilter())
            .setValidator(new LocalValidatorFactoryBean())
            .build();

    @Test
    void handlesBizExceptionWithUnifiedBodyAndHttpStatus() throws Exception {
        mockMvc.perform(get("/test/biz").header(TraceIdFilter.TRACE_ID_HEADER, "trace-test-biz"))
                .andExpect(status().isConflict())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "trace-test-biz"))
                .andExpect(jsonPath("$.code").value("TRADE_ORDER_STATUS_INVALID"))
                .andExpect(jsonPath("$.message").value("当前订单状态不允许退款"))
                .andExpect(jsonPath("$.traceId").value("trace-test-biz"))
                .andExpect(jsonPath("$.data.resourceType").value("ORDER"))
                .andExpect(jsonPath("$.data.resourceId").value("ORD-001"))
                .andExpect(jsonPath("$.info").doesNotExist())
                .andExpect(jsonPath("$.msg").doesNotExist());
    }

    @Test
    void handlesBodyValidationSingleField() throws Exception {
        mockMvc.perform(post("/test/validate-body")
                        .header(TraceIdFilter.TRACE_ID_HEADER, "trace-test-single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"age\": 20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYSTEM_PARAM_INVALID"))
                .andExpect(jsonPath("$.message").value("名称不能为空"))
                .andExpect(jsonPath("$.traceId").value("trace-test-single"))
                .andExpect(jsonPath("$.data.field").value("name"))
                .andExpect(jsonPath("$.data.reason").value("名称不能为空"));
    }

    @Test
    void handlesBodyValidationMultipleFields() throws Exception {
        mockMvc.perform(post("/test/validate-body")
                        .header(TraceIdFilter.TRACE_ID_HEADER, "trace-test-multi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYSTEM_PARAM_INVALID"))
                .andExpect(jsonPath("$.traceId").value("trace-test-multi"))
                .andExpect(jsonPath("$.data.field").doesNotExist())
                .andExpect(jsonPath("$.data.details").isNotEmpty());
    }

    @Test
    void handlesSystemExceptionWithSafeBody() throws Exception {
        mockMvc.perform(get("/test/system").header(TraceIdFilter.TRACE_ID_HEADER, "trace-test-system"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("SYSTEM_ERROR"))
                .andExpect(jsonPath("$.message").value("系统开小差了，请稍后再试"))
                .andExpect(jsonPath("$.traceId").value("trace-test-system"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Validated
    @RestController
    static class TestController {

        @GetMapping("/test/biz")
        void biz() {
            ErrorDetail detail = ErrorDetail.builder()
                    .resourceType("ORDER")
                    .resourceId("ORD-001")
                    .currentStatus("REFUNDED")
                    .expectedStatus("PAID")
                    .retryable(false)
                    .build();
            throw new BizException(ErrorCode.TRADE_ORDER_STATUS_INVALID, "当前订单状态不允许退款", detail);
        }

        @PostMapping("/test/validate-body")
        void validateBody(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/system")
        void system() {
            throw new IllegalStateException("password=secret&code=123456&token=abc");
        }
    }

    static class TestRequest {

        @NotBlank(message = "名称不能为空")
        private String name;

        @Min(value = 1, message = "年龄必须大于0")
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
