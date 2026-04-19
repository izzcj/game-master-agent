package io.github.izzcj.tavilymcp.tool;

import io.github.izzcj.tavilymcp.config.TavilyProperties;
import io.github.izzcj.tavilymcp.model.SearchWebResult;
import io.github.izzcj.tavilymcp.service.TavilySearchService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Tavily MCP 搜索工具
 *
 * @author Ale
 * @version 1.0.0
 */
@Component
public class TavilySearchTools {

    private final TavilySearchService tavilySearchService;
    private final TavilyProperties properties;

    /**
     * 创建 Tavily MCP 搜索工具。
     *
     * @param tavilySearchService Tavily 搜索服务
     * @param properties Tavily 配置项
     */
    public TavilySearchTools(TavilySearchService tavilySearchService, TavilyProperties properties) {
        this.tavilySearchService = tavilySearchService;
        this.properties = properties;
    }

    /**
     * 面向攻略场景搜索网页资料。
     *
     * @param query 攻略主题搜索词
     * @param game 可选游戏名称
     * @param maxResults 可选搜索结果数
     * @return 网页搜索结果
     */
    @McpTool(name = "search_web_for_walkthrough",
            description = "Search wiki pages, forum discussions, boss guides, builds, and walkthrough pages for games.")
    public SearchWebResult searchWebForWalkthrough(
            @McpToolParam(description = "Search query for the walkthrough topic", required = true) String query,
            @McpToolParam(description = "Optional game name to improve search precision") String game,
            @McpToolParam(description = "Optional requested result count, clamped by server policy") Integer maxResults
    ) {
        if (!this.properties.isEnabled() || !StringUtils.hasText(this.properties.getApiKey())) {
            return SearchWebResult.empty(query);
        }

        String finalQuery = StringUtils.hasText(game) ? game + " " + query : query;
        try {
            return this.tavilySearchService.search(finalQuery, "general" , maxResults == null ? 0 : maxResults);
        }
        catch (RuntimeException ex) {
            return SearchWebResult.empty(finalQuery);
        }
    }
}
