package com.stars.datachange.model.response;

import lombok.Data;

import java.util.LinkedHashSet;

/**
 * 数据字典模型
 * @author zhou
 * @since 2021/9/9 11:01
 */
@Data
public class DataDictionaryResult {

    /** 字段名 */
    private String name;

    /** 字段的多选值 */
    private LinkedHashSet<Map> maps;

    /**
     * 字段的多选值模型
     * @author zhouhao
     * @since  2021/9/8 17:29
     */
    @Data
    public static class Map {

        /** 字段代码 */
        private String code;

        /** 字段值 */
        private String value;
    }
}
