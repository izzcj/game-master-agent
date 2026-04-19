package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientResolver;
import io.github.izzcj.gamemaster.support.ChatRequestPayload;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.List;

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
        List<Advisor> advisors = this.advisors(payload);
        ToolCallback[] toolCallbacks = this.toolCallbacks(payload);
        return chatClient.prompt()
                .system(this.systemPrompt())
                .user(payload.getMessage())
                .toolCallbacks(toolCallbacks)
                .advisors(a -> {
                    a.param(ChatMemory.CONVERSATION_ID, payload.getChatId());
                    if (!advisors.isEmpty()) {
                        a.advisors(advisors);
                    }
                })
                .call()
                .content();
    }

    @Override
    public Flux<String> chatStream(ChatRequestPayload payload) {
        ChatClient chatClient = this.chatClientResolver.resolveOrDefault(payload.getChatClient());
        List<Advisor> advisors = this.advisors(payload);
        ToolCallback[] toolCallbacks = this.toolCallbacks(payload);
        return chatClient.prompt()
                .system(this.systemPrompt())
                .user(payload.getMessage())
                .toolCallbacks(toolCallbacks)
                .advisors(a -> {
                    a.param(ChatMemory.CONVERSATION_ID, payload.getChatId());
                    if (!advisors.isEmpty()) {
                        a.advisors(advisors);
                    }
                })
                .stream()
                .content();
    }

    /**
     * 配置额外的Advisor
     */
    protected List<Advisor> advisors(ChatRequestPayload payload) {
        return List.of();
    }

    /**
     * 配置ToolCallback
     */
    protected ToolCallback[] toolCallbacks(ChatRequestPayload payload) {
        return new ToolCallback[0];
    }

    /**
     * 返回当前Agent的系统提示词。
     */
    protected abstract String systemPrompt();
}
