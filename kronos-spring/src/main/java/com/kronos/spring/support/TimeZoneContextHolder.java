package com.kronos.spring.support;

import java.time.ZoneId;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:57
 * @desc
 */
public class TimeZoneContextHolder {
    private static final ThreadLocal<ZoneId> contextHolder = new ThreadLocal<>();

    public static void setTimeZone(String zoneId) {
        contextHolder.set(ZoneId.of(zoneId));
    }

    public static ZoneId getTimeZone() {
        return contextHolder.get() != null ? contextHolder.get() : ZoneId.of("UTC");
    }

    public static void clear() {
        contextHolder.remove();
    }

}
