package io.github.izzcj.gamemaster.support.exception;

/**
 * 业务异常。
 *
 * <p>用于表示可以直接返回给调用方的规则校验失败、状态非法等业务错误。
 */
public class BusinessException extends RuntimeException {

    /**
     * 使用业务错误消息创建异常。
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
    }
}
