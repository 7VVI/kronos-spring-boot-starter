package com.kronos.spring.annotation;

import java.lang.annotation.*;

/**
 * @author zhangyh
 * @Date 2025/5/26 9:59
 * @desc
 */
@Target({ElementType.TYPE,ElementType.METHOD})//注解放置的目标位置别
@Retention(RetentionPolicy.RUNTIME)//注解在哪个阶段执行
@Documented
public @interface ConvertTime {
}
