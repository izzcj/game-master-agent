package io.github.izzcj.gamemaster.memory;

import io.github.izzcj.gamemaster.support.KryoRedisSerializer;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * GameMasterMemory 自动配置。
 *
 * @author Ale
 * @version 1.0.0
 **/
@AutoConfiguration
public class GameMasterMemoryAutoConfiguration {

    /**
     * 默认 ChatMemory 实现。
     * Redis 可用时使用 RedisChatMemory，否则退回到本地窗口记忆。
     */
    @Bean
    public ChatMemory chatMemory(ObjectProvider<RedisTemplate<String, List<Message>>> redisChatMemoryRedisTemplateProvider) {
        RedisTemplate<String, List<Message>> redisTemplate = redisChatMemoryRedisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            return new RedisChatMemory(redisTemplate);
        }
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }

    /**
     * Redis ChatMemory 专用模板。
     */
    @Bean
    public RedisTemplate<String, List<Message>> redisChatMemoryRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, List<Message>> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer(StandardCharsets.UTF_8);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        KryoRedisSerializer<List<Message>> serializer = new KryoRedisSerializer<>();
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }

}
