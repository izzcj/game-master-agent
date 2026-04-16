package io.github.izzcj.gamemaster.config;

import io.github.izzcj.gamemaster.advisor.GameMasterLoggingAdvisor;
import io.github.izzcj.gamemaster.client.ChatClientRegistry;
import io.github.izzcj.gamemaster.client.MemoryChatClientRegistry;
import io.github.izzcj.gamemaster.log.ChatClientLogging;
import io.github.izzcj.gamemaster.log.DefaultChatClientLogging;
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
    @Bean("deepseekChatClient")
    public ChatClient deepSeekChatClient(@Qualifier("deepSeekChatModel") ChatModel chatModel, ChatClientLogging chatClientLogging) {
        return ChatClient.builder(chatModel).defaultAdvisors(new GameMasterLoggingAdvisor(chatClientLogging)).build();
    }

    /**
     * Minimax ChatClient Bean
     */
    @Bean("minimaxChatClient")
     public ChatClient minimaxChatClient(@Qualifier("miniMaxChatModel") ChatModel chatModel, ChatClientLogging chatClientLogging) {
        return ChatClient.builder(chatModel).defaultAdvisors(new GameMasterLoggingAdvisor(chatClientLogging)).build();
    }

    /**
     * 默认ChatClient注册中心
     */
    @Bean
    @ConditionalOnMissingBean(ChatClientRegistry.class)
    public ChatClientRegistry chatClientRegister(Map<String, ChatClient> chatClientMap) {
        MemoryChatClientRegistry memoryChatClientRegister = new MemoryChatClientRegistry();
        chatClientMap.forEach(memoryChatClientRegister::registerChatClient);
        return memoryChatClientRegister;
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
}
