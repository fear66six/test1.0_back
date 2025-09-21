package com.byrski.common.exception.handler;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.enums.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public RestBean<Void> handleMissingParams(MissingServletRequestParameterException ex) {
        log.error("缺少必需参数{}", ex.getParameterName());
        return RestBean.failure(ReturnCode.PARAM_EXCEPTION.getCode(),
            "缺少必需参数: " + ex.getParameterName());
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public RestBean<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("参数类型错误: {} 应为 {}", ex.getName(), ex.getRequiredType().getSimpleName());
        return RestBean.failure(ReturnCode.PARAM_EXCEPTION.getCode(),
            "参数类型错误: " + ex.getName() + " 应为 " + ex.getRequiredType().getSimpleName());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestBean<Void> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("参数验证失败: {}", ex.getBindingResult().getFieldError().getDefaultMessage());
        return RestBean.failure(ReturnCode.PARAM_EXCEPTION.getCode(),
            ex.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RestBean<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("不支持的HTTP方法: {}, 支持的方法有: {}", ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        return RestBean.failure(ReturnCode.METHOD_NOT_ALLOWED.getCode(),
                "不支持的HTTP方法: " + ex.getMethod() + ", 支持的方法有: " + String.join(", ", ex.getSupportedMethods()));
    }
}