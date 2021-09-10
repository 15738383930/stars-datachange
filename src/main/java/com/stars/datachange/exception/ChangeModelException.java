package com.stars.datachange.exception;

/**
 * 数据转换模型异常
 * @author zhouhao
 * @since  2021/9/6 10:47
 */
public class ChangeModelException extends ChangeException {

    public ChangeModelException(String msg) {
        super(msg);
    }

    ChangeModelException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
