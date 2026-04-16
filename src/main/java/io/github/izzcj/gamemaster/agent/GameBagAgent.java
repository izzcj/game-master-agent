package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 游戏百宝袋Agent
 * 重点在寻找游戏
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GameBagAgent {

    /**
     * ChatClient解析器
     */
    private final ChatClientResolver chatClientResolver;

    /**
     * 系统提示词
     */
    private static final String SYSTEM_PROMPT = """
            你是一个玩过各种游戏的重度游戏玩家，能够根据用户的描述，
            帮助用户找到合适的游戏，回答时需要给出游戏名称、类型、简要介绍、平台、推荐原因。
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
