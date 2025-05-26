package com.kronos.spring.support;

import com.kronos.spring.config.KronosProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author zhangyh
 * @Date 2025/5/21 8:49
 * @desc
 */
public class TimeZoneInterceptor implements HandlerInterceptor {

    private final KronosProperty properties;

    public TimeZoneInterceptor(KronosProperty properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取时区信息
        String timeZoneId = request.getHeader(properties.getClientZoneIdHeader());

        if (StringUtils.hasText(timeZoneId)) {
            TimeZoneContextHolder.setTimeZone(timeZoneId);
        } else {
            // 如果请求头中没有时区信息，则使用默认时区
            TimeZoneContextHolder.setTimeZone(properties.getDefaultClientZoneId());
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除ThreadLocal中的时区信息，防止内存泄漏
        TimeZoneContextHolder.clear();
    }
}