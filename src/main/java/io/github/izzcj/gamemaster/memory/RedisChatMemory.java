package io.github.izzcj.gamemaster.memory;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Redis 的聊天记忆实现。
 *
 * @author Ale
 * @version 1.0.0
 **/
@Slf4j
@RequiredArgsConstructor
public class RedisChatMemory implements ChatMemory {

    private static final String KEY_PREFIX = "chat:memory:";

    /**
     * Redis 模板
     */
    private final RedisTemplate<String, List<Message>> redisTemplate;

    @Override
    public void add(@NonNull String conversationId, @NonNull List<Message> messages) {
        if (!StringUtils.hasText(conversationId) || CollectionUtils.isEmpty(messages)) {
            return;
        }
        List<Message> history = new ArrayList<>(this.get(conversationId));
        history.addAll(messages);
        this.redisTemplate.opsForValue().set(this.buildCacheKey(conversationId), history);
    }

    @NonNull
    @Override
    public List<Message> get(@NonNull String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        List<Message> messages = this.redisTemplate.opsForValue().get(this.buildCacheKey(conversationId));
        if (CollectionUtils.isEmpty(messages)) {
            return List.of();
        }
        return List.copyOf(messages);
    }

    @Override
    public void clear(@NonNull String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        Boolean deleted = this.redisTemplate.delete(this.buildCacheKey(conversationId));
        log.debug("Cleared redis chat memory, conversationId={}, deleted={}", conversationId, deleted);
    }

    /**
     * 构建缓存key
     *
     * @param conversationId 会话ID
     * @return 缓存key
     */
    private String buildCacheKey(String conversationId) {
        return KEY_PREFIX + conversationId;
    }
}
