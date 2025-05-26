package com.kronos.spring.support;

import java.time.ZoneId;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:46
 * @desc
 */
public interface TypeConverter<T> {

    /**
     * 时区转换方法
     * @param value 原始值
     * @param fromZone 原始时区
     * @param toZone 目标时区
     * @param format 格式化字符串
     * @return 转换后的值
     */
    Object convert(T value, ZoneId fromZone, ZoneId toZone, String format);

    /**
     * 支持的类型
     * @return 类
     */
    Class<T> supportedType();
}
