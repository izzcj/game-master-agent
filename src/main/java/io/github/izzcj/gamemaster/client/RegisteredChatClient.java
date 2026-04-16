package io.github.izzcj.gamemaster.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明ChatClient Bean的注册元数据。
 *
 * @author Ale
 * @version 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisteredChatClient {

    /**
     * 对外暴露的名称。
     */
    String name();

    /**
     * 可选别名集合。
     */
    String[] aliases() default {};

    /**
     * 是否为默认客户端。
     */
    boolean isDefault() default false;
}
