package io.github.izzcj.gamemaster.model.response;

/**
 * 模型绑定响应。
 *
 * @param scene 业务场景
 * @param bindingKey 绑定键
 * @param beanName Bean 名称
 * @param provider 提供商
 * @param displayName 展示名称
 */
public record ModelBindingResponse(
    String scene,
    String bindingKey,
    String beanName,
    String provider,
    String displayName
) {
}
