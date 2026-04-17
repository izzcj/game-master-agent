package io.github.izzcj.gamemaster.support;

import cn.hutool.core.util.ArrayUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 基于 Kryo 的 Redis 序列化器。
 *
 * @param <T> 序列化目标类型
 * @author Ale
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    /**
     * 空字节
     */
    static final byte[] EMPTY_ARRAY = new byte[0];

    /**
     * 是否可以序列化
     *
     * @param type 类型
     * @return bool
     */
    @Override
    public boolean canSerialize(@Nonnull Class<?> type) {
        return true;
    }

    @Nullable
    @Override
    public byte[] serialize(@Nullable T value) throws SerializationException {
        if (value == null) {
            return EMPTY_ARRAY;
        }
        return KryoUtils.serialize(value);
    }

    @Nullable
    @Override
    public T deserialize(@Nullable byte[] bytes) throws SerializationException {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }

        return KryoUtils.deserialize(bytes);
    }
}
