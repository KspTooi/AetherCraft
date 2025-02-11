package com.ksptool.ql.commons.interceptor;

import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.WebUtils;
import com.ksptool.ql.commons.AuthContext;
import com.ksptool.ql.biz.model.po.UserPo;
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
            "/error",
            "/register"
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
        if (token == null) {
            res.sendRedirect("/login");
            return;
        }

        // 验证token并获取用户
        UserPo user = authService.verifyUser(req);
        if (user == null) {
            res.sendRedirect("/login");
            return;
        }

        // 将token和user存储到RequestContext中
        AuthContext.setToken(token);
        AuthContext.setCurrentUser(user);

        try {
            chain.doFilter(request, response);
        } finally {
            // 这里不需要清除，因为RequestAttributes.SCOPE_REQUEST范围的属性会在请求结束时自动清除
        }
    }

}