package com.stars.datachange.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据转换模型的属性标识
 * @author zhouhao
 * @since  2021/7/27 15:46
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeModelProperty {

    @AliasFor("chinese")
    String value() default "";

    /**
     * Chinese name of attribute
     */
    @AliasFor("value")
    String chinese() default "";

    /**
     * Need ignore delimiter of chinese<p>
     *     After using this delimiter, in {@link ChangeModelProperty#chinese}, the text after the delimiter will be ignored
     */
    String chineseIgnoreDelimiter() default "：";

    /**
     * To be determined
     */
    String english() default "";

    /**
     * Need bit operation
     */
    boolean bitOperation() default false;

    /**
     * Need split
     */
    boolean split() default false;

    /**
     * Delimiter of split<p>
     *      Take effect when {@link ChangeModelProperty#split} is true
     */
    String delimiter() default ",";

    /**
     * Need ignore data change<p>
     *      When true, the corresponding attribute will not be processed for data change
     */
    boolean ignore() default false;

    /**
     * Skip the comparison of new and old data for this attribute<p>
     */
    boolean skipComparison() default false;
}
