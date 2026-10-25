package org.example.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录与角色拦截器：接口层第一道权限校验。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "LOGIN_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith("/api/")) {
            return true;
        }
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(SESSION_KEY);
        if (user == null) {
            throw BusinessException.unauthorized("登录已失效，请重新登录");
        }
        if (path.startsWith("/api/mgr/") && user.getRole() != Role.MANAGER) {
            throw BusinessException.forbidden("仅管理者可以访问该功能");
        }
        if (path.startsWith("/api/hr/") && user.getRole() != Role.HR) {
            throw BusinessException.forbidden("仅一般用户可以访问该功能");
        }
        boolean allowedWhenMustChange = path.equals("/api/auth/password")
                || path.equals("/api/auth/logout")
                || path.equals("/api/auth/me");
        if (user.isMustChangePassword() && !allowedWhenMustChange) {
            throw new BusinessException(40302, "首次登录请先修改密码");
        }
        return true;
    }
}
