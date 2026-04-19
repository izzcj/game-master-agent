package io.github.izzcj.tavilymcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TavilySearchResponse(
        String answer,
        List<TavilySearchResponseItem> results
) {
}
