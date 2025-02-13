package com.stars.datachange.module;

import com.stars.datachange.model.response.DataDictionaryResult;

import java.util.Set;

/**
 * 数据字典模块
 * @author Hao.
 * @version 1.0
 * @since 2022/8/6 11:34
 */
@FunctionalInterface
public interface DataDictionary {

    /**
     * 数据字典<p>
     *     功能实现 可参考：{@link DefaultDataDictionary#dataDictionary(String)}
     * @param key 数据模型名（对应@ChangeModel中的 modelName）
     * @author zhouhao
     * @since 2022/8/6 11:34
     * @return 数据字典结果集
     */
    Set<DataDictionaryResult> dataDictionary(String key);
}
