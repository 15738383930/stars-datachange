package com.stars.datachange.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 数据转换模型标识<p>
 *     提供数据转换入口、绑定代码模型等操作
 * @author zhouhao
 * @since  2021/7/27 15:46
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ChangeModel {

    @AliasFor("modelCode")
    Class<? extends  Enum> value() default Enum.class;

    /**
     * code model of need to change data
     */
    @AliasFor("value")
    Class<? extends  Enum> modelCode() default Enum.class;

    /**
     * Shortcut mode<p>
     *     When set to true, all attributes under the data model will be marked with data conversion identification<p>
     *     PS: When set to true, some functions of data conversion cannot be used, for example:
     *     {@link com.ucan.shweilao.utils.DataChangeUtils#dataContrast(Object, Object)}
     */
    boolean quick() default false;

    /**
     * Data conversion source mode<p>
     *     Provide {@link ChangeModel#modelCode} mode and database dictionary table model
     */
    Source source() default Source.ENUM;

    /**
     * Data model name<p>
     *     It takes effect when {@link ChangeModel#source} is set to {@code Source.DB}.
     *     The default is the class name under {@link ChangeModel} (naming convention: camel case, lowercase first letter)
     */
    String modelName() default "";

    /**
     * 数据转换来源
     * @author zhouhao
     * @since  2021/9/8 15:53
     */
    enum Source {

        /** {@link ChangeModel#modelCode} */
        ENUM,

        /** Data Dictionary */
        DB

        ;
    }
}
