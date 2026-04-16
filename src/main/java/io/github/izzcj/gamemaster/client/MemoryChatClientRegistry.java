package io.github.izzcj.gamemaster.client;

import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的ChatClient注册中心
 *
 * @author Ale
 * @version 1.0.0
 */
public class MemoryChatClientRegistry implements ChatClientRegistry {

    /**
     * ChatClient映射表
     */
    private final Map<String, ChatClient> chatClientMap = new ConcurrentHashMap<>();

    @Override
    public void registerChatClient(String name, ChatClient chatClient) {
        this.chatClientMap.put(name, chatClient);
    }

    @Override
    public @NonNull ChatClient findChatClient(String name) throws IllegalArgumentException {
        ChatClient chatClient = this.chatClientMap.get(name);
        if (chatClient == null) {
            throw new IllegalArgumentException(String.format("ChatClient %s not found! ", name));
        }
        return chatClient;
    }
}
