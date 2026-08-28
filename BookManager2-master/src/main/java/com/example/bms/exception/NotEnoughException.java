package com.example.bms.exception;

/**
 * 库存不足异常（图书已借走）
 */
public class NotEnoughException extends BusinessException {
    public NotEnoughException() {
        super(500, "图书库存不足");
    }

    public NotEnoughException(String message) {
        super(500, message);
    }
}
