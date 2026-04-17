package io.github.izzcj.gamemaster.agent;

import io.github.izzcj.gamemaster.support.ChatRequestPayload;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * 游戏大师Agent统一接口
 *
 * @author Ale
 * @version 1.0.0
 */
public interface GameMasterAgent {

    /**
     * Agent标准名称
     */
    String name();

    /**
     * Agent别名集合
     */
    default Set<String> aliases() {
        return Set.of();
    }

    /**
     * 是否为默认Agent
     */
    default boolean isDefault() {
        return false;
    }

    /**
     * 同步聊天
     *
     * @param payload 请求载体
     */
    String chat(ChatRequestPayload payload);

    /**
     * 流式聊天
     *
     * @param payload 请求载体
     */
    Flux<String> chatStream(ChatRequestPayload payload);
}
