package com.example.backend.common;

public enum ErrorCode {
    SUCCESS(20000, "操作成功"),
    BAD_REQUEST(40001, "请求参数错误"),
    INVALID_PARAM(40002, "参数格式无效"),
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    TOKEN_EXPIRED(40101, "登录已过期，请重新登录"),
    TOKEN_INVALID(40102, "Token无效"),
    FORBIDDEN(40300, "权限不足"),
    CREDIT_INSUFFICIENT(40301, "信用分不足，无法接单"),
    NOT_CERTIFIED(40302, "未完成实名认证"),
    ACCOUNT_BANNED(40303, "账号已被封禁"),
    QUOTA_INSUFFICIENT(40304, "额度分不足"),
    NOT_FOUND(40400, "资源不存在"),
    USER_NOT_FOUND(40401, "用户不存在"),
    DEMAND_NOT_FOUND(40402, "需求不存在"),
    ORDER_NOT_FOUND(40403, "订单不存在"),
    CERTIFICATION_NOT_FOUND(40404, "认证记录不存在"),
    RATE_LIMITED(42900, "操作过于频繁，请稍后再试"),
    CONFLICT(40900, "操作冲突"),
    DEMAND_ALREADY_ACCEPTED(40901, "该需求已被接单"),
    ORDER_CANCELLED(40902, "订单已取消"),
    REVIEW_DUPLICATED(40903, "请勿重复评价"),
    USERNAME_EXISTS(40904, "用户名已被占用"),
    STUDENT_ID_EXISTS(40905, "学号已被占用"),
    CERTIFICATION_PENDING_EXISTS(40906, "已有待审核的认证申请"),
    CERTIFICATION_ALREADY_APPROVED(40907, "认证已通过，无需重复提交"),
    SERVER_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
