package com.example.backend.common;

public enum ErrorCode {
    SUCCESS(20000, "success"),
    BAD_REQUEST(40001, "bad request"),
    INVALID_PARAM(40002, "invalid parameter"),
    UNAUTHORIZED(40100, "unauthorized"),
    TOKEN_EXPIRED(40101, "token expired"),
    TOKEN_INVALID(40102, "token invalid"),
    FORBIDDEN(40300, "forbidden"),
    CREDIT_INSUFFICIENT(40301, "credit insufficient"),
    NOT_CERTIFIED(40302, "not certified"),
    ACCOUNT_BANNED(40303, "account banned"),
    NOT_FOUND(40400, "resource not found"),
    USER_NOT_FOUND(40401, "user not found"),
    DEMAND_NOT_FOUND(40402, "demand not found"),
    ORDER_NOT_FOUND(40403, "order not found"),
    CERTIFICATION_NOT_FOUND(40404, "certification not found"),
    CONFLICT(40900, "business conflict"),
    DEMAND_ALREADY_ACCEPTED(40901, "demand already accepted"),
    ORDER_CANCELLED(40902, "order cancelled"),
    REVIEW_DUPLICATED(40903, "review duplicated"),
    USERNAME_EXISTS(40904, "username already exists"),
    STUDENT_ID_EXISTS(40905, "student id already exists"),
    CERTIFICATION_PENDING_EXISTS(40906, "pending certification already exists"),
    CERTIFICATION_ALREADY_APPROVED(40907, "certification already approved"),
    SERVER_ERROR(50000, "internal server error");

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
