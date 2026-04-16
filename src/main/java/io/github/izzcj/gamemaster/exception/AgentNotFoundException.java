package io.github.izzcj.gamemaster.exception;

/**
 * Raised when the request specifies an unsupported agent.
 *
 * @author Ale
 * @version 1.0.0
 */
public class AgentNotFoundException extends RuntimeException {

    public AgentNotFoundException(String message) {
        super(message);
    }
}
