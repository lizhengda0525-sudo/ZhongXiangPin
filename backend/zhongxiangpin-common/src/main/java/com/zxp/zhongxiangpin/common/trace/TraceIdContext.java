package com.zxp.zhongxiangpin.common.trace;

import java.util.UUID;

/**
 * 请求 traceId 上下文。
 *
 * <p>上下文使用 ThreadLocal 保存当前请求追踪 ID，供统一响应、日志和后续审计复用。
 * Web 层必须在请求结束时调用 {@link #clear()}，避免线程复用导致 traceId 串线。</p>
 */
public final class TraceIdContext {

    // traceId 前缀，格式如 "trace-a1b2c3d4e5f6..."
    private static final String TRACE_PREFIX = "trace-";
    // ThreadLocal 持有当前线程的 traceId，每个请求线程独立
    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();

    private TraceIdContext() {
    }

    /**
     * 设置 traceId（由 TraceIdFilter 从请求头读取后调用）。
     * null 或空白值会被忽略，避免覆盖已有值。
     */
    public static void set(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            TRACE_ID_HOLDER.set(traceId);
        }
    }

    /**
     * 获取当前 traceId，如果没有则自动生成一个。
     * ApiResponse.success() 和 failure() 内部都调用此方法。
     */
    public static String currentOrCreate() {
        String current = TRACE_ID_HOLDER.get();
        if (current != null && !current.isBlank()) {
            return current;
        }
        // 生成 trace-UUID（去掉短横线，缩短长度）
        String generated = TRACE_PREFIX + UUID.randomUUID().toString().replace("-", "");
        TRACE_ID_HOLDER.set(generated);
        return generated;
    }

    /** 清理 ThreadLocal，防止线程池复用时串线。由 TraceIdFilter 的 finally 块调用。 */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
