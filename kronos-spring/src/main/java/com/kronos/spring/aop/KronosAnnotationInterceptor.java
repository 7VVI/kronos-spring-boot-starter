package com.kronos.spring.aop;

import com.kronos.spring.support.TimeZoneContextHolder;
import com.kronos.spring.support.converter.TimeZoneConverter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
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
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();
        ZoneId zoneId = ZoneId.of(TimeZoneContextHolder.getTimeZone().getID());
        beforeMethod(zoneId, arguments, method);
        Object result = invocation.proceed();
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        //不是查询方法并且不是要特殊处理的方法 不做处理
        if (getMapping == null || result == null) {
            return result;
        }
        if (method.getDeclaringClass() == Object.class) {
            return "";
        }
        return converter.toClientTimeZone(result, zoneId);
    }

    public void beforeMethod(ZoneId zoneId, Object[] params, Method method) {
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
