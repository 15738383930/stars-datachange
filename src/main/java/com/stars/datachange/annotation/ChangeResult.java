package com.stars.datachange.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据转换结果标识<p>
 *     提供基于注解的数据转换等操作
 * @author Hao.
 * @version 1.0
 * @since 2025/2/11 14:51
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeResult {

    /**
     * reverse v to k
     * @return
     */
    boolean rollback() default false;
}
