package com.kronos.contants;

import com.kronos.spring.config.KronosConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * @author zhangyh
 * @Date 2025/5/26 15:04
 * @desc
 */
@ComponentScan("com.kronos.spring")
@Import({KronosConfiguration.class})
public class KronosAutoConfiguration {
}
