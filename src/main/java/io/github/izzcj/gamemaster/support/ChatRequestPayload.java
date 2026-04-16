package io.github.izzcj.gamemaster.support;

import lombok.Data;

import java.util.List;

/**
 * Chat请求载体
 *
 * @author Ale
 * @version 1.0.0
 */
@Data
public class ChatRequestPayload {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 对话ID
     */
    private String chatId;

    /**
     * 聊天请求消息
     */
    private String message;

    /**
     * chatClient
     */
    private String chatClient;

    /**
     * agent
     */
    private String agent;

}
