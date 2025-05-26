package com.kronos.spring.support;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhangyh
 * @Date 2025/5/26 10:55
 * @desc 转换上下文，包含转换所需的所有信息
 */
public class ConversionContext {
    private final ZoneId      fromZone;
    private final ZoneId      toZone;
    private final String      defaultFormat;
    private final Set<Object> processedObjects;

    public ConversionContext(ZoneId fromZone, ZoneId toZone, String defaultFormat) {
        this.fromZone = fromZone;
        this.toZone = toZone;
        this.defaultFormat = defaultFormat;
        this.processedObjects = new HashSet<>();
    }

    public ZoneId getFromZone() { return fromZone; }
    public ZoneId getToZone() { return toZone; }
    public String getDefaultFormat() { return defaultFormat; }

    public boolean isProcessed(Object obj) {
        return processedObjects.contains(obj);
    }

    public void markProcessed(Object obj) {
        processedObjects.add(obj);
    }

    public void unmarkProcessed(Object obj) {
        processedObjects.remove(obj);
    }
}
