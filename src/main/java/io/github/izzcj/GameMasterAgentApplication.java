package io.github.izzcj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Game Master Agent 服务启动入口。
 *
 * <p>负责启用组件扫描、MyBatis Mapper 扫描、配置属性绑定和异步任务能力。
 */
@SpringBootApplication
@MapperScan("io.github.izzcj.gamemaster.mapper")
@ConfigurationPropertiesScan("io.github.izzcj.gamemaster")
@EnableAsync
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
