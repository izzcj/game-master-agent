package io.github.izzcj.gamemaster.support.util;

import java.util.UUID;

/**
 * 简单 ID 生成工具。
 */
public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * 生成带业务前缀的随机字符串 ID。
     *
     * @param prefix 业务前缀
     * @return 形如 {@code prefix-xxxxxxxx} 的 ID
     */
    public static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }
}
