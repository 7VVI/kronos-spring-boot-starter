package com.kronos.spring.support.converters;

import com.kronos.spring.support.TypeConverter;

import java.time.Instant;
import java.time.ZoneId;

/**
 * @author zhangyh
 * @Date 2025/5/26 10:56
 * @desc
 */
public class InstantConverter implements TypeConverter<Instant> {
    @Override
    public Object convert(Instant value, ZoneId fromZone, ZoneId toZone, String format) {
        // Instant本身就是UTC时间，不需要转换
        return value;
    }

    @Override
    public Class<Instant> supportedType() {
        return Instant.class;
    }
}
