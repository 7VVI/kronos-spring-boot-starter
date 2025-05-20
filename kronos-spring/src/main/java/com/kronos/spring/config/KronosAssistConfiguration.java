package com.kronos.spring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangyh
 * @Date 2025/5/20 16:55
 * @desc
 */
@Configuration
@RequiredArgsConstructor
public class KronosAssistConfiguration {

    private final KronosProperty properties;
}
