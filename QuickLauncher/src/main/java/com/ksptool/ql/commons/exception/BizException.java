package com.ksptool.ql.commons.exception;

public class BizException extends Exception{


    public BizException(String msg) {
        super(msg);
    }

    public BizException(String msg,Exception e) {
        super(msg,e);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
