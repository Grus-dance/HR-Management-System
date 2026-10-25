package org.example.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 页面拦截器：未登录访问页面时跳转登录页；按目录校验角色。
 */
@Component
public class PageAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(AuthInterceptor.SESSION_KEY);
        if (user == null) {
            response.sendRedirect("/login.html");
            return false;
        }
        if (path.startsWith("/manager/") && user.getRole() != Role.MANAGER) {
            response.sendRedirect("/login.html");
            return false;
        }
        if (path.startsWith("/hr/") && user.getRole() != Role.HR) {
            response.sendRedirect("/login.html");
            return false;
        }
        return true;
    }
}
