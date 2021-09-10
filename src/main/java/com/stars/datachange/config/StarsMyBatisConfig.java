package com.stars.datachange.config;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis配置
 * @author zhouhao
 * @since  2021/9/10 15:19
 */
@org.springframework.context.annotation.Configuration
@MapperScan("com.stars.datachange.mapper")
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(MybatisAutoConfiguration.class)
public class StarsMyBatisConfig {

    @Bean
    public Configuration configuration() {
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        return configuration;
    }

}