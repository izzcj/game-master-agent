package io.github.izzcj.tavilymcp.model;

import java.util.List;

/**
 * MCP 搜索工具返回给主服务的网页搜索结果
 *
 * @param query 搜索查询
 * @param answer Tavily 生成的摘要答案
 * @param results 搜索结果列表
 * @param suggestedCitationUrls 推荐引用链接列表
 * @author Ale
 * @version 1.0.0
 */
public record SearchWebResult(
        String query,
        String answer,
        List<SearchWebResultItem> results,
        List<String> suggestedCitationUrls
) {

    /**
     * 构建空搜索结果。
     *
     * @param query 搜索查询
     * @return 空搜索结果
     */
    public static SearchWebResult empty(String query) {
        return new SearchWebResult(query, null, List.of(), List.of());
    }
}
