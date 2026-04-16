package io.github.izzcj.gamemaster.exception;

/**
 * ChatClient注册相同名称或别名异常
 *
 * @author Ale
 * @version 1.0.0
 */
public class DuplicateChatClientException extends RuntimeException {

    public DuplicateChatClientException(String message) {
        super(message);
    }
}
