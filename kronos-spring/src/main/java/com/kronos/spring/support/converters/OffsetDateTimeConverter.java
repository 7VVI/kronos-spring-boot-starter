package com.kronos.spring.support.converters;

import com.kronos.spring.support.TypeConverter;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * @author zhangyh
 * @Date 2025/5/26 10:56
 * @desc
 */
public class OffsetDateTimeConverter implements TypeConverter<OffsetDateTime> {
    @Override
    public Object convert(OffsetDateTime value, ZoneId fromZone, ZoneId toZone, String format) {
        return value.atZoneSameInstant(toZone).toOffsetDateTime();
    }

    @Override
    public Class<OffsetDateTime> supportedType() {
        return OffsetDateTime.class;
    }
}

