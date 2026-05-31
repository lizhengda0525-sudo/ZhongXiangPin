package com.zxp.zhongxiangpin.common.response;

import com.zxp.zhongxiangpin.common.error.ErrorCode;
import com.zxp.zhongxiangpin.common.trace.TraceIdContext;

/**
 * 统一 API 响应信封。
 *
 * <p>响应字段固定为 {@code code/message/data/traceId}，与 OpenAPI 契约保持一致。
 * 新项目不保留旧项目 {@code info}、{@code msg} 等响应消息别名。</p>
 *
 * @param <T> 业务数据类型
 */
public class ApiResponse<T> {

    private final String code;     // 业务错误码，成功为 "0000"
    private final String message;  // 面向用户的提示消息
    private final T data;          // 业务数据，失败时可能为 null 或 ErrorDetail
    private final String traceId;  // 请求追踪 ID，从 ThreadLocal 自动获取

    // 私有构造器，强制通过静态工厂方法创建
    private ApiResponse(String code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    /** 成功响应，code="0000"，traceId 自动注入 */
    public static <T> ApiResponse<T> success(T data) {
        return of(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data, TraceIdContext.currentOrCreate());
    }

    /** 成功响应，无业务数据（如删除操作） */
    public static ApiResponse<Void> success() {
        return success(null);
    }

    /** 失败响应，由全局异常处理器调用 */
    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message, T data) {
        return of(errorCode.getCode(), message, data, TraceIdContext.currentOrCreate());
    }

    /** 通用构造，四个字段全部由调用方指定 */
    public static <T> ApiResponse<T> of(String code, String message, T data, String traceId) {
        return new ApiResponse<>(code, message, data, traceId);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTraceId() {
        return traceId;
    }
}
