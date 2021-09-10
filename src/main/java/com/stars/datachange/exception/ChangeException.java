package com.stars.datachange.exception;

/**
 * 数据转换异常
 * @author zhouhao
 * @since  2021/9/6 10:46
 */
class ChangeException extends RuntimeException {

    ChangeException(String msg) {
        super(msg);
    }

    ChangeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
