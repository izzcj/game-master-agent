package io.github.izzcj.gamemaster.client;

import io.github.izzcj.gamemaster.exception.ChatClientNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.StringUtils;

/**
 * 基于{@link ChatClientRegistry}的默认ChatClient解析器
 *
 * @author Ale
 * @version 1.0.0
 */
public class DefaultChatClientResolver implements ChatClientResolver {

    /**
     * ChatClient注册中心
     */
    private final ChatClientRegistry chatClientRegistry;

    public DefaultChatClientResolver(ChatClientRegistry chatClientRegistry) {
        this.chatClientRegistry = chatClientRegistry;
    }

    @Override
    public ChatClient resolve(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ChatClientNotFoundException("Chat client name must not be blank.");
        }
        return this.chatClientRegistry.findByName(name)
                .map(ChatClientDescriptor::chatClient)
                .orElseThrow(() -> new ChatClientNotFoundException("Chat client '%s' not found.".formatted(name)));
    }

    @Override
    public ChatClient resolveOrDefault(String name) {
        if (StringUtils.hasText(name)) {
            return this.resolve(name);
        }
        return this.chatClientRegistry.getDefault()
                .map(ChatClientDescriptor::chatClient)
                .orElseThrow(() -> new ChatClientNotFoundException("No default chat client configured."));
    }
}
