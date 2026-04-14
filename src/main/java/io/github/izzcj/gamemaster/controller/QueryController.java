package io.github.izzcj.gamemaster.controller;

import io.github.izzcj.gamemaster.application.QueryApplicationService;
import io.github.izzcj.gamemaster.model.request.ChatQueryRequest;
import io.github.izzcj.gamemaster.model.response.ChatQueryResponse;
import io.github.izzcj.gamemaster.support.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 对话接口控制器。
 */
@RestController
@RequestMapping("/api/chat")
public class QueryController {

    private final QueryApplicationService queryApplicationService;

    public QueryController(QueryApplicationService queryApplicationService) {
        this.queryApplicationService = queryApplicationService;
    }

    /**
     * 普通问答接口。
     *
     * @param request 问答请求
     * @return 问答响应
     */
    @PostMapping("/query")
    public ApiResponse<ChatQueryResponse> query(@Valid @RequestBody ChatQueryRequest request) {
        return ApiResponse.ok(queryApplicationService.query(request));
    }

    /**
     * SSE 流式问答接口。
     *
     * <p>当前实现一次性返回完整结果，便于后续平滑替换为真正的流式输出。
     *
     * @param request 问答请求
     * @return SSE 发射器
     */
    @PostMapping(path = "/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody ChatQueryRequest request) {
        SseEmitter emitter = new SseEmitter(30_000L);
        try {
            ChatQueryResponse response = queryApplicationService.query(request);
            emitter.send(SseEmitter.event().name("answer").data(response));
            emitter.complete();
        } catch (Exception exception) {
            emitter.completeWithError(exception);
        }
        return emitter;
    }
}
