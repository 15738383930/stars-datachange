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
            String logoSimple = "stars-datachange-banner-simple.txt";
            // version
            String path = "META-INF/maven/com.gitee.xuan_zheng/stars-datachange/pom.properties";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(logo).getInputStream()));
                 BufferedReader logoReader = new BufferedReader(new InputStreamReader(new ClassPathResource(logo).getInputStream()));
                 BufferedReader logoSimpleReader = new BufferedReader(new InputStreamReader(new ClassPathResource(logoSimple).getInputStream()));
                 BufferedReader versionReader = new BufferedReader(new InputStreamReader(new ClassPathResource(path).getInputStream()))) {
                final long count = reader.lines().filter(o -> o.contains("⣠")).count();
                String version = versionReader.lines().filter(o -> o.contains("version")).map(o -> o.split("=")[1]).findFirst().orElse("");
                if (count > 0) {
                    logoReader.lines().forEach(System.out::println);
                    System.out.println(String.format("                         %s\n", version));
                } else {
                    logoSimpleReader.lines().forEach(System.out::println);
                    System.out.println(String.format("                                           %s\n", version));
                }
            }
        }
    }
}
