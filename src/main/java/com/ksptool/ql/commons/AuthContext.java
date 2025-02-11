package com.ksptool.ql.commons;

import com.ksptool.ql.biz.model.po.UserPo;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class AuthContext {
    private static final String TOKEN_ATTRIBUTE = "CURRENT_TOKEN";
    private static final String USER_ATTRIBUTE = "CURRENT_USER";

    public static void setToken(String token) {
        RequestContextHolder.currentRequestAttributes()
            .setAttribute(TOKEN_ATTRIBUTE, token, RequestAttributes.SCOPE_REQUEST);
    }

    public static String getToken() {
        return (String) RequestContextHolder.currentRequestAttributes()
            .getAttribute(TOKEN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }

    public static void setCurrentUser(UserPo user) {
        RequestContextHolder.currentRequestAttributes()
            .setAttribute(USER_ATTRIBUTE, user, RequestAttributes.SCOPE_REQUEST);
    }

    public static UserPo getCurrentUser() {
        return (UserPo) RequestContextHolder.currentRequestAttributes()
            .getAttribute(USER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }

    public static Long getCurrentUserId() {
        UserPo user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
} 