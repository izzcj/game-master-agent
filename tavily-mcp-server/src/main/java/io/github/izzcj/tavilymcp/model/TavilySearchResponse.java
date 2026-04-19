package io.github.izzcj.tavilymcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Tavily 搜索接口响应体
 *
 * @param answer Tavily 生成的摘要答案
 * @param results 搜索结果列表
 * @author Ale
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilySearchResponse(
        String answer,
        List<TavilySearchResponseItem> results
) {
}
