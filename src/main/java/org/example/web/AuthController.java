package org.example.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.ApiResponse;
import org.example.common.AuthInterceptor;
import org.example.common.BusinessException;
import org.example.common.Role;
import org.example.common.SessionUser;
import org.example.dto.ChangePasswordRequest;
import org.example.dto.LoginRequest;
import org.example.service.AuthService;
import org.example.vo.LoginVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginVo> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginVo loginVo = authService.login(request.getEmpNo(), request.getPassword());
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = httpRequest.getSession(true);
        SessionUser sessionUser = new SessionUser(loginVo.getEmployeeId(), loginVo.getEmpNo(),
                loginVo.getName(), Role.of(loginVo.getRole()), loginVo.isMustChangePassword());
        session.setAttribute(AuthInterceptor.SESSION_KEY, sessionUser);
        return ApiResponse.ok(loginVo);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<LoginVo> me(@SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(authService.me(user.getEmployeeId()));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        authService.changePassword(user.getEmployeeId(), request.getOldPassword(), request.getNewPassword());
        user.setMustChangePassword(false);
        return ApiResponse.ok();
    }
}
