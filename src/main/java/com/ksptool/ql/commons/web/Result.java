package com.ksptool.ql.commons.web;

import com.ksptool.ql.commons.exception.BizException;
import org.springframework.validation.BindingResult;

public class Result<T> {

    /**
     * 业务状态码
     * 0 - 业务正常
     * 1 - 业务异常
     * 2 - 内部服务器错误
     */
    private final int code;

    /**
     * 描述信息
     */
    private final String message;

    /**
     * 返回数据
     */
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 业务正常
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    // 业务异常
    public static <T> Result<T> error(String message) {
        return new Result<>(1, message, null);
    }

    public static <T> Result<T> error(BindingResult br) {
        return new Result<>(1, br.getAllErrors().getFirst().getDefaultMessage(), null);
    }

    // 业务异常
    public static <T> Result<T> error(BizException e) {
        return new Result<>(1, e.getMessage(), null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    // 内部服务器错误
    public static <T> Result<Object> internalError(String message,Object throwable) {
        return new Result<>(2, message, throwable);
    }

    // Getter 方法
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}