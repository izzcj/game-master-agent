package io.github.izzcj.gamemaster.controller;

import io.github.izzcj.gamemaster.exception.ChatClientNotFoundException;
import io.github.izzcj.gamemaster.exception.DuplicateChatClientException;
import io.github.izzcj.gamemaster.exception.InvalidChatClientConfigurationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将业务异常映射为稳定的HTTP响应。
 *
 * @author Ale
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理ChatClient未找到的异常。
     */
    @ExceptionHandler(ChatClientNotFoundException.class)
    public ProblemDetail handleChatClientNotFound(ChatClientNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Chat client not found");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    /**
     * 处理ChatClient配置错误的异常。
     */
    @ExceptionHandler({DuplicateChatClientException.class, InvalidChatClientConfigurationException.class})
    public ProblemDetail handleChatClientConfiguration(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Chat client configuration error");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }
}
