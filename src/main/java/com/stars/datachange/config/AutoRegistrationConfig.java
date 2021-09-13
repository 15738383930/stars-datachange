package com.stars.datachange.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 自动注册配置
 * @author zhou
 * @since 2021/9/10 10:19
 */
@Configuration
@ComponentScan("com.stars.datachange.**")
public class AutoRegistrationConfig {
}
