package com.jincheng.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_COACH = "coach";
    private static final String ROLE_MEMBER = "member";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (isPublicApi(uri)) {
            return true;
        }

        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");

        if (uri.startsWith("/api/admin")) {
            return authorize(response, ROLE_ADMIN.equals(role));
        }
        if (uri.startsWith("/api/coach")) {
            return authorize(response, ROLE_COACH.equals(role));
        }
        if (uri.startsWith("/api/member")) {
            return authorize(response, ROLE_MEMBER.equals(role));
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private boolean isPublicApi(String uri) {
        return uri.startsWith("/api/login") || uri.startsWith("/api/verifyCode");
    }

    private boolean authorize(HttpServletResponse response, boolean allow) throws Exception {
        if (allow) {
            return true;
        }
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或权限不足\"}");
        return false;
    }
}
