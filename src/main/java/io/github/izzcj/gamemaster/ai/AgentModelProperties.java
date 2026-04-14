package io.github.izzcj.gamemaster.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 模型绑定与路由配置。
 */
@ConfigurationProperties("agent.model")
public class AgentModelProperties {

    /** 可用对话模型集合，键为业务绑定名。 */
    private Map<String, ModelBinding> chatModels = new LinkedHashMap<>();
    /** 可用嵌入模型集合，键为业务绑定名。 */
    private Map<String, ModelBinding> embeddingModels = new LinkedHashMap<>();
    /** 场景到模型绑定名的映射。 */
    private Map<String, String> routing = new LinkedHashMap<>();

    public Map<String, ModelBinding> getChatModels() {
        return chatModels;
    }

    public void setChatModels(Map<String, ModelBinding> chatModels) {
        this.chatModels = chatModels;
    }

    public Map<String, ModelBinding> getEmbeddingModels() {
        return embeddingModels;
    }

    public void setEmbeddingModels(Map<String, ModelBinding> embeddingModels) {
        this.embeddingModels = embeddingModels;
    }

    public Map<String, String> getRouting() {
        return routing;
    }

    public void setRouting(Map<String, String> routing) {
        this.routing = routing;
    }

    /**
     * 单个模型绑定定义。
     */
    public static class ModelBinding {

        /** Spring Bean 名称。 */
        private String beanName;
        /** 提供商标识。 */
        private String provider;
        /** 后台展示名称。 */
        private String displayName;

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
