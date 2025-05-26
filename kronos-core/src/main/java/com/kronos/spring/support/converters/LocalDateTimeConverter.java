package com.kronos.spring.support.converters;

import com.kronos.spring.support.TypeConverter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:48
 * @desc
 */
public class LocalDateTimeConverter implements TypeConverter<LocalDateTime> {
    @Override
    public Object convert(LocalDateTime value, ZoneId fromZone, ZoneId toZone, String format) {
        if (value == null) {
            return null;
        }

        // 转换LocalDateTime到ZonedDateTime
        ZonedDateTime zonedDateTime = value.atZone(fromZone);

        // 转换时区
        ZonedDateTime converted = zonedDateTime.withZoneSameInstant(toZone);

        // 转回LocalDateTime
        return converted.toLocalDateTime();
    }

    @Override
    public Class<LocalDateTime> supportedType() {
        return LocalDateTime.class;
    }
}
