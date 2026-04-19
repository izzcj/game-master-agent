package io.github.izzcj.tavilymcp.model;

/**
 * MCP 搜索工具返回的单条网页搜索结果
 *
 * @param title 搜索结果标题
 * @param url 搜索结果链接
 * @param siteName 站点名称
 * @param snippet 搜索结果摘要
 * @author Ale
 * @version 1.0.0
 */
public record SearchWebResultItem(
        String title,
        String url,
        String siteName,
        String snippet
) {
}
