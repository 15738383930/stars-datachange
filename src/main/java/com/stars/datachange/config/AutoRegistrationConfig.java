package com.stars.datachange.config;

import com.stars.datachange.autoconfigure.StarsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.StandardCharsets;

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
            // version
            String pom = "META-INF/maven/com.gitee.xuan_zheng/stars-datachange/pom.properties";
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            try (BufferedReader logoReader = new BufferedReader(new InputStreamReader(new ClassPathResource(logo).getInputStream(), StandardCharsets.UTF_8));
                BufferedReader pomReader = new BufferedReader(new InputStreamReader(new ClassPathResource(pom).getInputStream()))) {
                logoReader.lines().forEach(System.out::println);
                String version = pomReader.lines().filter(o -> o.contains("version")).map(o -> o.split("=")[1]).findFirst().orElse("");
                System.out.printf("                         %s\n", version);
            }
            System.setOut(new PrintStream(System.out));
        }
    }
}
