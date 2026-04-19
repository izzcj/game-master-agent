package io.github.izzcj.tavilymcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Tavily 搜索接口请求体
 *
 * @param apiKey Tavily API Key
 * @param query 搜索查询
 * @param topic 搜索主题
 * @param searchDepth 搜索深度
 * @param maxResults 最大结果数
 * @param includeAnswer 是否返回摘要答案
 * @param includeRawContent 是否返回原始页面内容
 * @param includeDomains 包含的域名列表
 * @param excludeDomains 排除的域名列表
 * @author Ale
 * @version 1.0.0
 */
public record TavilySearchRequest(
        @JsonProperty("api_key") String apiKey,
        String query,
        String topic,
        @JsonProperty("search_depth") String searchDepth,
        @JsonProperty("max_results") int maxResults,
        @JsonProperty("include_answer") boolean includeAnswer,
        @JsonProperty("include_raw_content") boolean includeRawContent,
        @JsonProperty("include_domains") List<String> includeDomains,
        @JsonProperty("exclude_domains") List<String> excludeDomains
) {
}
