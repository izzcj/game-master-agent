package io.github.izzcj.gamemaster.model.response;

import java.util.List;

/**
 * 模型配置总览响应。
 *
 * @param chatScenes 对话场景绑定
 * @param embedding 嵌入模型绑定
 */
public record ModelConfigResponse(
    List<ModelBindingResponse> chatScenes,
    ModelBindingResponse embedding
) {
}
