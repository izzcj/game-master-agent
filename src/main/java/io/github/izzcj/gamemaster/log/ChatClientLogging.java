package io.github.izzcj.gamemaster.log;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

/**
 * ChatClient日志
 *
 * @author Ale
 * @version 1.0.0
 **/
public interface ChatClientLogging {

    /**
     * ChatClientRequest日志
     *
     * @param chatClientRequest ChatClientRequest
     */
    default void logRequest(ChatClientRequest chatClientRequest) {
    }

    /**
     * ChatClientResponse日志
     *
     * @param chatClientResponse ChatClientResponse
     */
    void logResponse(ChatClientResponse chatClientResponse);

}
