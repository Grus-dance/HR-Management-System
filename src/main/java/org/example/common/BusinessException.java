package org.example.common;

import lombok.Getter;

/**
 * 业务异常：由全局异常处理器转为统一响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(40001, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(40101, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(40301, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(40401, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(40901, message);
    }
}
