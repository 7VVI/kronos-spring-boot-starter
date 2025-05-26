package com.kronos.spring.support.converters;

import com.kronos.spring.support.TypeConverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:49
 * @desc
 */
public class ZonedDateTimeConverter implements TypeConverter<ZonedDateTime> {
    @Override
    public Object convert(ZonedDateTime value, ZoneId fromZone, ZoneId toZone, String format) {
        if (value == null) {
            return null;
        }

        // 直接转换时区
        return value.withZoneSameInstant(toZone);
    }

    @Override
    public Class<ZonedDateTime> supportedType() {
        return ZonedDateTime.class;
    }
}
