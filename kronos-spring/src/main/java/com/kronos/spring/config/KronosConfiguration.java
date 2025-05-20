package com.kronos.spring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:59
 * @desc
 */
@Configuration(proxyBeanMethods = false)
public class KronosConfiguration {

    @Bean
    public static KronosProperty dynamicDataSourceProperties() {
        return new KronosProperty();
    }

    @Bean
    @ConditionalOnMissingBean
    public KronosBeanPostProcessor kronosBeanPostProcessor() {
        return new KronosBeanPostProcessor();
    }
}
