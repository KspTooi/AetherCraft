package com.ksptool.ql.commons.web;

/**
 * SSE响应结果包装类
 * @param <T> 数据类型
 */
public class SseResult<T> {
    
    /**
     * 状态码
     * 0 - 开始
     * 1 - 数据发送
     * 2 - 结束
     * 10 - 错误
     */
    private final int code;

    /**
     * 数据内容
     */
    private final T data;

    private SseResult(int code, T data) {
        this.code = code;
        this.data = data;
    }

    /**
     * 开始事件
     * @param data 数据内容
     * @return SseResult实例
     * @param <T> 数据类型
     */
    public static <T> SseResult<T> start(T data) {
        return new SseResult<>(0, data);
    }

    /**
     * 数据发送事件
     * @param data 数据内容
     * @return SseResult实例
     * @param <T> 数据类型
     */
    public static <T> SseResult<T> data(T data) {
        return new SseResult<>(1, data);
    }

    /**
     * 结束事件
     * @param data 数据内容
     * @return SseResult实例
     * @param <T> 数据类型
     */
    public static <T> SseResult<T> end(T data) {
        return new SseResult<>(2, data);
    }

    /**
     * 错误事件
     * @param data 错误信息
     * @return SseResult实例
     * @param <T> 数据类型
     */
    public static <T> SseResult<T> error(T data) {
        return new SseResult<>(10, data);
    }

    // Getter方法
    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }
} 