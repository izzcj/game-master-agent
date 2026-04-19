package io.github.izzcj.gamemaster.tool;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * MCP配置项
 *
 * @author Ale
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "gamemaster.mcp")
public class GameMasterMcpProperties {

    /**
     * 是否启用MCP功能
     */
    private boolean enabled = true;

    /**
     * 连接名称
     */
    private String connectionName = "tavily";

    /**
     * 允许使用的工具名称列表
     */
    private List<String> toolNames = List.of("search_web_for_walkthrough");
}
