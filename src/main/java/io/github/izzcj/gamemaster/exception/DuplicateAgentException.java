package io.github.izzcj.gamemaster.exception;

/**
 * Agent名称或别名冲突异常
 *
 * @author Ale
 * @version 1.0.0
 */
public class DuplicateAgentException extends RuntimeException {

    public DuplicateAgentException(String message) {
        super(message);
    }
}
