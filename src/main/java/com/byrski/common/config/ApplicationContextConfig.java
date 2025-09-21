package com.byrski.common.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * Spring应用上下文配置类，用于获取Spring容器中的Bean。
 * 通过实现ApplicationContextAware接口，在容器启动后获取ApplicationContext对象，
 * 并提供静态方法getBean()用于方便地获取Bean实例。
 */
@Configuration
public class ApplicationContextConfig implements ApplicationContextAware {

    /**
     * Spring应用上下文对象。
     */
    private static ApplicationContext context;

    /**
     * 设置Spring应用上下文。ApplicationContextAware接口方法。
     * @param applicationContext Spring应用上下文对象
     * @throws BeansException  如果设置上下文失败
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 通过类型获取Spring容器中的Bean。
     * @param clazz Bean的类型
     * @param <T> Bean的类型参数
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 通过名称和类型获取Spring容器中的Bean。
     * @param name Bean的名称
     * @param clazz Bean的类型
     * @param <T> Bean的类型参数
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }
}