package io.github.izzcj.gamemaster.client;

import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于内存的ChatClient注册中心
 *
 * @author Ale
 * @version 1.0.0
 */
public class MemoryChatClientRegister implements ChatClientRegister {

    /**
     * ChatClient映射表
     */
    private final Map<String, ChatClient> chatClientMap = new ConcurrentHashMap<>();

    public MemoryChatClientRegister(Map<String, ChatClient> chatClientMap) {
        this.chatClientMap.putAll(chatClientMap);
    }

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
