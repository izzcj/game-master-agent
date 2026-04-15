package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.client.ChatClientRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 游戏大师APP
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GameMasterAgent {

    /**
     * ChatClient注册中心
     */
    private final ChatClientRegister chatClientRegister;

    /**
     * 聊天
     *
     * @param chatClientName ChatClient名称
     * @param prompt         提示词
     * @return 回复内容
     */
    public String chat(String chatClientName, String prompt) {
        ChatClient chatClient = chatClientRegister.findChatClient(chatClientName);
        return chatClient.prompt()
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
        ChatClient chatClient = chatClientRegister.findChatClient(chatClientName);
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
