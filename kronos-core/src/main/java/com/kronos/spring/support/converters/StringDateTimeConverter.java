package com.kronos.spring.support.converters;

import com.kronos.spring.support.TypeConverter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:50
 * @desc
 */
public class StringDateTimeConverter implements TypeConverter<String> {
    @Override
    public Object convert(String value, ZoneId fromZone, ZoneId toZone, String format) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        try {
            // 尝试解析日期时间字符串
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

            // 先尝试解析为ZonedDateTime（如果字符串包含时区信息）
            ZonedDateTime dateTime;
            try {
                dateTime = ZonedDateTime.parse(value, formatter);
            } catch (Exception e) {
                // 如果不是ZonedDateTime，尝试解析为LocalDateTime
                LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                dateTime = localDateTime.atZone(fromZone);
            }

            // 转换时区
            ZonedDateTime converted = dateTime.withZoneSameInstant(toZone);

            // 格式化为字符串
            return converted.format(formatter);
        } catch (Exception e) {
            // 如果无法解析，则认为不是时间格式的字符串，原样返回
            return value;
        }
    }

    @Override
    public Class<String> supportedType() {
        return String.class;
    }
}
