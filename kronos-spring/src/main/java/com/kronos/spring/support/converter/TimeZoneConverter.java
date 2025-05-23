package com.kronos.spring.support.converter;

import com.kronos.spring.annotation.Time;
import com.kronos.spring.config.KronosProperty;
import org.springframework.util.ReflectionUtils;

import java.time.ZoneId;
import java.util.Collection;
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

    /**
     * 将对象从客户端时区转换为后端时区
     */
    public Object convertToBackendTimeZone(Object object, ZoneId clientZoneId) {
        if (object == null) {
            return null;
        }

        if (object instanceof Collection) {
            ((Collection<?>) object).forEach(item -> convertToBackendTimeZone(item, clientZoneId));
            return null;
        }

        if (object instanceof Map) {
            ((Map<?, ?>) object).values().forEach(value -> convertToBackendTimeZone(value, clientZoneId));
            return null;
        }

        // 处理基础类型
        if (isSimpleType(object.getClass())) {
            return processBasicType(object, clientZoneId, true);
        }

        // 处理对象类型
        return processFields(object, clientZoneId, true);
    }

    /**
     * 将对象从后端时区转换为客户端时区
     */
    public Object convertToClientTimeZone(Object object, ZoneId clientZoneId) {
        if (object == null) {
            return null;
        }

        if (object instanceof Collection) {
            ((Collection<?>) object).forEach(item -> convertToClientTimeZone(item, clientZoneId));
            return null;
        }

        if (object instanceof Map) {
            ((Map<?, ?>) object).values().forEach(value -> convertToClientTimeZone(value, clientZoneId));
            return null;
        }

        // 处理基础类型
        if (isSimpleType(object.getClass())) {
            return processBasicType(object, clientZoneId, false);
        }

        // 处理对象类型
        return processFields(object, clientZoneId, false);
    }

    /**
     * 时区转换
     * @param object 需要转换的对象
     * @param clientZoneId 客户端时区
     * @param toBackend 是否转换到后端时区
     */
    /**
     * 处理基础类型的时区转换
     */
    private Object processBasicType(Object object, ZoneId clientZoneId, boolean toBackend) {
        if (!isSimpleType(object.getClass()) || !converters.containsKey(object.getClass())) {
            return object;
        }
        TypeConverter<?> converter = converters.get(object.getClass());
        ZoneId backendZoneId = ZoneId.of(properties.getBackendZoneId());
        if (toBackend) {
            return callConvert(converter, object, clientZoneId, backendZoneId, properties.getDefaultDateTimeFormat());
        } else {
            return callConvert(converter, object, backendZoneId, clientZoneId, properties.getDefaultDateTimeFormat());
        }
    }

    /**
     * 处理对象类型的时区转换
     */
    private Object processFields(Object object, ZoneId clientZoneId, boolean toBackend) {

        Class<?> clazz = object.getClass();

        ReflectionUtils.doWithFields(clazz, field -> {
            ReflectionUtils.makeAccessible(field);
            Object value = field.get(object);

            if (value == null) {
                return;
            }

            // 检查字段是否有@TimeZoneField注解并且ignore=true
            Time annotation = field.getAnnotation(Time.class);
            if (annotation == null) {
                return;
            }

            // 处理嵌套对象
            if (!isSimpleType(field.getType()) && !(value instanceof Collection) && !(value instanceof Map)) {
                if (toBackend) {
                    convertToBackendTimeZone(value, clientZoneId);
                } else {
                    convertToClientTimeZone(value, clientZoneId);
                }
                return;
            }

            // 处理集合
            if (value instanceof Collection) {
                Collection<?> collection = (Collection<?>) value;
                if (!collection.isEmpty()) {
                    if (toBackend) {
                        collection.forEach(item -> convertToBackendTimeZone(item, clientZoneId));
                    } else {
                        collection.forEach(item -> convertToClientTimeZone(item, clientZoneId));
                    }
                }
                return;
            }

            // 处理Map
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                if (!map.isEmpty()) {
                    if (toBackend) {
                        map.values().forEach(v -> convertToBackendTimeZone(v, clientZoneId));
                    } else {
                        map.values().forEach(v -> convertToClientTimeZone(v, clientZoneId));
                    }
                }
                return;
            }

            // 处理时间类型字段
            TypeConverter<?> converter = converters.get(field.getType());
            if (converter != null) {
                String format = annotation.format();
                ZoneId backendZoneId = ZoneId.of(properties.getBackendZoneId());

                Object convertedValue;
                if (toBackend) {
                    convertedValue=callConvert(converter,value,clientZoneId,backendZoneId,format);
                } else {
                    convertedValue=callConvert(converter,value,backendZoneId,clientZoneId,format);
                }

                field.set(object, convertedValue);
            }
        });
        return object;
    }

    /**
     * 绕过编译类型检查
     */
    public static <T> Object callConvert(TypeConverter<T> converter, Object value, ZoneId from, ZoneId to, String format) {
        return converter.convert((T) value, from, to, format); // 这里强转合法
    }

    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.equals(String.class) || Number.class.isAssignableFrom(clazz) ||
                Boolean.class.equals(clazz) || Character.class.equals(clazz) ||
                !converters.containsKey(clazz);
    }
}
