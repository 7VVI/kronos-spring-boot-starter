package com.kronos.spring.config;

import com.kronos.spring.annotation.ConvertTime;
import com.kronos.spring.aop.KronosAnnotationAdvisor;
import com.kronos.spring.aop.KronosAnnotationInterceptor;
import com.kronos.spring.support.TimeZoneConverter;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:58
 * @desc
 */
public class KronosBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

    private final TimeZoneConverter timeZoneConverter;

    public KronosBeanPostProcessor(TimeZoneConverter timeZoneConverter) {
        this.timeZoneConverter = timeZoneConverter;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        KronosAnnotationInterceptor kronosAnnotationInterceptor = new KronosAnnotationInterceptor(this.timeZoneConverter);
        this.advisor = new KronosAnnotationAdvisor(kronosAnnotationInterceptor, ConvertTime.class);
    }
}
