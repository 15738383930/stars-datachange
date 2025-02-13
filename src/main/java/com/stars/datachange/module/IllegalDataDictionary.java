package com.stars.datachange.module;

import com.stars.datachange.exception.ChangeException;
import com.stars.datachange.model.response.DataDictionaryResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 非法的数据字典模块
 * @author Hao.
 * @version 1.0
 * @since 2022/8/6 11:34
 */
@Component
@ConditionalOnProperty(prefix = "stars.config", name = "db", havingValue = "false")
@ConditionalOnSingleCandidate(DataDictionary.class)
public class IllegalDataDictionary implements DataDictionary {

    @Override
    public Set<DataDictionaryResult> dataDictionary(String key) {
        throw new ChangeException("Unusable, Make sure that the database environment is mounted to your project!");
    }

}
