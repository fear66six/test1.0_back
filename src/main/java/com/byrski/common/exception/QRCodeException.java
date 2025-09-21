package com.byrski.common.exception;

// 自定义异常类
public class QRCodeException extends Exception {
    public QRCodeException(String message) {
        super(message);
    }
    
    public QRCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}