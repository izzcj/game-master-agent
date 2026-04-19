package io.github.izzcj.gamemaster.tool;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Mcp工具适配器
 *
 * @author Ale
 * @version 1.0.0
 */
@Slf4j
public class McpToolCallbackAdapter implements ToolCallbackProvider {

    private final GameMasterMcpProperties properties;
    private final Supplier<SyncMcpToolCallbackProvider> providerSupplier;

    public McpToolCallbackAdapter(GameMasterMcpProperties properties,
                                  Supplier<SyncMcpToolCallbackProvider> providerSupplier) {
        this.properties = properties;
        this.providerSupplier = providerSupplier;
    }

    /**
     * 根据配置获取MCP工具回调。
     *
     * @param walkthroughEnabled 是否启用攻略功能
     * @return 工具回调数组
     */
    public ToolCallback[] toolCallbacks(boolean walkthroughEnabled) {
        if (!walkthroughEnabled || !this.properties.isEnabled()) {
            return new ToolCallback[0];
        }

        try {
            SyncMcpToolCallbackProvider provider = this.providerSupplier.get();
            if (provider == null) {
                return new ToolCallback[0];
            }

            ToolCallback[] callbacks = provider.getToolCallbacks();
            if (callbacks.length == 0) {
                return new ToolCallback[0];
            }

            Set<String> allowedToolNames = CollectionUtils.isEmpty(this.properties.getToolNames())
                    ? Set.of()
                    : new LinkedHashSet<>(this.properties.getToolNames());
            if (allowedToolNames.isEmpty()) {
                return callbacks;
            }

            String connectionName = this.properties.getConnectionName();
            return Arrays.stream(callbacks)
                    .filter(Objects::nonNull)
                    .filter(callback -> {
                        callback.getToolDefinition();
                        return true;
                    })
                    .filter(callback -> matchesAllowedTool(callback.getToolDefinition().name(), allowedToolNames, connectionName))
                    .toArray(ToolCallback[]::new);
        }
        catch (RuntimeException ex) {
            log.warn("Failed to resolve MCP tool callbacks for walkthrough agent, falling back to local capabilities.", ex);
            return new ToolCallback[0];
        }
    }

    @Nonnull
    @Override
    public ToolCallback[] getToolCallbacks() {
        return this.toolCallbacks(true);
    }

    /**
     * 检查回调名称是否在允许的工具名称列表中。
     *
     * @param callbackName 回调名称
     * @param allowedToolNames 允许的工具名称集合
     * @param connectionName 连接名称
     * @return 是否匹配
     */
    private boolean matchesAllowedTool(String callbackName, Set<String> allowedToolNames, String connectionName) {
        if (callbackName == null) {
            return false;
        }
        return allowedToolNames.stream().anyMatch(toolName -> matchesToolName(callbackName, toolName, connectionName));
    }

    /**
     * 检查回调名称是否匹配工具名称。
     *
     * @param callbackName 回调名称
     * @param toolName 工具名称
     * @param connectionName 连接名称
     * @return 是否匹配
     */
    private boolean matchesToolName(String callbackName, String toolName, String connectionName) {
        if (callbackName.equals(toolName)) {
            return true;
        }

        boolean nameMatches = callbackName.endsWith(toolName);
        if (!nameMatches) {
            return false;
        }

        return connectionName == null || connectionName.isBlank() || callbackName.contains(connectionName);
    }
}
