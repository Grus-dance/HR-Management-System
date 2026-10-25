package org.example.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一响应体：code=0 表示成功，非 0 为业务错误码。
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "OK", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(0, "OK", null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
