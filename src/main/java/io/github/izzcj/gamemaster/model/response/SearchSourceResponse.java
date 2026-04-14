package io.github.izzcj.gamemaster.model.response;

/**
 * 搜索源配置响应。
 *
 * @param name 名称
 * @param type 类型
 * @param enabled 是否启用
 */
public record SearchSourceResponse(
    String name,
    String type,
    boolean enabled
) {
}
