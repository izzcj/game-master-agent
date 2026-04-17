package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientResolver;
import io.github.izzcj.gamemaster.support.ChatRequestPayload;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import reactor.core.publisher.Flux;

/**
 * 游戏大师Agent抽象基类
 *
 * @author Ale
 * @version 1.0.0
 */
public abstract class AbstractGameMasterAgent implements GameMasterAgent {

    /**
     * ChatClient解析器
     */
    private final ChatClientResolver chatClientResolver;

    protected AbstractGameMasterAgent(ChatClientResolver chatClientResolver) {
        this.chatClientResolver = chatClientResolver;
    }

    @Override
    public String chat(ChatRequestPayload payload) {
        ChatClient chatClient = this.chatClientResolver.resolveOrDefault(payload.getChatClient());
        return chatClient.prompt()
                .system(this.systemPrompt())
                .user(payload.getMessage())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, payload.getChatId()))
                .call()
                .content();
    }

    @Override
    public Flux<String> chatStream(ChatRequestPayload payload) {
        ChatClient chatClient = this.chatClientResolver.resolveOrDefault(payload.getChatClient());
        return chatClient.prompt()
                .system(this.systemPrompt())
                .user(payload.getMessage())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, payload.getChatId()))
                .stream()
                .content();
    }

    /**
     * 返回当前Agent的系统提示词。
     */
    protected abstract String systemPrompt();
}
