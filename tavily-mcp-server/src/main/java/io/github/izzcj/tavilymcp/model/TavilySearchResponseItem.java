package io.github.izzcj.tavilymcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tavily 搜索接口返回的单条结果
 *
 * @param title 搜索结果标题
 * @param url 搜索结果链接
 * @param content 页面摘要内容
 * @param rawContent 页面原始内容
 * @author Ale
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilySearchResponseItem(
        String title,
        String url,
        String content,
        @JsonProperty("raw_content") String rawContent
) {
}
