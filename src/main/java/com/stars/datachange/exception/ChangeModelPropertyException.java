package com.stars.datachange.exception;

/**
 * 数据转换模型的属性异常
 * @author zhouhao
 * @since  2021/9/6 10:47
 */
public class ChangeModelPropertyException extends ChangeModelException {

    public ChangeModelPropertyException(String msg) {
        super(msg);
    }

    public ChangeModelPropertyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
