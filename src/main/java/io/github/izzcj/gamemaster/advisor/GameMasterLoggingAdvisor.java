package io.github.izzcj.gamemaster.advisor;

import io.github.izzcj.gamemaster.log.ChatClientLogging;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 游戏大师日志Advisor
 *
 * @author Ale
 * @version 1.0.0
 **/
@Slf4j
public class GameMasterLoggingAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * chatClient日志
     */
    private final ChatClientLogging chatClientLogging;

    public GameMasterLoggingAdvisor(ChatClientLogging chatClientLogging) {
        this.chatClientLogging = chatClientLogging;
    }

    @Nonnull
    @Override
    public ChatClientResponse adviseCall(@Nonnull ChatClientRequest chatClientRequest, @NonNull CallAdvisorChain callAdvisorChain) {
        this.chatClientLogging.logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.chatClientLogging.logResponse(chatClientResponse);
        return chatClientResponse;
    }

    @Nonnull
    @Override
    public Flux<ChatClientResponse> adviseStream(@Nonnull ChatClientRequest chatClientRequest, @NonNull StreamAdvisorChain streamAdvisorChain) {
        this.chatClientLogging.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this.chatClientLogging::logResponse);
    }

    @Nonnull
    @Override
    public String getName() {
        return "GameMasterLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
