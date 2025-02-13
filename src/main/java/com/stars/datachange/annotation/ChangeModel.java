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
     * @return Class
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    @AliasFor("value")
    Class<? extends  Enum> modelCode() default Enum.class;

    /**

     * Shortcut mode<p>
     *     When set to true, all attributes under the data model will be marked with data conversion identification<p>
     *     PS: When set to true, some functions of data conversion cannot be used, for example:
     *     {@link com.stars.datachange.utils.DataChangeUtils#dataContrast(Object, Object)}
     * @return boolean
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    boolean quick() default false;

    /**
     * Data conversion source mode<p>
     *     Provide {@link ChangeModel#modelCode} mode and database dictionary table model, NONE used as an identifier only
     * @return Source
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    Source source() default Source.NONE;

    /**
     * Data model name<p>
     *     It takes effect when {@link ChangeModel#source} is set to {@code Source.DB}.<p>
     *     The default is the class name under {@link ChangeModel} (naming convention: camel case, lowercase first letter)<p>
     *     PS: If this parameter is set to "-1", all data in the DB dictionary table is used for data conversion<p>
     * @return String
     * @author zhouhao
     * @since  2021/9/13 14:06
     */
    String modelName() default "";

    /**
     * compatible Annotations<p>
     *     When data is converted, compatible annotations are required on the attributes<p>
     *     Scan all annotations of attributes in the model by default
     * @return Class
     * @author zhouhao
     * @since 2021/12/28 9:10
     */
    Class<? extends Annotation>[] compatible() default {};

    /**
     * Data Change Source
     * @author zhouhao
     * @since  2021/9/8 15:53
     */
    enum Source {

        /** {@link ChangeModel#modelCode} */
        ENUM,

        /** Data Dictionary */
        DB,

        /** identifier */
        NONE

        ;
    }
}
