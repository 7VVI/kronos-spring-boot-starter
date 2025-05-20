package com.kronos.spring.support.converter;

import com.kronos.spring.config.KronosProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:46
 * @desc
 */
public class TimeZoneConverter {

    private final KronosProperty                  properties;
    private final Map<Class<?>, TypeConverter<?>> converters = new HashMap<>();

    public TimeZoneConverter(KronosProperty properties) {
        this.properties = properties;
    }

    public void registerConverter(Class<?> type, TypeConverter<?> converter) {
        converters.put(type, converter);
    }

}
