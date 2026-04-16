package io.github.izzcj.gamemaster.client;

import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.Set;

/**
 * ChatClient描述符
 *
 * @param name       对外暴露的名称
 * @param aliases    可选别名集合
 * @param chatClient ChatClient实例
 * @param isDefault  是否为默认客户端
 * @param metadata   预留给后续路由扩展的元数据
 */
public record ChatClientDescriptor(
        String name,
        Set<String> aliases,
        ChatClient chatClient,
        boolean isDefault,
        Map<String, Object> metadata
) {
}
