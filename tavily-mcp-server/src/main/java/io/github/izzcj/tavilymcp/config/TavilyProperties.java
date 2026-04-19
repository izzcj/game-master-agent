package io.github.izzcj.tavilymcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "tavily")
public class TavilyProperties {

    private boolean enabled = true;

    private String apiKey;

    private String baseUrl = "https://api.tavily.com";

    private int maxResults = 5;

    private String topic = "general";

    private String searchDepth = "basic";

    private List<String> includeDomains = List.of();

    private List<String> excludeDomains = List.of();

    private Duration timeout = Duration.ofSeconds(8);

    private boolean includeAnswer = true;

    private boolean includeRawContent = false;
}
