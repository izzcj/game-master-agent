package io.github.izzcj.gamemaster.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * 默认ChatClient日志实现
 * 控制台输出
 *
 * @author Ale
 * @version 1.0.0
 **/
@Slf4j
public class DefaultChatClientLogging implements ChatClientLogging {

    @Override
    public void logResponse(ChatClientResponse chatClientResponse) {
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse == null) {
            return;
        }
        log.info("输入token: {}", chatResponse.getMetadata().getUsage().getPromptTokens());
        log.info("输出token: {}", chatResponse.getMetadata().getUsage().getCompletionTokens());
        log.info("总token: {}", chatResponse.getMetadata().getUsage().getTotalTokens());
    }
}
