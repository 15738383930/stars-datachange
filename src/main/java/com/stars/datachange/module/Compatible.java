package com.stars.datachange.module;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * 兼容模块
 * @author Hao.
 * @version 1.0
 * @since 2021/12/27 15:06
 */
@FunctionalInterface
public interface Compatible {

    /**
     * 运行兼容模型
     * @param dataClass 数据模型
     * @param result 数据转换结果集
     * @param annotations 要兼容的注解（默认实现现有兼容的所有注解）
     * @author zhouhao
     * @since  2021/9/7 9:55
     */
    void run(Class<?> dataClass, Map<String, Object> result, Collection<Class<? extends Annotation>> annotations);
}
