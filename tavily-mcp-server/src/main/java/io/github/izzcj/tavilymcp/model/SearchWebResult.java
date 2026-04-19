package io.github.izzcj.tavilymcp.model;

import java.util.List;

public record SearchWebResult(
        String query,
        String answer,
        List<SearchWebResultItem> results,
        List<String> suggestedCitationUrls
) {

    public static SearchWebResult empty(String query) {
        return new SearchWebResult(query, null, List.of(), List.of());
    }
}
