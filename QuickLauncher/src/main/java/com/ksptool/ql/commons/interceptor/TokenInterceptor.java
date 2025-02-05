package com.ksptool.ql.commons.interceptor;

import com.ksptool.ql.biz.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;


//@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final List<String> whitelist = Arrays.asList(
            "/login",
            "/css/**",
            "/js/**",
            "/h2-console/**",
            "/error"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    //@Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestUri = request.getRequestURI();

        // 白名单路径直接放行
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }

        String token = request.getHeader("token");
        if (token == null ||!isValidToken(token)) {
            response.sendRedirect("/login"); // 跳转到登录页面
            return false;
        } else {
            // Token 有效，允许访问
            return true;
        }
    }

    private boolean isValidToken(String token) {

        var userId = authService.verifyToken(token);

        if(userId == null){
            return false;
        }

        return true;
    }
}