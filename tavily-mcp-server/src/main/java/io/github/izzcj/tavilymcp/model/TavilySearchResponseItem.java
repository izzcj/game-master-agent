package io.github.izzcj.tavilymcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilySearchResponseItem(
        String title,
        String url,
        String content,
        @JsonProperty("raw_content") String rawContent
) {
}
