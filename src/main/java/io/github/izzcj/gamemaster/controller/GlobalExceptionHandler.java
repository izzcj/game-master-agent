package io.github.izzcj.gamemaster.controller;

import io.github.izzcj.gamemaster.exception.AgentNotFoundException;
import io.github.izzcj.gamemaster.exception.ChatClientNotFoundException;
import io.github.izzcj.gamemaster.exception.DuplicateAgentException;
import io.github.izzcj.gamemaster.exception.DuplicateChatClientException;
import io.github.izzcj.gamemaster.exception.InvalidAgentConfigurationException;
import io.github.izzcj.gamemaster.exception.InvalidChatClientConfigurationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将业务异常映射为稳定的HTTP响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatClientNotFoundException.class)
    public ProblemDetail handleChatClientNotFound(ChatClientNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Chat client not found");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(AgentNotFoundException.class)
    public ProblemDetail handleAgentNotFound(AgentNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Agent not found");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler({
            DuplicateChatClientException.class,
            InvalidChatClientConfigurationException.class,
            DuplicateAgentException.class,
            InvalidAgentConfigurationException.class
    })
    public ProblemDetail handleConfiguration(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Application configuration error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}
