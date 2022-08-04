package com.stars.datachange.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 外部配置属性
 * @author zhou
 * @since 2021/6/30 13:37
 */
@Component
public final class StarsProperties {

    public static Dictionary dictionary;

    public static Config config;

    public StarsProperties(Dictionary dictionary, Config config){
        StarsProperties.dictionary = dictionary;
        StarsProperties.config = config;
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
    @ConfigurationProperties("stars.config")
    public static class Config {

        /** 默认映射属性的后缀 */
        private String[] mappingSuffix = {"Text", "Str", "Ext"};

        /** 是否启用stars-datachange banner */
        private boolean banner = true;
    }
}
