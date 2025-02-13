package com.stars.datachange.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 外部配置属性
 * @author zhou
 * @since 2021/6/30 13:37
 */
@Component
public final class StarsProperties {

    public static Dictionary dictionary;

    public static Config config;

    public static Cache cache;

    public StarsProperties(Dictionary dictionary, Config config, Cache cache){
        StarsProperties.dictionary = dictionary;
        StarsProperties.config = config;
        StarsProperties.cache = cache;
    }

    @Data
    @Component
    @ConfigurationProperties("stars.dictionary")
    public static class Dictionary {

        /** 字典表的表名 */
        private String tableName = "sys_dictionary";

        /** 属性名称对应的列名 */
        private String fieldName = "name";

        /** 属性代码对应的列名 */
        private String fieldCode = "code";

        /** 属性值对应的列名 */
        private String fieldValue = "value";

        /** 主键对应的列名 */
        private String fieldId = "id";

        /** 父主键的列名 */
        private String fieldParentId = "parent_id";
    }

    @Data
    @Component
    @ConfigurationProperties("stars.cache")
    public static class Cache {

        /** 开启缓存 */
        private boolean open = false;

        /** 缓存空值 */
        private boolean cacheNullValues = false;

        /** 缓存的名称列表 */
        private Set<String> cacheNames = new HashSet<>();

        /**
         * 动态配置<p>
         * 设置为true会自动配置缓存（默认）<p>
         * 设置为false需要手动严格配置cacheNames
         */
        private boolean dynamic = true;

        /** 访问后过期（秒） */
        private long expireAfterAccess = 0L;

        /** 写入后过期（秒）：最常用，默认不过期 */
        private long expireAfterWrite = 0L;

        /**
         * 写入后刷新（秒）：高级用法 <p>
         *  缓存到指定时间后，不会过期。有新请求过来时，会刷新缓存（只会有一个线程去刷新缓存，其他线程拿历史缓存。高并发友好）。<p>
         *  如果长时间没有新请求，就一直不会刷新缓存，导致缓存数据时效率低。所以通常配合expireAfterWrite一起使用：<p>
         *      一般设置refreshAfterWrite < expireAfterWrite，即使长时间没有新请求，缓存也会由expireAfterWrite过期而过期<p>
         *      例：refreshAfterWrite（25 * 60秒） expireAfterWrite（60 * 60秒）<p>
         *      缓存每25分钟刷新一次，且在1小时后过期<p>
         *      PS: refreshAfterWrite可以保证并发性能，也会有频繁查询的开销、缓存不会过期的内存开销，配合expireAfterWrite使用效果最佳
         */
        private int refreshAfterWrite = 0;

        /** 详细的访问后过期（高优先级）：key-缓存名称 value-访问后过期时间（秒） */
        private Map<String, Long> expires = new HashMap<>();

        /** 缓存的初始容量 */
        private int initialCapacity = 1;

        /** 缓存详细的初始容量（高优先级）：key-缓存名称 value-初始容量 */
        private Map<String, Integer> capacityMap = new HashMap<>();

        /** 缓存的最大容量 */
        private long maximumSize = 10L;
    }

    @Data
    @Component
    @ConfigurationProperties("stars.config")
    public static class Config {

        /** 默认映射属性的后缀 */
        private String[] mappingSuffix = {"Text", "Str", "Ext"};

        /** 是否启用stars-datachange banner */
        private boolean banner = true;

        /** 是否数据库环境 */
        private boolean db = true;
    }
}
