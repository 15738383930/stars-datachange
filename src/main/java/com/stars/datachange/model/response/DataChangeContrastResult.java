package com.stars.datachange.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据转换对比结果
 * @author zhou
 * @date 2021/9/9 11:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataChangeContrastResult {

    /** 字段中文名 */
    private String name;

    /** 老的字段值 */
    private Object oldData;

    /** 新的字段值 */
    private Object newData;
}
