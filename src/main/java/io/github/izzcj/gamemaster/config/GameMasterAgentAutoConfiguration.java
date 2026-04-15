package io.github.izzcj.gamemaster.config;

import io.github.izzcj.gamemaster.client.ChatClientRegister;
import io.github.izzcj.gamemaster.client.MemoryChatClientRegister;
import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

/**
 * 游戏大师Agent自动配置
 *
 * @author Ale
 * @version 1.0.0
 */
@AutoConfiguration
public class GameMasterAgentAutoConfiguration {

    /**
     * DeepSeekChatClientBean
     */
    @Bean("deepseekChatClient")
    public ChatClient deepSeekChatClient(@Qualifier("deepSeekChatModel") ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    /**
     * MinimaxChatClientBean
     */
    @Bean("minimaxChatClient")
    public ChatClient minimaxChatClient(@Qualifier("miniMaxChatModel") ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    /**
     * 默认ChatClient注册中心
     */
    @Bean
    @ConditionalOnMissingBean(ChatClientRegister.class)
    public ChatClientRegister chatClientRegister(Map<String, ChatClient> chatClientMap) {
        return new MemoryChatClientRegister(chatClientMap);
    }

    /**
     * 跨域配置
     */
    @Bean
    @ConditionalOnMissingBean(name = "gameMasterAgentCorsConfigurer")
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
}
