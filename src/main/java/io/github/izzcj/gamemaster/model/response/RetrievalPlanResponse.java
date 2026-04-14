package io.github.izzcj.gamemaster.model.response;

import java.util.List;

/**
 * 检索计划响应。
 *
 * @param queryText 实际检索词
 * @param useExternalSearch 是否启用外部搜索
 * @param topK 召回条数
 * @param knowledgeBaseIds 检索范围
 */
public record RetrievalPlanResponse(
    String queryText,
    boolean useExternalSearch,
    int topK,
    List<String> knowledgeBaseIds
) {
}
