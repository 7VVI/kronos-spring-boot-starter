package com.kronos.spring.support.converter;

import com.kronos.spring.config.KronosProperty;
import com.kronos.spring.support.ConversionContext;
import com.kronos.spring.support.ObjectProcessor;
import com.kronos.spring.support.converter.impl.*;

import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:46
 * @desc
 */
public class TimeZoneConverter {

    private final ZoneId                          backendZoneId;
    private final String                          defaultDateFormat;
    private final Map<Class<?>, TypeConverter<?>> typeConverters;
    private final ObjectProcessor                 objectProcessor;

    public TimeZoneConverter(KronosProperty properties) {
        this.backendZoneId = ZoneId.of(properties.getBackendZoneId());
        this.defaultDateFormat = properties.getDefaultDateTimeFormat();
        this.typeConverters = initializeTypeConverters();
        this.objectProcessor = new ObjectProcessor(typeConverters);
    }

    /**
     * 客户端时区 -> 后端时区
     */
    public <T> T toBackendTimeZone(T object, ZoneId clientZoneId) {
        return convertTimeZone(object, clientZoneId, backendZoneId);
    }

    /**
     * 后端时区 -> 客户端时区
     */
    public <T> T toClientTimeZone(T object, ZoneId clientZoneId) {
        return convertTimeZone(object, backendZoneId, clientZoneId);
    }

    /**
     * 注册自定义类型转换器
     */
    public <T> void registerConverter(Class<T> type, TypeConverter<T> converter) {
        typeConverters.put(type, converter);
    }

    /**
     * 核心转换逻辑
     */
    @SuppressWarnings("unchecked")
    private <T> T convertTimeZone(T object, ZoneId fromZone, ZoneId toZone) {
        if (object == null || fromZone.equals(toZone)) {
            return object;
        }

        ConversionContext context = new ConversionContext(fromZone, toZone, defaultDateFormat);
        return (T) objectProcessor.process(object, context);
    }

    /**
     * 初始化默认类型转换器
     */
    private Map<Class<?>, TypeConverter<?>> initializeTypeConverters() {
        Map<Class<?>, TypeConverter<?>> converters = new ConcurrentHashMap<>();
        converters.put(String.class, new StringDateTimeConverter());
        converters.put(Date.class, new DateConverter());
        converters.put(LocalDateTime.class, new LocalDateTimeConverter());
        converters.put(ZonedDateTime.class, new ZonedDateTimeConverter());
        converters.put(Instant.class, new InstantConverter());
        converters.put(OffsetDateTime.class, new OffsetDateTimeConverter());
        return converters;
    }
}
