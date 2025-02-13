package com.stars.datachange.config;

import com.stars.datachange.module.DataDictionary;
import com.stars.datachange.module.StarsCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "stars.cache.open", havingValue = "true", matchIfMissing = true)
public class StarsCacheConfig {

    @Resource
    private DataDictionary dataDictionary;

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager() {
        return new StarsCacheManager(dataDictionary);
    }

}
