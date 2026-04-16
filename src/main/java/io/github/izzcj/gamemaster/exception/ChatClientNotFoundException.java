package io.github.izzcj.gamemaster.exception;

/**
 * ChatClient解析失败异常
 *
 * @author Ale
 * @version 1.0.0
 */
public class ChatClientNotFoundException extends RuntimeException {

    public ChatClientNotFoundException(String message) {
        super(message);
    }
}
