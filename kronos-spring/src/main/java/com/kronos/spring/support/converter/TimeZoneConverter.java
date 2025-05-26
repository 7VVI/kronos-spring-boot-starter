package com.kronos.spring.support.converter;

import com.kronos.spring.annotation.Time;
import com.kronos.spring.config.KronosProperty;
import com.kronos.spring.support.converter.impl.DateConverter;
import com.kronos.spring.support.converter.impl.LocalDateTimeConverter;
import com.kronos.spring.support.converter.impl.StringDateTimeConverter;
import com.kronos.spring.support.converter.impl.ZonedDateTimeConverter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:46
 * @desc
 */
public class TimeZoneConverter {

    // 缓存反射信息，提高性能
    private final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

    // 缓存已处理的对象，避免循环引用
    private final ThreadLocal<Set<Object>> processedObjects = ThreadLocal.withInitial(HashSet::new);

    private final KronosProperty                  properties;
    private final Map<Class<?>, TypeConverter<?>> converters = new HashMap<>();

    public TimeZoneConverter(KronosProperty properties) {
        this.properties = properties;
        registerConverter(String.class, new StringDateTimeConverter());
        registerConverter(Date.class, new DateConverter());
        registerConverter(LocalDateTime.class, new LocalDateTimeConverter());
        registerConverter(ZonedDateTime.class, new ZonedDateTimeConverter());
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

    public boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.equals(String.class) || Number.class.isAssignableFrom(clazz) ||
                Boolean.class.equals(clazz) || Character.class.equals(clazz)||converters.containsKey(clazz);
    }


    /**
     * 处理单个参数
     */
    public Object processParameter(Object param, ZoneId zoneId, boolean forceProcess) {
        if (param == null) {
            return null;
        }

        Class<?> paramClass = param.getClass();

        // 处理基本类型和时间类型
        if (isTimeType(paramClass)) {
            if (forceProcess) {
                return this.convertToBackendTimeZone(param, zoneId);
            }
            return param;
        }

        // 处理字符串类型
        if (paramClass == String.class) {
            if (forceProcess) {
                return this.convertToBackendTimeZone(param, zoneId);
            }
            return param;
        }

        // 处理集合类型
        if (Collection.class.isAssignableFrom(paramClass)) {
            return processCollection((Collection<?>) param, zoneId);
        }

        // 处理数组类型
        if (paramClass.isArray()) {
            return processArray(param, zoneId);
        }

        // 处理Map类型
        if (Map.class.isAssignableFrom(paramClass)) {
            return processMap((Map<?, ?>) param, zoneId);
        }

        // 处理普通对象
        if (isCustomObject(paramClass)) {
            return processObject(param, zoneId);
        }

        return param;
    }

    /**
     * 处理普通对象
     */
    private Object processObject(Object obj, ZoneId zoneId) {
        if (obj == null) {
            return null;
        }

        // 防止循环引用
        Set<Object> processed = processedObjects.get();
        if (processed.contains(obj)) {
            return obj;
        }
        processed.add(obj);

        try {
            Class<?> objClass = obj.getClass();
            List<Field> fields = getFieldsWithCache(objClass);

            boolean modified = false;
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);

                    if (fieldValue == null) {
                        continue;
                    }

                    // 检查字段是否有@Time注解
                    boolean hasTimeAnnotation = field.isAnnotationPresent(Time.class);

                    Object convertedValue = processParameter(fieldValue, zoneId, hasTimeAnnotation);

                    if (convertedValue != fieldValue) {
                        field.set(obj, convertedValue);
                        modified = true;
                    }

                } catch (IllegalAccessException e) {
                    // 记录警告日志，但不中断处理
                    System.err.println("无法访问字段: " + field.getName() + " in class: " + objClass.getName());
                }
            }

            return obj;

        } finally {
            processed.remove(obj);
        }
    }

    /**
     * 处理集合类型
     */
    @SuppressWarnings("unchecked")
    private Object processCollection(Collection<?> collection, ZoneId zoneId) {
        if (collection == null || collection.isEmpty()) {
            return collection;
        }

        // 防止循环引用
        Set<Object> processed = processedObjects.get();
        if (processed.contains(collection)) {
            return collection;
        }
        processed.add(collection);

        try {
            boolean modified = false;

            if (collection instanceof List) {
                List<Object> list = (List<Object>) collection;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    Object convertedItem = processParameter(item, zoneId, false);
                    if (convertedItem != item) {
                        list.set(i, convertedItem);
                        modified = true;
                    }
                }
            } else if (collection instanceof Set) {
                Set<Object> originalSet = (Set<Object>) collection;
                Set<Object> newSet = new LinkedHashSet<>();
                for (Object item : originalSet) {
                    Object convertedItem = processParameter(item, zoneId, false);
                    newSet.add(convertedItem);
                    if (convertedItem != item) {
                        modified = true;
                    }
                }
                if (modified) {
                    originalSet.clear();
                    originalSet.addAll(newSet);
                }
            } else {
                // 其他集合类型，尝试重新构建
                Collection<Object> newCollection = createSimilarCollection(collection);
                for (Object item : collection) {
                    Object convertedItem = processParameter(item, zoneId, false);
                    newCollection.add(convertedItem);
                    if (convertedItem != item) {
                        modified = true;
                    }
                }
                if (modified) {
                    return newCollection;
                }
            }

            return collection;

        } finally {
            processed.remove(collection);
        }
    }

    /**
     * 创建类似的集合类型
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> createSimilarCollection(Collection<?> original) {
        try {
            return (Collection<Object>) original.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // 如果无法创建相同类型，使用默认类型
            if (original instanceof Set) {
                return new LinkedHashSet<>();
            } else {
                return new ArrayList<>();
            }
        }
    }

    /**
     * 处理数组类型
     */
    private Object processArray(Object array, ZoneId zoneId) {
        if (array == null) {
            return null;
        }

        Class<?> componentType = array.getClass().getComponentType();

        // 只处理对象数组
        if (componentType.isPrimitive()) {
            return array;
        }

        Object[] objectArray = (Object[]) array;
        boolean modified = false;

        for (int i = 0; i < objectArray.length; i++) {
            Object item = objectArray[i];
            Object convertedItem = processParameter(item, zoneId, false);
            if (convertedItem != item) {
                objectArray[i] = convertedItem;
                modified = true;
            }
        }

        return array;
    }

    /**
     * 处理Map类型
     */
    @SuppressWarnings("unchecked")
    private Object processMap(Map<?, ?> map, ZoneId zoneId) {
        if (map == null || map.isEmpty()) {
            return map;
        }

        // 防止循环引用
        Set<Object> processed = processedObjects.get();
        if (processed.contains(map)) {
            return map;
        }
        processed.add(map);

        try {
            Map<Object, Object> objectMap = (Map<Object, Object>) map;
            boolean modified = false;

            // 创建新的entries来避免ConcurrentModificationException
            List<Map.Entry<Object, Object>> entries = new ArrayList<>(objectMap.entrySet());

            for (Map.Entry<Object, Object> entry : entries) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                Object convertedValue = processParameter(value, zoneId, false);

                if (convertedValue != value) {
                    objectMap.put(key, convertedValue);
                    modified = true;
                }
            }

            return map;

        } finally {
            processed.remove(map);
        }
    }

    /**
     * 判断是否为时间类型
     */
    private boolean isTimeType(Class<?> clazz) {
        return java.time.LocalDateTime.class.isAssignableFrom(clazz) ||
                java.time.LocalDate.class.isAssignableFrom(clazz) ||
                java.time.LocalTime.class.isAssignableFrom(clazz) ||
                java.time.ZonedDateTime.class.isAssignableFrom(clazz) ||
                java.time.OffsetDateTime.class.isAssignableFrom(clazz) ||
                java.time.Instant.class.isAssignableFrom(clazz) ||
                java.util.Date.class.isAssignableFrom(clazz) ||
                java.sql.Timestamp.class.isAssignableFrom(clazz) ||
                java.sql.Date.class.isAssignableFrom(clazz) ||
                java.sql.Time.class.isAssignableFrom(clazz);
    }

    /**
     * 判断是否为自定义对象类型
     */
    private boolean isCustomObject(Class<?> clazz) {
        // 排除Java基本类型、包装类型、常用类型
        return !clazz.isPrimitive() &&
                !clazz.getName().startsWith("java.lang") &&
                !clazz.getName().startsWith("java.math") &&
                !clazz.getName().startsWith("java.util") &&
                !clazz.getName().startsWith("java.time") &&
                !clazz.getName().startsWith("java.sql") &&
                !clazz.isEnum() &&
                !clazz.isArray();
    }

    /**
     * 获取类的所有字段（包括父类），使用缓存提高性能
     */
    private List<Field> getFieldsWithCache(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, this::getAllFields);
    }

    /**
     * 获取类的所有字段（包括父类）
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(fields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }
}
