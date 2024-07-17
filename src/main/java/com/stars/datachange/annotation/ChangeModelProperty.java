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
     * @return String
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    @AliasFor("value")
    String chinese() default "";

    /**
     * Need ignore delimiter of chinese<p>
     *     After using this delimiter, in {@link ChangeModelProperty#chinese}, the text after the delimiter will be ignored
     * @return String
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    String chineseIgnoreDelimiter() default "：";

    /**
     * To be determined
     * @return String
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    String english() default "";

    /**
     * Need bit operation
     * @return boolean
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    boolean bitOperation() default false;

    /**
     * Need split
     * @return boolean
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    boolean split() default false;

    /**
     * Delimiter of split<p>
     *      Take effect when {@link ChangeModelProperty#split} is true
     * @return String
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    String delimiter() default ",";

    /**
     * Need ignore data change<p>
     *      When true, the corresponding property will not be processed for data change
     * @return boolean
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    boolean ignore() default false;

    /**
     * Skip the comparison of new and old data for this property<br>
     * @return boolean
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    boolean skipComparison() default false;

    /**
     * Attribute of data conversion result mapping<p>
     *     PS: Mapped property type must be {@link String}
     * @return String
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    String mapping() default "";

    /**
     * An alias for a property<p>
     *     When converting data, you can replace the previous attribute name for data conversion
     * @return String
     * @author zhouhao
     * @since  2022/4/30 10:35
     */
    String alias() default "";
}
