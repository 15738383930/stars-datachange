package com.stars.datachange.module;

import com.stars.datachange.autoconfigure.StarsProperties;
import com.stars.datachange.mapper.StarsDictionaryMapper;
import com.stars.datachange.model.response.DataDictionaryResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 默认的数据字典
 * @author Hao.
 * @version 1.0
 * @since 2022/8/6 11:34
 */
@Component
@ConditionalOnProperty(prefix = "stars.config", name = "db", havingValue = "true", matchIfMissing = true)
@ConditionalOnSingleCandidate(DataDictionary.class)
public class DefaultDataDictionary implements DataDictionary {

    @Resource
    private StarsDictionaryMapper starsDictionaryMapper;

    @Override
    public Set<DataDictionaryResult> dataDictionary(String key) {
        return starsDictionaryMapper.findList(StarsProperties.dictionary, key);
    }

}
