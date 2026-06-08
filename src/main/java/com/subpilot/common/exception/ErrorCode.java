package com.subpilot.common.exception;

public enum ErrorCode {
    SUCCESS(0, "success"),
    PARAM_ERROR(40001, "参数错误"),
    UNAUTHORIZED(40100, "未登录"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "资源冲突"),
    SYSTEM_ERROR(50000, "系统错误");

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
