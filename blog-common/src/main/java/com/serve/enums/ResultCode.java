package com.serve.enums;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(400, "操作失败"),
    UNAUTHORIZED(401, "未登录或token失效"),
    FORBIDDEN(403, "无权限访问"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"), // 限流专用
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
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
