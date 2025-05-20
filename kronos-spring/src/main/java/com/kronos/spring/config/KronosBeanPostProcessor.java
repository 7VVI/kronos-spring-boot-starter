package com.kronos.spring.config;

import com.kronos.spring.annotation.Time;
import com.kronos.spring.aop.KronosAnnotationAdvisor;
import com.kronos.spring.aop.KronosAnnotationInterceptor;
import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:58
 * @desc
 */
public class KronosBeanPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        KronosAnnotationInterceptor kronosAnnotationInterceptor = new KronosAnnotationInterceptor();
        this.advisor = new KronosAnnotationAdvisor(kronosAnnotationInterceptor, Time.class);
    }
}
