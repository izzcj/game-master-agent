package io.github.izzcj.gamemaster.model.response;

import java.util.List;

/**
 * 问答响应。
 *
 * @param sessionId 会话 ID
 * @param answer 回答文本
 * @param resolvedGame 解析出的游戏名
 * @param retrievalPlan 检索计划
 * @param citations 引用列表
 */
public record ChatQueryResponse(
    String sessionId,
    String answer,
    String resolvedGame,
    RetrievalPlanResponse retrievalPlan,
    List<CitationResponse> citations
) {
}
