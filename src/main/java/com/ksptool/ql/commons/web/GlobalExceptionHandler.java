package com.ksptool.ql.commons.web;

import com.ksptool.ql.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResponseEntity<Result<Object>> handleBizException(BizException ex) {
        log.error(ex.getMessage(),ex);
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Result<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("参数校验失败: {}", ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        StringBuilder errorMessageBuilder = new StringBuilder("参数校验失败: ");
        boolean first = true;
        for (FieldError error : fieldErrors) {
            if (!first) {
                errorMessageBuilder.append("; ");
            }
            errorMessageBuilder.append(String.format("字段 '%s' %s", error.getField(), error.getDefaultMessage()));
            first = false;
            log.debug("校验错误 - 字段: {}, 消息: {}", 
                      error.getField(), 
                      error.getDefaultMessage());
        }
        
        String friendlyErrorMessage = errorMessageBuilder.toString();

        return new ResponseEntity<>(Result.error(friendlyErrorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<Result<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("请求体读取失败: {}", ex.getMessage(), ex);

        String detailedMessage = "请求体格式错误或无法读取";
        Throwable cause = ex.getCause();

        if (cause instanceof JsonMappingException jme) {

            StringBuilder pathBuilder = new StringBuilder();
            for (JsonMappingException.Reference ref : jme.getPath()) {
                String fieldName = ref.getFieldName();
                if (fieldName != null) {
                    if (!pathBuilder.isEmpty()) {
                        pathBuilder.append(".");
                    }
                    pathBuilder.append(fieldName);
                }
            }
            String path = pathBuilder.toString();

            if (!path.isEmpty()) {
                String specificErrorMsg = null;
                if (cause instanceof InvalidFormatException ife) {
                    specificErrorMsg = String.format("字段 '%s' 的类型不匹配，期望类型是 '%s'",
                            path, ife.getTargetType().getSimpleName());
                }
                if (cause instanceof MismatchedInputException mie && specificErrorMsg == null) {
                    specificErrorMsg = String.format("字段 '%s' 的输入类型不匹配，期望类型是 '%s'",
                            path, mie.getTargetType().getSimpleName());
                }
                if (specificErrorMsg == null) {
                    specificErrorMsg = String.format("字段 '%s' 处理时出错: %s", path, jme.getOriginalMessage());
                }
                detailedMessage = specificErrorMsg;
            }

            if (path.isEmpty()) {
                detailedMessage = jme.getOriginalMessage();
                if (detailedMessage == null || detailedMessage.trim().isEmpty()) {
                    detailedMessage = "请求体JSON结构错误";
                }
            }

            log.warn("详细的JSON解析错误: {}", detailedMessage);
            return new ResponseEntity<>(Result.internalError(detailedMessage), HttpStatus.BAD_REQUEST);
        }

        detailedMessage = "请求体读取失败,可能的原因:请求头为空、JSON解析错误";
        return new ResponseEntity<>(Result.internalError(detailedMessage), HttpStatus.BAD_REQUEST);
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