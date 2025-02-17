package com.ksptool.ql.commons.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Result<Object>> handleRuntimeException(RuntimeException ex) {
        log.error(ex.getMessage(),ex);
        return new ResponseEntity<>(Result.internalError(ex.getMessage(),null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理其他Exception
     */
    //@ExceptionHandler(Exception.class)
    //@ResponseBody
    //public ResponseEntity<Result<Void>> handleException(Exception ex) {
    //    log.error(ex.getMessage(),ex);
    //    return new ResponseEntity<>(Result.internalError(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    //}



}