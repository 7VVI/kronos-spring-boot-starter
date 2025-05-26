package com.kronos.spring.annotation;

import java.lang.annotation.*;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:34
 * @desc
 */
@Target({ElementType.PARAMETER,ElementType.FIELD})//注解放置的目标位置别
@Retention(RetentionPolicy.RUNTIME)//注解在哪个阶段执行
@Documented
public @interface Time {

    /**
     * 日期时间格式, 用于字符串类型的转换
     */
    String format() default "yyyy-MM-dd HH:mm:ss";
}
