package io.github.izzcj.gamemaster.client;

import io.github.izzcj.gamemaster.exception.DuplicateChatClientException;
import io.github.izzcj.gamemaster.exception.InvalidChatClientConfigurationException;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存的ChatClient注册中心
 *
 * @author Ale
 * @version 1.0.0
 */
public class MemoryChatClientRegistry implements ChatClientRegistry {

    /**
     * 按名称索引的ChatClient描述信息
     */
    private final Map<String, ChatClientDescriptor> chatClientDescriptorMap = new ConcurrentHashMap<>();

    /**
     * 别名到标准名称的映射
     */
    private final Map<String, String> aliases = new ConcurrentHashMap<>();

    /**
     * 默认客户端名称
     */
    private volatile String defaultClientName;

    @Override
    public synchronized void register(ChatClientDescriptor descriptor) {
        this.validateDescriptor(descriptor);

        String normalizedName = normalize(descriptor.name());
        if (this.chatClientDescriptorMap.containsKey(normalizedName)) {
            throw new DuplicateChatClientException("Chat client name '%s' is already registered.".formatted(descriptor.name()));
        }

        this.ensureAliasesAvailable(descriptor.name(), descriptor.aliases());
        if (descriptor.isDefault()) {
            this.ensureDefaultAvailable(descriptor.name());
            this.defaultClientName = normalizedName;
        }

        this.chatClientDescriptorMap.put(normalizedName, descriptor);
        for (String alias : descriptor.aliases()) {
            this.aliases.put(normalize(alias), normalizedName);
        }
    }

    @Override
    public Optional<ChatClientDescriptor> findByName(String name) {
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }

        String normalized = normalize(name);
        ChatClientDescriptor directMatch = this.chatClientDescriptorMap.get(normalized);
        if (directMatch != null) {
            return Optional.of(directMatch);
        }

        String targetName = this.aliases.get(normalized);
        if (targetName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.chatClientDescriptorMap.get(targetName));
    }

    @Override
    public List<ChatClientDescriptor> list() {
        return List.copyOf(new ArrayList<>(this.chatClientDescriptorMap.values()));
    }

    @Override
    public Optional<ChatClientDescriptor> getDefault() {
        if (this.defaultClientName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.chatClientDescriptorMap.get(this.defaultClientName));
    }

    /**
     * 校验ChatClient描述符的有效性
     *
     * @param descriptor ChatClient描述符
     */
    private void validateDescriptor(ChatClientDescriptor descriptor) {
        if (descriptor == null) {
            throw new InvalidChatClientConfigurationException("Chat client descriptor must not be null.");
        }
        if (!StringUtils.hasText(descriptor.name())) {
            throw new InvalidChatClientConfigurationException("Chat client name must not be blank.");
        }
        if (descriptor.chatClient() == null) {
            throw new InvalidChatClientConfigurationException(
                    "Chat client instance for '%s' must not be null.".formatted(descriptor.name()));
        }
    }

    /**
     * 确保别名可用
     *
     * @param clientName 客户端名称
     * @param aliases    别名集合
     */
    private void ensureAliasesAvailable(String clientName, Set<String> aliases) {
        for (String alias : aliases) {
            if (!StringUtils.hasText(alias)) {
                throw new InvalidChatClientConfigurationException(
                        "Blank alias is not allowed for chat client '%s'.".formatted(clientName));
            }

            String normalizedAlias = normalize(alias);
            if (this.chatClientDescriptorMap.containsKey(normalizedAlias)) {
                throw new DuplicateChatClientException(
                        "Alias '%s' conflicts with an existing chat client name.".formatted(alias));
            }

            if (this.aliases.containsKey(normalizedAlias)) {
                throw new DuplicateChatClientException("Alias '%s' is already registered.".formatted(alias));
            }
        }
    }

    /**
     * 确保默认客户端可用
     *
     * @param clientName 客户端名称
     */
    private void ensureDefaultAvailable(String clientName) {
        if (this.defaultClientName != null) {
            throw new InvalidChatClientConfigurationException(
                    "Multiple default chat clients configured. Existing='%s', new='%s'."
                            .formatted(this.defaultClientName, clientName));
        }
    }

    /**
     * 标准化字符串
     *
     * @param value 待标准化的字符串
     * @return 标准化后的字符串
     */
    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
