package io.github.izzcj.gamemaster.client;

import jakarta.annotation.Nonnull;
import org.springframework.ai.chat.client.ChatClient;

/**
 * ChatClient注册中心
 *
 * @author Ale
 * @version 1.0.0
 */
public interface ChatClientRegister {

    /**
     * 注册ChatClient
     *
     * @param name       ChatClient名称
     * @param chatClient ChatClient实例
     */
    void registerChatClient(String name, ChatClient chatClient);

    /**
     * 根据名称查找ChatClient
     *
     * @param name ChatClient名称
     * @return ChatClient实例，如果未找到则返回null
     */
    @Nonnull
    ChatClient findChatClient(String name) throws IllegalArgumentException;

}
