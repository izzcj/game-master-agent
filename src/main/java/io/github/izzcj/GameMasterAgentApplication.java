package io.github.izzcj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Game Master Agent 服务启动入口。
 */
@EnableAsync
@SpringBootApplication
public class GameMasterAgentApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GameMasterAgentApplication.class, args);
    }
}
