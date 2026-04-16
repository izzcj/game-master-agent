package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 游戏攻略Agent
 * 重点在提供游戏攻略
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GameWalkthroughAgent {

    /**
     * ChatClient解析器
     */
    private final ChatClientResolver chatClientResolver;

    /**
     * 系统提示词
     */
    private static final String SYSTEM_PROMPT = """
            你是一个长期活跃在各大游戏社区及wiki的资深游戏攻略制作者，你需要根据用户的描述，
            提供详细的游戏攻略，包括但不限于游戏玩法、技巧、关卡攻略、角色介绍等。
            """;

    /**
     * 聊天
     *
     * @param chatClientName ChatClient名称
     * @param prompt         提示词
     * @return 回复内容
     */
    public String chat(String chatClientName, String prompt) {
        ChatClient chatClient = this.chatClientResolver.resolveOrDefault(chatClientName);
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 聊天流式响应
     *
     * @param chatClientName ChatClient名称
     * @param prompt         提示词
     * @return 流式回复内容
     */
    public Flux<String> chatStream(String chatClientName, String prompt) {
        ChatClient chatClient = this.chatClientResolver.resolveOrDefault(chatClientName);
        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .stream()
                .content();
    }
}
