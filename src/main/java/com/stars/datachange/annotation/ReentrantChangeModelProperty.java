package com.stars.datachange.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据转换模型的重入属性标识
 * @author Hao.
 * @version 1.0
 * @since 2024/9/27 11:11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReentrantChangeModelProperty {
}
