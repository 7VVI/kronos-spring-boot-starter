package com.kronos.spring.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:46
 * @desc
 */
@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = KronosProperty.PREFIX)
public class KronosProperty {

    public static final String PREFIX = "spring.kronos";

    /**
     * 后端标准时区ID，默认为UTC
     */
    private String backendZoneId = "UTC";

    /**
     * 默认前端时区ID，当请求中没有指定时区时使用
     */
    private String defaultClientZoneId = "Asia/Shanghai";

    /**
     * 前端时区请求头名称
     */
    private String clientZoneIdHeader = "X-Time-Zone";

    /**
     * 时间格式，用于字符串时间的解析和格式化
     */
    private String defaultDateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 是否处理请求参数中的时区转换
     */
    private Boolean handleRequest = true;

    /**
     * 是否处理响应中的时区转换
     */
    private Boolean handleResponse = true;


}
