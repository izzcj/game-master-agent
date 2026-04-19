package io.github.izzcj.tavilymcp;

import io.github.izzcj.tavilymcp.config.TavilyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TavilyProperties.class)
public class TavilyMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TavilyMcpServerApplication.class, args);
    }
}
