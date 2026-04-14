package io.github.izzcj.gamemaster.rag;

import java.util.List;

/**
 * 检索执行计划。
 *
 * @param queryText 检索文本
 * @param useExternalSearch 是否启用外部搜索
 * @param topK 召回条数
 * @param knowledgeBaseIds 检索的知识库范围
 * @param resolvedGame 解析出的目标游戏
 * @param platform 目标平台
 */
public record RetrievalPlan(
    String queryText,
    boolean useExternalSearch,
    int topK,
    List<String> knowledgeBaseIds,
    String resolvedGame,
    String platform
) {
}
