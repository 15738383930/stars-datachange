package com.stars.datachange.config;

import com.stars.datachange.autoconfigure.StarsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 自动注册配置
 * @author zhou
 * @since 2021/9/10 10:19
 */
@Configuration
@ComponentScan("com.stars.datachange.**")
public class AutoRegistrationConfig {

    @Bean
    public void banner() throws IOException {
        if (StarsProperties.config.isBanner()) {
            // logo
            String logo = "stars-datachange-banner.txt";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(logo).getInputStream()))) {
                reader.lines().forEach(System.out::println);
            }
            // version
            String path = "META-INF/maven/com.gitee.xuan_zheng/stars-datachange/pom.properties";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(path).getInputStream()))) {
                String version = reader.lines().filter(o -> o.contains("version")).map(o -> o.split("=")[1]).findFirst().orElse("");
                System.out.println(String.format("                         %s\n", version));
            }
        }
    }
}
