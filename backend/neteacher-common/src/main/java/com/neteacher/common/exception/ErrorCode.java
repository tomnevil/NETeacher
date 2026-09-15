package com.neteacher.common.exception;

import lombok.Getter;

/**
 * 统一错误码。建议业务模块在各自包内扩展本枚举或新增业务错误码常量。
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),
    SYSTEM_ERROR(500, "系统异常，请稍后重试"),
    PARAM_INVALID(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权访问该资源"),
    NOT_FOUND(404, "资源不存在"),
    BIZ_ERROR(1000, "业务异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
