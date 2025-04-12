package com.ksptool.ql.commons.interceptor;

import com.google.gson.Gson;
import com.ksptool.ql.biz.service.AuthService;
import com.ksptool.ql.commons.WebUtils;
import com.ksptool.ql.biz.model.vo.UserSessionVo;
import com.ksptool.ql.commons.web.Result;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1) // 设置过滤器顺序，确保在其他过滤器之前执行
public class TokenFilter implements Filter {

    @Autowired
    private AuthService authService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Gson gson = new Gson();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/login",
            "/css/**",
            "/js/**",
            "/img/**",
            "/commons/**",
            "/res/**",
            "/h2-console/**",
            "/error",
            "/register",
            "/userRegister",
            "/install-wizard/**",
            "/welcome",
            "/"
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

        // 获取并验证会话
        UserSessionVo session = authService.getUserSessionByHSR(req);
        if (session == null) {
            // 检查是否为 AJAX 请求 (通过自定义请求头)
            String requestWithHeader = req.getHeader("AE-Request-With");
            if ("XHR".equalsIgnoreCase(requestWithHeader)) {
                // 如果是 AJAX 请求，返回 401 Unauthorized 和包含重定向URL的Result
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json;charset=UTF-8");
                // 添加 Location 响应头
                res.setHeader("Location", "/login");

                // 创建 Result 对象
                Result<String> result = Result.error(1, "会话已失效，请重新登录", "/login");

                try (PrintWriter writer = res.getWriter()) {
                    writer.write(gson.toJson(result));
                }
                return; // 处理完 AJAX 请求后返回
            }
            // 如果不是 AJAX 请求 (例如表单提交或直接访问URL)，执行重定向
            res.sendRedirect("/login");
            return;
        }

        // 将会话信息存储到请求上下文中
        AuthService.setCurrentUserSession(session);

        try {
            chain.doFilter(request, response);
        } finally {
            // RequestAttributes.SCOPE_REQUEST 范围的属性会在请求结束时自动清除
            // 无需手动清理 AuthService.clearCurrentUserSession();
        }
    }

}