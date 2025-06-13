package com.ksptool.ql.commons.aop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.ksptool.ql.commons.annotation.PrintLog;
import com.ksptool.ql.commons.utils.GsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用于控制台打印出请求访问日志
 */
@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);

    @Pointcut("@annotation(com.ksptool.ql.commons.annotation.PrintLog)")
    public void printPointCut(){}

    @Pointcut("@within(com.ksptool.ql.commons.annotation.PrintLog)")
    public void printPointCutClass(){}

    private static final Gson gson = new Gson();


    @Around("printPointCut() || printPointCutClass()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {

        HttpServletRequest hsr = null;
        List<String> sensitiveFields = new ArrayList<>();

        try{
            hsr = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();//获取request
            sensitiveFields.addAll(getSensitiveFields(pjp));
        }catch (Exception ex){
            return pjp.proceed();
        }

        final var start = System.currentTimeMillis();
        final var method = hsr.getMethod();
        final var url = hsr.getRequestURI();
        final var source = hsr.getRemoteAddr();
        final var reqBody = getRequestBody(pjp, sensitiveFields);

        Object ret = pjp.proceed();
        final var respBody = getResponseBody(ret, sensitiveFields);

        final var end = System.currentTimeMillis();
        final var cost = end - start;
        final var isTriggerSensitive = !sensitiveFields.isEmpty();

        log.info("请求=>{}[{}] 路径:{} 载荷:{} 响应:{} 耗时:{} ms 已脱敏:{}", url,method , source, reqBody, respBody, cost, isTriggerSensitive);
        return ret;
    }

    /**
     * 获取响应体
     * @param ret 响应体
     * @param sensitiveFields 敏感字段列表
     * @return 过滤后的响应体
     */
    private String getResponseBody(Object ret, List<String> sensitiveFields){
        if(ret == null){
            return "无";
        }
        JsonElement json = gson.toJsonTree(ret);
        return filterSensitiveFields(json, sensitiveFields).toString();
    }


    /**
     * 获取请求体
     * @param pjp 切点
     * @param sensitiveFields 敏感字段列表
     * @return 过滤后的请求体
     */
    public String getRequestBody(ProceedingJoinPoint pjp, List<String> sensitiveFields){

        Object[] para = pjp.getArgs();

        if(para == null || para.length < 1){
            return "无";
        }

        MethodSignature sign = (MethodSignature) pjp.getSignature();
        Annotation[][] pAnno = sign.getMethod().getParameterAnnotations();

        if(pAnno.length < 1){
            return "无";
        }

        //反射获取参数上首个RequestBody的序列
        int seq = -1;

        for(int a=0; a < pAnno.length; a++){

            for(int b=0; b < pAnno[a].length; b++){

                if (pAnno[a][b] instanceof RequestBody) {
                    seq = a;
                    break;
                }

            }

        }

        //没有获取到RequestBody序列
        if(seq < 0){
            return "无";
        }

        try{
            JsonElement json = gson.toJsonTree(para[seq]);
            return filterSensitiveFields(json, sensitiveFields).toString();
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return "获取失败";
        }

    }


    /**
     * 过滤敏感字段
     * @param json 原始json
     * @param sensitiveFields 敏感字段列表
     * @return 过滤后的json
     */
    public JsonElement filterSensitiveFields(JsonElement json, List<String> sensitiveFields){
        
        if(json == null || sensitiveFields == null || sensitiveFields.isEmpty()){
            return json;
        }

        for(String field : sensitiveFields){
            json = GsonUtils.replaceContent(json, field, "***");
        }

        return json;
    }


    /**
     * 获取方法上的敏感字段
     * @param pjp 切点
     * @return 敏感字段列表
     */
    private List<String> getSensitiveFields(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        
        // 只检查方法上的注解
        PrintLog methodAnnotation = signature.getMethod().getAnnotation(com.ksptool.ql.commons.annotation.PrintLog.class);
        
        if (methodAnnotation != null && methodAnnotation.sensitiveFields().length > 0) {
            return Arrays.asList(methodAnnotation.sensitiveFields());
        }
        
        return Collections.emptyList();
    }

}
