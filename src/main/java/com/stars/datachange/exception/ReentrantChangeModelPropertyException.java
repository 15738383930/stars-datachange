package com.stars.datachange.exception;

/**
 * 数据转换模型的重入属性异常
 * @author Hao.
 * @version 1.0
 * @since 2024/9/29 10:02
 */
public class ReentrantChangeModelPropertyException extends ChangeModelPropertyException {

    public ReentrantChangeModelPropertyException(String msg) {
        super(msg);
    }

    public ReentrantChangeModelPropertyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
