package io.github.izzcj.gamemaster.search;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 外部搜索配置。
 */
@ConfigurationProperties("agent.search")
public class SearchProperties {

    /** 默认召回条数。 */
    private int defaultTopK = 5;
    /** 搜索提供商配置列表。 */
    private List<Provider> providers = new ArrayList<>();

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    /**
     * 单个搜索提供商配置。
     */
    public static class Provider {

        /** 提供商名称。 */
        private String name;
        /** 提供商类型。 */
        private String type;
        /** 是否启用。 */
        private boolean enabled;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
