package io.github.izzcj.tavilymcp.tool;

import io.github.izzcj.tavilymcp.config.TavilyProperties;
import io.github.izzcj.tavilymcp.model.SearchWebResult;
import io.github.izzcj.tavilymcp.service.TavilySearchService;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TavilySearchTools {

    private final TavilySearchService tavilySearchService;
    private final TavilyProperties properties;

    public TavilySearchTools(TavilySearchService tavilySearchService, TavilyProperties properties) {
        this.tavilySearchService = tavilySearchService;
        this.properties = properties;
    }

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
