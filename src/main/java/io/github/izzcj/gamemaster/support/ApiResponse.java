package io.github.izzcj.gamemaster.support;

/**
 * 统一接口响应包装。
 *
 * @param success 是否处理成功
 * @param data 响应数据
 * @param message 响应消息
 * @param <T> 数据类型
 */
public record ApiResponse<T>(boolean success, T data, String message) {

    /**
     * 构造成功响应，默认消息为 {@code OK}。
     *
     * @param data 响应数据
     * @return 成功响应
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "OK");
    }

    /**
     * 构造带自定义消息的成功响应。
     *
     * @param data 响应数据
     * @param message 自定义消息
     * @return 成功响应
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    /**
     * 构造失败响应。
     *
     * @param message 失败消息
     * @return 失败响应
     * @param <T> 数据类型
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
