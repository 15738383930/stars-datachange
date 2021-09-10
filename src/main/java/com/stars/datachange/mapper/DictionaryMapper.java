package com.stars.datachange.mapper;

import com.stars.datachange.autoconfigure.StarsProperties;
import com.stars.datachange.model.response.DataDictionaryResult;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * 数据字典持久层
 * @author zhouhao
 *
 */
public interface DictionaryMapper {

    /**
     * 查询数据字典
     * @param dictionary
     * @return
     */
    Set<DataDictionaryResult> findList(@Param("dictionary") StarsProperties.Dictionary dictionary, @Param("key") String key);

}