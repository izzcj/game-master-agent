package io.github.izzcj.gamemaster.client;

import org.springframework.ai.chat.client.ChatClient;

/**
 * ChatClient解析器
 *
 * @author Ale
 * @version 1.0.0
 */
public interface ChatClientResolver {

    /**
     * 根据显式名称或别名解析ChatClient。
     *
     * @param name 客户端名称或别名
     * @return ChatClient实例
     */
    ChatClient resolve(String name);

    /**
     * 优先按显式名称解析，否则回退到默认客户端。
     *
     * @param name 请求指定的客户端名称
     * @return ChatClient实例
     */
    ChatClient resolveOrDefault(String name);
}
