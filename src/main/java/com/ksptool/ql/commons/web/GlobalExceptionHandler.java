package com.ksptool.ql.commons.web;

import com.ksptool.ql.commons.exception.BizException;
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
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResponseEntity<Result<Object>> handleBizException(BizException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return new ResponseEntity<>(Result.error(ex), HttpStatus.OK);
    }

    /**
     * 处理所有RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Result<Object>> handleRuntimeException(RuntimeException ex) {
        log.error(ex.getMessage(),ex);
        return new ResponseEntity<>(Result.internalError(ex.getMessage(),null), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    public ResponseEntity<Result<Object>> handleSecurityException(SecurityException ex) {

        if(ex.getMessage().contains("权限")) {
            return new ResponseEntity<>(Result.internalError(ex.getMessage(),null), HttpStatus.UNAUTHORIZED);
        }
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