package com.kronos.spring.config;

import com.kronos.spring.support.TimeZoneInterceptor;
import com.kronos.spring.support.TimeZoneConverter;
import com.kronos.spring.support.TypeConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:59
 * @desc
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
@ConditionalOnProperty(prefix = KronosProperty.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class KronosConfiguration implements WebMvcConfigurer {

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    @ConditionalOnMissingBean
    public TimeZoneConverter timeZoneConverter(List<TypeConverter<?>> converters) {
        return new TimeZoneConverter(dynamicDataSourceProperties(), converters);
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public static KronosProperty dynamicDataSourceProperties() {
        return new KronosProperty();
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    @ConditionalOnMissingBean
    public KronosBeanPostProcessor kronosBeanPostProcessor(List<TypeConverter<?>> converters) {
        return new KronosBeanPostProcessor(timeZoneConverter(converters));
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public TimeZoneInterceptor timeZoneInterceptor() {
        return new TimeZoneInterceptor(dynamicDataSourceProperties());
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(timeZoneInterceptor());
    }
}
