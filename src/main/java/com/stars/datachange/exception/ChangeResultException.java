package com.stars.datachange.exception;

/**
 * 数据转换结果异常
 * @author Hao.
 * @version 2.0
 * @since 2025/2/12 14:47
 */
public class ChangeResultException extends ChangeException {

    public ChangeResultException(String msg) {
        super(msg);
    }

    ChangeResultException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
