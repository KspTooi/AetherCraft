package com.ksptool.ql.commons.exception;

public class ModelSeriesNotExistsException extends BizException{
    public ModelSeriesNotExistsException(String msg) {
        super(msg);
    }

    public ModelSeriesNotExistsException(String msg, Exception e) {
        super(msg, e);
    }
}
