package com.kronos.spring.support;

import java.util.TimeZone;

/**
 * @author zhangyh
 * @Date 2025/5/20 17:57
 * @desc
 */
public class TimeZoneContextHolder {
    private static final ThreadLocal<TimeZone> USER_TIME_ZONE = new ThreadLocal<>();

    /**
     * 设置当前用户的时区
     */
    public static void setTimeZone(TimeZone timeZone) {
        USER_TIME_ZONE.set(timeZone);
    }

    /**
     * 设置当前用户的时区（字符串形式）
     */
    public static void setTimeZone(String timeZoneId) {
        USER_TIME_ZONE.set(TimeZone.getTimeZone(timeZoneId));
    }

    /**
     * 获取当前用户的时区
     */
    public static TimeZone getTimeZone() {
        return USER_TIME_ZONE.get();
    }


    /**
     * 清除当前用户的时区信息
     */
    public static void clear() {
        USER_TIME_ZONE.remove();
    }

}
