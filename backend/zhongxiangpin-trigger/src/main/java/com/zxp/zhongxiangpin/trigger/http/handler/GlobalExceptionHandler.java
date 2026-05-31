package com.zxp.zhongxiangpin.trigger.http.handler;

import com.zxp.zhongxiangpin.common.error.ErrorCode;
import com.zxp.zhongxiangpin.common.error.ErrorDetail;
import com.zxp.zhongxiangpin.common.exception.BizException;
import com.zxp.zhongxiangpin.common.response.ApiResponse;
import com.zxp.zhongxiangpin.common.trace.TraceIdContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>负责把业务异常、参数校验异常和未捕获异常转换为统一响应信封。
 * 本类只暴露安全错误上下文，不向响应或日志输出密码、验证码、token、SQL、Redis key 或异常堆栈。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常，携带稳定错误码和可安全暴露的错误详情
     * @return 包含正确 HTTP 状态和统一错误响应体的响应
     */
    // ===== 业务异常：Controller 或 Service 层主动 throw BizException =====
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleBizException(BizException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        LOGGER.warn("Business exception. traceId={}, errorCode={}", TraceIdContext.currentOrCreate(), errorCode.getCode());
        // 用 BizException 中的 httpStatus 返回（如 409 Conflict），而非统一 200
        return buildErrorResponse(errorCode, exception.getResponseMessage(), exception.getErrorDetail());
    }

    /**
     * 处理 JSON 请求体参数校验异常。
     *
     * @param exception Spring Validation 参数异常
     * @return HTTP 400 和字段级错误详情
     */
    // ===== @RequestBody @Valid 校验失败：Spring 在进入 Controller 前抛出 =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        LOGGER.warn("Body validation failed. traceId={}", TraceIdContext.currentOrCreate());
        // 取出所有字段错误
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        FieldError first = fieldErrors.isEmpty() ? null : fieldErrors.get(0);
        ErrorDetail.Builder builder = ErrorDetail.builder()
                .reason(first == null ? ErrorCode.SYSTEM_PARAM_INVALID.getMessage() : first.getDefaultMessage())
                .retryable(false);
        // 只有单个错误时保留 data.field，兼容前端直接读取
        if (fieldErrors.size() == 1 && first != null) {
            builder.field(first.getField());
        }
        // 所有错误都写入 data.details，前端可一次性展示全部问题
        for (FieldError fe : fieldErrors) {
            builder.detail(fe.getField(), fe.getDefaultMessage());
        }
        ErrorDetail detail = builder.build();
        return buildErrorResponse(ErrorCode.SYSTEM_PARAM_INVALID, detail.getReason(), detail);
    }

    /**
     * 处理 query/path 参数校验异常。
     *
     * @param exception 约束校验异常
     * @return HTTP 400 和字段级错误详情
     */
    // ===== @RequestParam / @PathVariable 校验失败：@Validated 类级别代理抛出 =====
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        LOGGER.warn("Constraint validation failed. traceId={}", TraceIdContext.currentOrCreate());
        ConstraintViolation<?> first = exception.getConstraintViolations().stream().findFirst().orElse(null);
        ErrorDetail.Builder builder = ErrorDetail.builder()
                .reason(first == null ? ErrorCode.SYSTEM_PARAM_INVALID.getMessage() : first.getMessage())
                .retryable(false);
        // 遍历所有违反的约束，写入 details（如 pageNo 不满足 @Min）
        for (ConstraintViolation<?> v : exception.getConstraintViolations()) {
            builder.detail(v.getPropertyPath().toString(), v.getMessage());
        }
        ErrorDetail detail = builder.build();
        return buildErrorResponse(ErrorCode.SYSTEM_PARAM_INVALID, detail.getReason(), detail);
    }

    /**
     * 处理未捕获异常。
     *
     * <p>日志只记录 traceId 和异常类型，避免把请求中的密码、验证码、token 或内部堆栈写入日志。</p>
     *
     * @param exception 未预期异常
     * @return HTTP 500 和系统兜底错误响应
     */
    // ===== 兜底：任何未被上面三个 handler 捕获的异常都到这里 =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleException(Exception exception) {
        String traceId = TraceIdContext.currentOrCreate();
        // 只记 traceId 和异常类型，不记 getMessage()（防止敏感信息写入日志）
        LOGGER.error("Unhandled exception captured. traceId={}, exceptionType={}",
                traceId, exception.getClass().getName());
        // data 返回 null，不暴露任何内部信息给前端
        return buildErrorResponse(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), null);
    }

    // 统一构建错误响应：把 ApiResponse 包装到 ResponseEntity 中，设置正确的 HTTP 状态码
    private ResponseEntity<ApiResponse<ErrorDetail>> buildErrorResponse(
            ErrorCode errorCode, String message, ErrorDetail detail) {
        ApiResponse<ErrorDetail> response = ApiResponse.failure(errorCode, message, detail);
        return ResponseEntity.status(HttpStatus.valueOf(errorCode.getHttpStatus())).body(response);
    }
}
