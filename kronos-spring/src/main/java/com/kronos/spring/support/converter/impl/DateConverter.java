package com.kronos.spring.support.converter.impl;

import com.kronos.spring.support.converter.TypeConverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:47
 * @desc
 */
public class DateConverter implements TypeConverter<Date> {


    @Override
    public Object convert(Date value, ZoneId fromZone, ZoneId toZone, String format) {
        if (value == null) {
            return null;
        }

        // 转换Date到ZonedDateTime
        ZonedDateTime zonedDateTime = value.toInstant().atZone(fromZone);

        // 转换时区
        ZonedDateTime converted = zonedDateTime.withZoneSameInstant(toZone);

        // 转回Date
        return Date.from(converted.toInstant());
    }
}
