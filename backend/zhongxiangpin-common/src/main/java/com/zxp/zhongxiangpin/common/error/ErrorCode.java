package com.zxp.zhongxiangpin.common.error;

/**
 * 项目统一错误码。
 *
 * <p>枚举值与 {@code docs/plan/openapi.yaml} 中的 ErrorCode 保持一致。
 * HTTP 状态用于全局异常处理器返回协议层语义，不能为了统一响应体把失败都返回 200。</p>
 */
public enum ErrorCode {

    // ===== 通用 =====
    SUCCESS("0000", "成功", 200),                // 请求成功

    // ===== 认证授权 =====
    AUTH_401("AUTH_401", "请先登录", 401),        // 未登录或 token 过期
    AUTH_403("AUTH_403", "无权访问当前资源", 403), // 已登录但权限不足
    AUTH_USERNAME_DUPLICATED("AUTH_USERNAME_DUPLICATED", "用户名已存在", 409),
    AUTH_PHONE_DUPLICATED("AUTH_PHONE_DUPLICATED", "手机号已存在", 409),
    AUTH_PASSWORD_MISMATCH("AUTH_PASSWORD_MISMATCH", "两次输入的密码不一致", 400),
    AUTH_PHONE_INVALID("AUTH_PHONE_INVALID", "手机号格式不合法", 400),
    AUTH_BAD_CREDENTIALS("AUTH_BAD_CREDENTIALS", "账号或密码错误", 400),
    AUTH_CODE_INVALID("AUTH_CODE_INVALID", "验证码错误或已过期", 400),
    AUTH_ACCOUNT_DISABLED("AUTH_ACCOUNT_DISABLED", "账号已禁用", 403),
    AUTH_ADMIN_REQUIRED("AUTH_ADMIN_REQUIRED", "需要后台管理员权限", 403),

    // ===== 会场活动 =====
    MARKET_NOT_FOUND("MARKET_NOT_FOUND", "会场资源不存在", 404),
    MARKET_ACTIVITY_UNAVAILABLE("MARKET_ACTIVITY_UNAVAILABLE", "活动不可用", 409),
    MARKET_TAG_NOT_MATCHED("MARKET_TAG_NOT_MATCHED", "当前用户不满足活动参与条件", 403),

    // ===== 交易订单 =====
    TRADE_TEAM_FULL("TRADE_TEAM_FULL", "拼团队伍名额已满", 409),
    TRADE_ORDER_STATUS_INVALID("TRADE_ORDER_STATUS_INVALID", "当前订单状态不允许执行该操作", 409),
    TRADE_PRICE_CHANGED("TRADE_PRICE_CHANGED", "订单价格已变化，请重新确认", 409),
    TRADE_ORDER_NOT_FOUND("TRADE_ORDER_NOT_FOUND", "订单不存在", 404),
    TRADE_ORDER_FORBIDDEN("TRADE_ORDER_FORBIDDEN", "无权访问当前订单", 403),

    // ===== 后台治理 =====
    ADMIN_INVALID_OPERATION("ADMIN_INVALID_OPERATION", "后台操作不合法", 409),

    // ===== 系统级 =====
    SYSTEM_PARAM_INVALID("SYSTEM_PARAM_INVALID", "请求参数错误", 400),  // 参数校验失败
    SYSTEM_ERROR("SYSTEM_ERROR", "系统开小差了，请稍后再试", 500);       // 未预期异常兜底

    // 每个枚举携带三个字段：业务错误码、默认消息、HTTP 状态码
    private final String code;       // 业务错误码，如 "TRADE_ORDER_NOT_FOUND"
    private final String message;    // 默认中文消息，BizException 可覆盖
    private final int httpStatus;    // HTTP 状态码，用于 ResponseEntity.status()

    ErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
