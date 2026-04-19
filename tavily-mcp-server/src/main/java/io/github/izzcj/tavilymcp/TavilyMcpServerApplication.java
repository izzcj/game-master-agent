package io.github.izzcj.tavilymcp;

import io.github.izzcj.tavilymcp.config.TavilyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Tavily MCP 服务启动类
 *
 * @author Ale
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigurationProperties(TavilyProperties.class)
public class TavilyMcpServerApplication {

    /**
     * 启动 Tavily MCP 服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TavilyMcpServerApplication.class, args);
    }
}
