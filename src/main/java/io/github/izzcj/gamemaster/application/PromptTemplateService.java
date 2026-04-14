package io.github.izzcj.gamemaster.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * Prompt 模板渲染服务。
 */
@Service
public class PromptTemplateService {

    private final ResourceLoader resourceLoader;

    public PromptTemplateService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 加载模板并用变量做简单占位替换。
     *
     * @param templateName 模板名，不含后缀
     * @param variables 模板变量
     * @return 渲染后的文本
     */
    public String render(String templateName, Map<String, String> variables) {
        try {
            String template = resourceLoader.getResource("classpath:prompts/" + templateName + ".st")
                .getContentAsString(StandardCharsets.UTF_8);
            String rendered = template;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            return rendered;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load prompt template " + templateName, exception);
        }
    }
}
