package io.github.izzcj.gamemaster.controller;

import io.github.izzcj.gamemaster.agent.GameMasterAgentRouter;
import io.github.izzcj.gamemaster.support.ChatRequestPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 聊天接口层
 *
 * @author Ale
 * @version 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/chat")
public class ChatController {

    /**
     * 游戏大师agent
     */
    private final GameMasterAgentRouter gameMasterAgentRouter;

    /**
     * 聊天
     *
     * @param payload        请求载体
     * @return 回复内容
     */
    @PostMapping
    public String chat(@RequestBody ChatRequestPayload payload) {
        return this.gameMasterAgentRouter.chat(payload.getAgent(), payload.getChatClient(), payload.getMessage());
    }

    /**
     * 聊天流式响应
     *
     * @param payload        请求载体
     * @return 流式回复内容
     */
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequestPayload payload) {
        return this.gameMasterAgentRouter.chatStream(payload.getAgent(), payload.getChatClient(), payload.getMessage());
    }
}
