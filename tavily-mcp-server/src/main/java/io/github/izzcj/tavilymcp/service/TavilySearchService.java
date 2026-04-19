package io.github.izzcj.tavilymcp.service;

import io.github.izzcj.tavilymcp.config.TavilyProperties;
import io.github.izzcj.tavilymcp.model.SearchWebResult;
import io.github.izzcj.tavilymcp.model.SearchWebResultItem;
import io.github.izzcj.tavilymcp.model.TavilySearchRequest;
import io.github.izzcj.tavilymcp.model.TavilySearchResponse;
import io.github.izzcj.tavilymcp.model.TavilySearchResponseItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TavilySearchService {

    private final RestClient restClient;
    private final TavilyProperties properties;

    @Autowired
    public TavilySearchService(RestClient.Builder restClientBuilder, TavilyProperties properties) {
        this(buildRestClient(restClientBuilder, properties), properties);
    }

    public TavilySearchService(RestClient restClient, TavilyProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public SearchWebResult search(String query, String focus, int maxResults) {
        if (!StringUtils.hasText(query)) {
            return SearchWebResult.empty(query);
        }

        TavilySearchRequest request = new TavilySearchRequest(
                this.properties.getApiKey(),
                query,
                resolveTopic(focus),
                this.properties.getSearchDepth(),
                clampMaxResults(maxResults),
                this.properties.isIncludeAnswer(),
                this.properties.isIncludeRawContent(),
                this.properties.getIncludeDomains(),
                this.properties.getExcludeDomains()
        );

        TavilySearchResponse response = this.restClient.post()
                .uri("/search")
                .body(request)
                .retrieve()
                .body(TavilySearchResponse.class);

        if (response == null || response.results() == null || response.results().isEmpty()) {
            return SearchWebResult.empty(query);
        }

        List<SearchWebResultItem> items = response.results().stream()
                .filter(Objects::nonNull)
                .map(this::mapItem)
                .toList();

        List<String> citationUrls = items.stream()
                .map(SearchWebResultItem::url)
                .filter(StringUtils::hasText)
                .toList();

        return new SearchWebResult(query, response.answer(), items, citationUrls);
    }

    public int clampMaxResults(int requestedMaxResults) {
        int configuredMaxResults = Math.max(1, this.properties.getMaxResults());
        if (requestedMaxResults <= 0) {
            return configuredMaxResults;
        }
        return Math.min(requestedMaxResults, configuredMaxResults);
    }

    private String resolveTopic(String focus) {
        return StringUtils.hasText(focus) ? focus : this.properties.getTopic();
    }

    private static RestClient buildRestClient(RestClient.Builder builder, TavilyProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeout());
        requestFactory.setReadTimeout(properties.getTimeout());
        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    private SearchWebResultItem mapItem(TavilySearchResponseItem item) {
        String snippet = StringUtils.hasText(item.content()) ? item.content() : item.rawContent();
        return new SearchWebResultItem(
                item.title(),
                item.url(),
                resolveSiteName(item.url()),
                snippet
        );
    }

    private String resolveSiteName(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        try {
            String host = URI.create(url).getHost();
            if (!StringUtils.hasText(host)) {
                return null;
            }
            return host.startsWith("www.") ? host.substring(4) : host;
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
