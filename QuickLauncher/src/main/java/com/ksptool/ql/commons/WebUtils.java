package com.ksptool.ql.commons;

import jakarta.servlet.http.HttpServletRequest;

public class WebUtils {

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        var cookies = request.getCookies();
        if (cookies!= null) {
            for (var cookie: cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
