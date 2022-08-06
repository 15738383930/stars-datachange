package com.stars.datachange.module;

import com.stars.datachange.model.response.DataDictionaryResult;

import java.util.Set;

/**
 * 数据字典模型
 * @author Hao.
 * @version 1.0
 * @since 2022/8/6 11:34
 */
@FunctionalInterface
public interface DataDictionary {

    /**
     * 数据字典<p>
     *     功能实现 可参考：{@link DefaultDataDictionary#dataDictionary(String)}
     * @param key 字典对象名
     * @author zhouhao
     * @since 2022/8/6 11:34
     * @return 数据字典结果集
     */
    Set<DataDictionaryResult> dataDictionary(String key);
}
