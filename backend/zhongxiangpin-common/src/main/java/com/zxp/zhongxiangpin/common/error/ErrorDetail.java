package com.zxp.zhongxiangpin.common.error;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 可安全返回给前端的错误详情。
 *
 * <p>本对象只承载字段名、资源 ID、状态、是否可重试等定位信息；
 * 禁止放入异常堆栈、SQL、Redis key、token、验证码或内部类名。</p>
 */
public class ErrorDetail {

    private final String field;           // 出错的字段名（参数校验时用）
    private final String reason;          // 错误原因说明
    private final String resourceType;    // 资源类型，如 "ORDER"、"USER"
    private final String resourceId;      // 资源 ID，如订单号
    private final String currentStatus;   // 资源当前状态
    private final String expectedStatus;  // 期望的状态
    private final Boolean retryable;      // 是否可重试
    private final Map<String, Object> details;  // 扩展信息（参数校验时存放所有字段错误）

    // 私有构造器，只能通过 Builder 创建
    private ErrorDetail(Builder builder) {
        this.field = builder.field;
        this.reason = builder.reason;
        this.resourceType = builder.resourceType;
        this.resourceId = builder.resourceId;
        this.currentStatus = builder.currentStatus;
        this.expectedStatus = builder.expectedStatus;
        this.retryable = builder.retryable;
        // 用 unmodifiableMap 包装，防止外部修改；LinkedHashMap 保持插入顺序
        this.details = Collections.unmodifiableMap(new LinkedHashMap<>(builder.details));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getField() {
        return field;
    }

    public String getReason() {
        return reason;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getExpectedStatus() {
        return expectedStatus;
    }

    public Boolean getRetryable() {
        return retryable;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * 错误详情构造器。
     *
     * <p>构造器只接收安全上下文字段，由调用方在进入构造器前完成敏感信息裁剪。</p>
     */
    public static class Builder {

        private String field;
        private String reason;
        private String resourceType;
        private String resourceId;
        private String currentStatus;
        private String expectedStatus;
        private Boolean retryable;
        private final Map<String, Object> details = new LinkedHashMap<>();

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder currentStatus(String currentStatus) {
            this.currentStatus = currentStatus;
            return this;
        }

        public Builder expectedStatus(String expectedStatus) {
            this.expectedStatus = expectedStatus;
            return this;
        }

        public Builder retryable(Boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        public Builder detail(String key, Object value) {
            if (key != null && value != null) {
                this.details.put(key, value);
            }
            return this;
        }

        public ErrorDetail build() {
            return new ErrorDetail(this);
        }
    }
}
