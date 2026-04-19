package io.github.izzcj.tavilymcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
