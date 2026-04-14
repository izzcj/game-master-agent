package io.github.izzcj.gamemaster.application;

import io.github.izzcj.gamemaster.model.request.ChatQueryRequest;
import io.github.izzcj.gamemaster.rag.RetrievalPlan;
import io.github.izzcj.gamemaster.search.SearchProperties;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 检索计划生成器。
 */
@Service
public class RetrievalPlanner {

    private final SearchProperties searchProperties;

    public RetrievalPlanner(SearchProperties searchProperties) {
        this.searchProperties = searchProperties;
    }

    /**
     * 基于请求与游戏识别结果生成检索计划。
     *
     * @param request 问答请求
     * @param resolvedGame 识别出的游戏
     * @return 检索计划
     */
    public RetrievalPlan plan(ChatQueryRequest request, ResolvedGame resolvedGame) {
        List<String> knowledgeBaseIds = request.getKnowledgeBaseIds();
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            knowledgeBaseIds = List.of("kb-default");
        }
        boolean useExternalSearch = Boolean.TRUE.equals(request.getUseExternalSearch());
        return new RetrievalPlan(
            request.getQuestion(),
            useExternalSearch,
            searchProperties.getDefaultTopK(),
            knowledgeBaseIds,
            resolvedGame.gameName(),
            request.getPlatform()
        );
    }
}
