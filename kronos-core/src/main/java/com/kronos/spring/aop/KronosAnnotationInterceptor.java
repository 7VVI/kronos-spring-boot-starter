package com.kronos.spring.aop;

import com.kronos.spring.support.TimeZoneContextHolder;
import com.kronos.spring.support.TimeZoneConverter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.time.ZoneId;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:42
 * @desc
 */
public class KronosAnnotationInterceptor implements MethodInterceptor {

    private final TimeZoneConverter converter;

    public KronosAnnotationInterceptor(TimeZoneConverter converter) {
        this.converter = converter;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        ZoneId zoneId = ZoneId.of(TimeZoneContextHolder.getTimeZone().getID());
        beforeMethod(zoneId,  invocation.getArguments());
        Object result = invocation.proceed();
        return converter.toClientTimeZone(result, zoneId);
    }

    public void beforeMethod(ZoneId zoneId, Object[] params) {
        //获取方法参数上的注解（因为方法可以有多参数；参数上可以有多注解，返回二维数组）
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            if (param == null) {
                continue;
            }
            Object convertedParam = converter.toBackendTimeZone(param, zoneId);
            if (convertedParam != param) {
                params[i] = convertedParam;
            }
        }
    }
}
