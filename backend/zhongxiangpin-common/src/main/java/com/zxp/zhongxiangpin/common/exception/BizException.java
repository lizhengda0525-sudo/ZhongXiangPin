package com.zxp.zhongxiangpin.common.exception;

import com.zxp.zhongxiangpin.common.error.ErrorCode;
import com.zxp.zhongxiangpin.common.error.ErrorDetail;

/**
 * 业务异常。
 *
 * <p>用于在领域、应用和基础设施之间传递稳定错误码，最终由 trigger 层全局异常处理器
 * 转换为 {@code code/message/data/traceId} 响应信封。</p>
 */
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;       // 稳定错误码，由异常处理器转为响应 code 字段
    private final String responseMessage;    // 面向前端的消息（可覆盖 ErrorCode 默认值）
    private final ErrorDetail errorDetail;   // 可安全暴露的错误详情（可为 null）

    /** 只传错误码，使用 ErrorCode 自带的默认消息 */
    public BizException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    /** 错误码 + 自定义消息（覆盖默认消息） */
    public BizException(ErrorCode errorCode, String responseMessage) {
        this(errorCode, responseMessage, null);
    }

    /** 错误码 + 错误详情（使用默认消息） */
    public BizException(ErrorCode errorCode, ErrorDetail errorDetail) {
        this(errorCode, errorCode.getMessage(), errorDetail);
    }

    /**
     * 构造携带安全错误详情的业务异常。
     *
     * @param errorCode 稳定错误码，必须来自 OpenAPI 已定义枚举或后续授权新增枚举
     * @param responseMessage 面向前端展示的中文错误说明，不得包含敏感信息
     * @param errorDetail 可安全暴露的字段或资源定位信息
     */
    public BizException(ErrorCode errorCode, String responseMessage, ErrorDetail errorDetail) {
        super(responseMessage);
        this.errorCode = errorCode;
        this.responseMessage = responseMessage;
        this.errorDetail = errorDetail;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public ErrorDetail getErrorDetail() {
        return errorDetail;
    }
}
