package com.stars.datachange.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 外部配置属性
 * @author zhou
 * @date 2021/6/30 13:37
 */
@Component
public final class StarsProperties {

    public static Dictionary dictionary;

    public StarsProperties(Dictionary dictionary){
        StarsProperties.dictionary = dictionary;
    }

    @Data
    @Component
    @ConfigurationProperties("stars.dictionary")
    public static class Dictionary {

        /** 字典表的表名 */
        private String tableName = "sys_dictionary";

        /** 列-字段名称 */
        private String fieldName = "name";

        /** 列-字段代码 */
        private String fieldCode = "code";

        /** 列-字段值 */
        private String fieldValue = "value";

        /** 列-字段ID */
        private String fieldId = "id";

        /** 列-字段父ID */
        private String fieldParentId = "parent_id";
    }
}
