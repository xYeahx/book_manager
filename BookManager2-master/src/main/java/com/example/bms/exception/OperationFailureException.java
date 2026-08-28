package com.example.bms.exception;

/**
 * 操作失败异常
 */
public class OperationFailureException extends BusinessException {
    public OperationFailureException() {
        super(500, "操作失败");
    }

    public OperationFailureException(String message) {
        super(500, message);
    }
}
