package io.github.izzcj.gamemaster.client;

import java.util.List;
import java.util.Optional;

/**
 * ChatClient注册中心
 *
 * @author Ale
 * @version 1.0.0
 */
public interface ChatClientRegistry {

    /**
     * 注册ChatClient描述信息。
     *
     * @param descriptor ChatClient描述信息
     */
    void register(ChatClientDescriptor descriptor);

    /**
     * 根据名称或别名查找ChatClient描述信息。
     *
     * @param name 客户端名称或别名
     * @return 匹配到的描述信息
     */
    Optional<ChatClientDescriptor> findByName(String name);

    /**
     * 获取所有已注册的ChatClient。
     *
     * @return 描述信息列表
     */
    List<ChatClientDescriptor> list();

    /**
     * 获取默认ChatClient。
     *
     * @return 默认客户端描述信息
     */
    Optional<ChatClientDescriptor> getDefault();
}
