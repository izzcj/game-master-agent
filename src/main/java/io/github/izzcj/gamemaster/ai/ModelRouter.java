package io.github.izzcj.gamemaster.ai;

import io.github.izzcj.gamemaster.support.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 模型路由器。
 *
 * <p>统一根据业务场景解析当前应使用的对话模型或嵌入模型。
 */
@Component
public class ModelRouter {

    private final AgentModelProperties properties;
    private final Environment environment;
    private final AtomicReference<AgentModelProperties> current = new AtomicReference<>();

    public ModelRouter(AgentModelProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    /**
     * 初始化当前生效的模型配置快照。
     */
    @PostConstruct
    public void init() {
        current.set(properties);
    }

    /**
     * 根据业务场景选择对话模型。
     *
     * @param scene 模型场景
     * @return 模型绑定
     */
    public AgentModelProperties.ModelBinding selectChatModel(ModelScene scene) {
        AgentModelProperties snapshot = current.get();
        String routingKey = snapshot.getRouting().get(scene.getKey());
        AgentModelProperties.ModelBinding binding = snapshot.getChatModels().get(routingKey);
        if (binding == null) {
            String fallbackKey = snapshot.getRouting().get("fallback-chat");
            binding = snapshot.getChatModels().get(fallbackKey);
        }
        if (binding == null) {
            throw new BusinessException("No chat model configured for scene " + scene.name());
        }
        return binding;
    }

    /**
     * 选择默认嵌入模型。
     *
     * @return 嵌入模型绑定
     */
    public AgentModelProperties.ModelBinding selectEmbeddingModel() {
        AgentModelProperties snapshot = current.get();
        String routingKey = snapshot.getRouting().get("embedding");
        AgentModelProperties.ModelBinding binding = snapshot.getEmbeddingModels().get(routingKey);
        if (binding == null) {
            throw new BusinessException("No embedding model configured");
        }
        return binding;
    }

    /**
     * 返回所有对话场景对应的当前模型绑定，用于后台展示。
     *
     * @return 场景到模型绑定的映射
     */
    public Map<ModelScene, AgentModelProperties.ModelBinding> describeChatScenes() {
        Map<ModelScene, AgentModelProperties.ModelBinding> sceneBindings = new EnumMap<>(ModelScene.class);
        for (ModelScene scene : ModelScene.values()) {
            sceneBindings.put(scene, selectChatModel(scene));
        }
        return sceneBindings;
    }

    /**
     * 从当前环境重新装载模型配置。
     */
    public void reload() {
        AgentModelProperties rebound = Binder.get(environment)
            .bind("agent.model", Bindable.of(AgentModelProperties.class))
            .orElse(properties);
        current.set(rebound);
    }
}
