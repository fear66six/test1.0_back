package com.byrski.common.exception;

import com.byrski.domain.enums.ReturnCode;
import lombok.Getter;

@Getter
public class ByrSkiException extends RuntimeException {

    private Integer code;
    private String msg;

    public ByrSkiException() {
    }

    public ByrSkiException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public ByrSkiException(ReturnCode returnCode) {
        this(returnCode.getCode(), returnCode.getMsg());
    }

    public ByrSkiException(String msg) {
        super(msg);
        this.code = ReturnCode.FAIL.getCode();
        this.msg = msg;
    }

    public ByrSkiException(String msg, Throwable throwable) {
        super(msg, throwable);
        this.code = ReturnCode.FAIL.getCode();
        this.msg = msg;
    }

    public ByrSkiException(Throwable throwable) {
        super(throwable);
        this.code = ReturnCode.FAIL.getCode();
    }
}
