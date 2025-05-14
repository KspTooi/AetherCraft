package com.ksptool.ql.commons.exception;

public class AuthException extends BizException {

    public AuthException(String msg) {
        super(msg);
    }

    public AuthException(String msg, Exception e) {
        super(msg, e);
    }
}
