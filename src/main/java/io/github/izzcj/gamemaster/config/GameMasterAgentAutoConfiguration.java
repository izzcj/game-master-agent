package io.github.izzcj.gamemaster.config;

import io.github.izzcj.gamemaster.advisor.GameMasterLoggingAdvisor;
import io.github.izzcj.gamemaster.client.ChatClientDescriptor;
import io.github.izzcj.gamemaster.client.ChatClientResolver;
import io.github.izzcj.gamemaster.client.ChatClientRegistry;
import io.github.izzcj.gamemaster.client.DefaultChatClientResolver;
import io.github.izzcj.gamemaster.client.MemoryChatClientRegistry;
import io.github.izzcj.gamemaster.client.RegisteredChatClient;
import io.github.izzcj.gamemaster.log.ChatClientLogging;
import io.github.izzcj.gamemaster.log.DefaultChatClientLogging;
import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 娓告垙澶у笀Agent鑷姩閰嶇疆
 *
 * @author Ale
 * @version 1.0.0
 */
@AutoConfiguration
public class GameMasterAgentAutoConfiguration {

    /**
     * 默认ChatClient日志实现
     */
    @Bean
    @ConditionalOnMissingBean(ChatClientLogging.class)
    public ChatClientLogging defaultChatClientLogging() {
        return new DefaultChatClientLogging();
    }

    /**
     * DeepSeek ChatClient Bean
     */
    @Bean
    @RegisteredChatClient(name = "deepseek", aliases = {"ds"}, isDefault = true)
    public ChatClient deepSeekChatClient(@Qualifier("deepSeekChatModel") ChatModel chatModel,
                                         ChatClientLogging chatClientLogging) {
        return ChatClient.builder(chatModel).defaultAdvisors(new GameMasterLoggingAdvisor(chatClientLogging)).build();
    }

    /**
     * Minimax ChatClient Bean
     */
    @Bean
    @RegisteredChatClient(name = "minimax", aliases = {"mm"})
    public ChatClient minimaxChatClient(@Qualifier("miniMaxChatModel") ChatModel chatModel,
                                        ChatClientLogging chatClientLogging) {
        return ChatClient.builder(chatModel).defaultAdvisors(new GameMasterLoggingAdvisor(chatClientLogging)).build();
    }

    /**
     * 默认ChatClient注册中心
     */
    @Bean
    @ConditionalOnMissingBean(ChatClientRegistry.class)
    public ChatClientRegistry chatClientRegistry(Map<String, ChatClient> chatClientMap,
                                                 ApplicationContext applicationContext) {
        MemoryChatClientRegistry registry = new MemoryChatClientRegistry();
        chatClientMap.forEach((beanName, chatClient) -> registry.register(buildDescriptor(beanName, chatClient, applicationContext)));
        return registry;
    }

    /**
     * 默认ChatClient解析器
     */
    @Bean
    @ConditionalOnMissingBean(ChatClientResolver.class)
    public ChatClientResolver chatClientResolver(ChatClientRegistry chatClientRegistry) {
        return new DefaultChatClientResolver(chatClientRegistry);
    }

    /**
     * 跨域配置
     */
    @Bean
    public WebMvcConfigurer gameMasterAgentCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowCredentials(true)
                        .allowedOriginPatterns("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("*")
                        .maxAge(1800);
            }
        };
    }

    /**
     * 构建ChatClient描述符
     *
     * @param beanName           ChatClient Bean名称
     * @param chatClient         ChatClient实例
     * @param applicationContext Spring ApplicationContext
     * @return ChatClient描述符
     */
    private ChatClientDescriptor buildDescriptor(String beanName, ChatClient chatClient, ApplicationContext applicationContext) {
        RegisteredChatClient annotation = applicationContext.findAnnotationOnBean(beanName, RegisteredChatClient.class);
        String name = beanName;
        Set<String> aliases = Set.of();
        boolean isDefault = false;
        if (annotation != null) {
            if (StringUtils.hasText(annotation.name())) {
                name = annotation.name();
            }
            aliases = Arrays.stream(annotation.aliases())
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            isDefault = annotation.isDefault();
        }
        return new ChatClientDescriptor(name, aliases, chatClient, isDefault, Map.of("beanName", beanName));
    }
}
