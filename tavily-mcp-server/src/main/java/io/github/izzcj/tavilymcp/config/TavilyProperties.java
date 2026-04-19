package io.github.izzcj.tavilymcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Tavily 搜索服务配置项
 *
 * @author Ale
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "tavily")
public class TavilyProperties {

    /**
     * 是否启用 Tavily 搜索能力
     */
    private boolean enabled = true;

    /**
     * Tavily API Key
     */
    private String apiKey;

    /**
     * Tavily API 基础地址
     */
    private String baseUrl = "https://api.tavily.com";

    /**
     * 单次搜索允许返回的最大结果数
     */
    private int maxResults = 5;

    /**
     * 默认搜索主题
     */
    private String topic = "general";

    /**
     * 默认搜索深度
     */
    private String searchDepth = "basic";

    /**
     * 搜索时限定包含的域名列表
     */
    private List<String> includeDomains = List.of();

    /**
     * 搜索时排除的域名列表
     */
    private List<String> excludeDomains = List.of();

    /**
     * Tavily 请求超时时间
     */
    private Duration timeout = Duration.ofSeconds(8);

    /**
     * 是否在响应中包含 Tavily 生成的答案
     */
    private boolean includeAnswer = true;

    /**
     * 是否在响应中包含原始页面内容
     */
    private boolean includeRawContent = false;
}
