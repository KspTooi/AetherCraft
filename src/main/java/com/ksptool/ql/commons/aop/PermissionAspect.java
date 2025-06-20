package com.ksptool.ql.commons.aop;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.biz.model.vo.UserSessionVo;
import com.ksptool.ql.commons.annotation.RequirePermission;
import com.ksptool.ql.commons.annotation.RequirePermissionRest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;

/**
 * 权限校验切面
 * 用于拦截带有RequirePermission或RequirePermissionRest注解的方法，进行权限校验
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    /**
     * 环绕通知，拦截带有RequirePermission注解的方法
     * 权限不足时重定向到noPermission页面
     */
    @Around("@annotation(com.ksptool.ql.commons.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取RequirePermission注解
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            // 如果没有注解，直接执行原方法
            return joinPoint.proceed();
        }
        
        // 获取需要的权限标识
        String permission = requirePermission.value();
        
        // 检查当前用户是否有权限
        if (AuthService.hasPermission(permission)) {
            // 有权限，执行原方法
            return joinPoint.proceed();
        } else {
            // 没有权限，记录日志
            String username = getUsernameFromSession();
            String methodName = method.getDeclaringClass().getName() + "." + method.getName();
            log.warn("权限校验失败: 用户[{}]尝试访问[{}]，但缺少权限[{}]", username, methodName, permission);
            
            // 重定向到noPermission页面
            return new ModelAndView("redirect:/noPermission");
        }
    }
    
    /**
     * 环绕通知，拦截带有RequirePermissionRest注解的方法
     * 权限不足时抛出SecurityException异常
     */
    @Around("@annotation(com.ksptool.ql.commons.annotation.RequirePermissionRest)")
    public Object checkPermissionRest(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取RequirePermissionRest注解
        RequirePermissionRest requirePermissionRest = method.getAnnotation(RequirePermissionRest.class);
        if (requirePermissionRest == null) {
            // 如果没有注解，直接执行原方法
            return joinPoint.proceed();
        }
        
        // 获取需要的权限标识
        String permission = requirePermissionRest.value();
        
        // 检查当前用户是否有权限
        if (AuthService.hasPermission(permission)) {
            // 有权限，执行原方法
            return joinPoint.proceed();
        } else {
            // 没有权限，记录日志
            String username = getUsernameFromSession();
            String methodName = method.getDeclaringClass().getName() + "." + method.getName();
            log.warn("权限校验失败: 用户[{}]尝试访问REST接口[{}]，但缺少权限[{}]", username, methodName, permission);
            
            // 抛出权限不足异常
            throw new SecurityException("权限不足：" + permission);
        }
    }
    
    /**
     * 从当前会话中获取用户名
     * @return 当前用户名，如果未登录则返回"未登录用户"
     */
    private String getUsernameFromSession() {
        UserSessionVo session = AuthService.getCurrentUserSession();
        if (session == null) {
            return "未登录用户";
        }
        Long userId = session.getUserId();
        return userId != null ? "用户ID:" + userId : "未知用户";
    }
} 