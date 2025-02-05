package com.ksptool.ql.commons.interceptor;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.WebUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1) // 设置过滤器顺序，确保在其他过滤器之前执行
public class TokenFilter implements Filter {

    @Autowired
    private AuthService authService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/login",
            "/css/**",
            "/js/**",
            "/img/**",
            "/commons/**",
            "/res/**",
            "/h2-console/**",
            "/error"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        // 白名单路径直接放行
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, uri)) {
                chain.doFilter(request, response);
                return;
            }
        }

        String token = WebUtils.getCookieValue(req, "token");
        if (token == null || authService.verifyToken(token) == null) {
            res.sendRedirect("/login"); // 跳转到登录页面
            return;
        }

        chain.doFilter(request, response);
    }

}