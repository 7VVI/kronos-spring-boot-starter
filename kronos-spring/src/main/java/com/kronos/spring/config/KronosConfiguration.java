package com.kronos.spring.config;

import com.kronos.spring.support.TimeZoneInterceptor;
import com.kronos.spring.support.converter.TimeZoneConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:59
 * @desc
 */
@Configuration(proxyBeanMethods = true)
public class KronosConfiguration implements WebMvcConfigurer {

    @Bean
    public TimeZoneConverter timeZoneConverter() {
        return new TimeZoneConverter(dynamicDataSourceProperties());
    }

    @Bean
    public static KronosProperty dynamicDataSourceProperties() {
        return new KronosProperty();
    }

    @Bean
    @ConditionalOnMissingBean
    public KronosBeanPostProcessor kronosBeanPostProcessor() {
        return new KronosBeanPostProcessor(timeZoneConverter());
    }

    /**
     * 注册时区拦截器
     */
    @Bean
    public TimeZoneInterceptor timeZoneInterceptor() {
        return new TimeZoneInterceptor( dynamicDataSourceProperties());
    }

    /**
     * 将时区拦截器添加到拦截器链中
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(timeZoneInterceptor());
    }
}
