package com.kronos.spring.aop;

import com.kronos.spring.annotation.Time;
import com.kronos.spring.support.TimeZoneContextHolder;
import com.kronos.spring.support.converter.TimeZoneConverter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        if (getMapping==null||result==null) {
            return result;
        }
        if (method.getDeclaringClass() == Object.class) {
            return "";
        }
        return converter.convertToClientTimeZone(result, zoneId);
    }

    public void beforeMethod(ZoneId zoneId, Object[] params, Method method) {
        //获取方法参数上的注解（因为方法可以有多参数；参数上可以有多注解，返回二维数组）
        Annotation[][] an = method.getParameterAnnotations();
        int index = 0;
        //循环参数
        for (Annotation[] an1 : an) {
            Object param = params[index];
            //循环参数上的注解
            for (Annotation an2 : an1) {
                //有自定义校验注解
                if (an2 instanceof Time && param != null) {
                    //时区转换
                    Object result = converter.convertToBackendTimeZone(param, zoneId);
                    if (result!=null) {
                        params[index] = result;
                    }
                }
            }
            index++;
        }
    }

    /**
     * 获取类中带有时区转化注解的字段
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> allFields = new ArrayList<>();

        // 获取当前类及其所有父类的字段
        while (clazz != null && clazz != Object.class) {
            allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        //筛选出字段中带有自定义注解的字段
        List<Field> fieldList = new ArrayList<>();
        for (Field field : allFields) {
            if (field.isAnnotationPresent(Time.class)) {
                fieldList.add(field);
            }
        }
        return fieldList;
    }
}
