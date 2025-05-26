package com.kronos.spring.support;

import com.kronos.spring.annotation.Time;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangyh
 * @Date 2025/5/26 10:55
 * @desc
 */
@Slf4j
public class ObjectProcessor {
    private final Map<Class<?>, TypeConverter<?>> typeConverters;

    // 缓存反射信息，提高性能
    private final Map<Class<?>, List<Field>>      fieldCache;

    public ObjectProcessor(Map<Class<?>, TypeConverter<?>> typeConverters) {
        this.typeConverters = typeConverters;
        this.fieldCache = new ConcurrentHashMap<>();
    }

    /**
     * 处理对象的入口方法
     */
    public Object process(Object object, ConversionContext context) {
        if (object == null) {
            return null;
        }

        Class<?> type = object.getClass();

        // 基础类型处理
        if (isSimpleType(type)) {
            return processSimpleType(object, context);
        }

        // 防止循环引用
        if (context.isProcessed(object)) {
            return object;
        }

        context.markProcessed(object);
        try {
            // 集合类型处理
            if (object instanceof Collection) {
                return processCollection((Collection<?>) object, context);
            }

            // Map类型处理
            if (object instanceof Map) {
                return processMap((Map<?, ?>) object, context);
            }

            // 数组类型处理
            if (type.isArray()) {
                return processArray(object, context);
            }

            // 自定义对象处理
            return processCustomObject(object, context);

        } finally {
            context.unmarkProcessed(object);
        }
    }

    /**
     * 处理简单类型（包括时间类型）
     */
    @SuppressWarnings("unchecked")
    private Object processSimpleType(Object object, ConversionContext context) {
        TypeConverter<Object> converter = (TypeConverter<Object>) typeConverters.get(object.getClass());
        if (converter != null) {
            return converter.convert(object, context.getFromZone(), context.getToZone(), context.getDefaultFormat());
        }
        return object;
    }

    /**
     * 处理集合类型
     */
    @SuppressWarnings("unchecked")
    private Object processCollection(Collection<?> collection, ConversionContext context) {
        if (collection.isEmpty()) {
            return collection;
        }

        if (collection instanceof List) {
            List<Object> list = (List<Object>) collection;
            for (int i = 0; i < list.size(); i++) {
                list.set(i, process(list.get(i), context));
            }
        } else if (collection instanceof Set) {
            Set<Object> set = (Set<Object>) collection;
            Set<Object> newSet = createSimilarSet(set);
            for (Object item : set) {
                newSet.add(process(item, context));
            }
            set.clear();
            set.addAll(newSet);
        } else {
            // 其他集合类型，创建新集合
            Collection<Object> newCollection = createSimilarCollection(collection);
            for (Object item : collection) {
                newCollection.add(process(item, context));
            }
            return newCollection;
        }

        return collection;
    }

    /**
     * 处理Map类型
     */
    @SuppressWarnings("unchecked")
    private Object processMap(Map<?, ?> map, ConversionContext context) {
        if (map.isEmpty()) {
            return map;
        }

        Map<Object, Object> objectMap = (Map<Object, Object>) map;
        Map<Object, Object> processedEntries = new HashMap<>();

        for (Map.Entry<Object, Object> entry : objectMap.entrySet()) {
            Object processedValue = process(entry.getValue(), context);
            processedEntries.put(entry.getKey(), processedValue);
        }

        objectMap.putAll(processedEntries);
        return map;
    }

    /**
     * 处理数组类型
     */
    private Object processArray(Object array, ConversionContext context) {
        if (array.getClass().getComponentType().isPrimitive()) {
            return array; // 基础类型数组不处理
        }

        Object[] objectArray = (Object[]) array;
        for (int i = 0; i < objectArray.length; i++) {
            objectArray[i] = process(objectArray[i], context);
        }

        return array;
    }

    /**
     * 处理自定义对象
     */
    private Object processCustomObject(Object object, ConversionContext context) {
        Class<?> type = object.getClass();
        List<Field> fields = getFieldsWithCache(type);

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(object);

                if (fieldValue == null) {
                    continue;
                }

                // 检查是否需要处理时区转换
                if (shouldProcessField(field, fieldValue)) {
                    Object convertedValue = process(fieldValue, context);
                    field.set(object, convertedValue);
                }

            } catch (IllegalAccessException e) {
                // 记录日志但不中断处理
                log.debug("无法访问字段: " + field.getName() + " in " + type.getName());
            }
        }

        return object;
    }

    /**
     * 判断字段是否需要处理
     */
    private boolean shouldProcessField(Field field, Object fieldValue) {
        // 有@Time注解的字段需要处理
        if (field.isAnnotationPresent(Time.class)) {
            return true;
        }

        // 时间类型字段需要处理
        if (isTimeType(fieldValue.getClass())) {
            return true;
        }

        // 集合、Map、自定义对象需要递归处理
        return fieldValue instanceof Collection ||
                fieldValue instanceof Map ||
                isCustomObject(fieldValue.getClass());
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                Number.class.isAssignableFrom(type) ||
                Boolean.class.equals(type) ||
                Character.class.equals(type) ||
                typeConverters.containsKey(type);
    }

    /**
     * 判断是否为时间类型
     */
    private boolean isTimeType(Class<?> type) {
        return LocalDateTime.class.isAssignableFrom(type) ||
                LocalDate.class.isAssignableFrom(type) ||
                LocalTime.class.isAssignableFrom(type) ||
                ZonedDateTime.class.isAssignableFrom(type) ||
                OffsetDateTime.class.isAssignableFrom(type) ||
                Instant.class.isAssignableFrom(type) ||
                Date.class.isAssignableFrom(type) ||
                java.sql.Timestamp.class.isAssignableFrom(type) ||
                java.sql.Date.class.isAssignableFrom(type) ||
                java.sql.Time.class.isAssignableFrom(type);
    }

    /**
     * 判断是否为自定义对象
     */
    private boolean isCustomObject(Class<?> type) {
        return !type.isPrimitive() &&
                !type.getName().startsWith("java.") &&
                !type.getName().startsWith("javax.") &&
                !type.isEnum() &&
                !type.isArray();
    }

    /**
     * 获取类的所有字段（带缓存）
     */
    private List<Field> getFieldsWithCache(Class<?> type) {
        return fieldCache.computeIfAbsent(type, this::getAllFields);
    }

    /**
     * 获取类的所有字段（包括父类）
     */
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = type;

        while (currentClass != null && currentClass != Object.class) {
            Collections.addAll(fields, currentClass.getDeclaredFields());
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * 创建相似的Set集合
     */
    @SuppressWarnings("unchecked")
    private Set<Object> createSimilarSet(Set<?> original) {
        try {
            return (Set<Object>) original.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return new LinkedHashSet<>();
        }
    }

    /**
     * 创建相似的集合
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> createSimilarCollection(Collection<?> original) {
        try {
            return (Collection<Object>) original.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return original instanceof Set ? new LinkedHashSet<>() : new ArrayList<>();
        }
    }
}
