package org.example.service;

import org.example.vo.LoginVo;

/**
 * 认证与账号服务。
 */
public interface AuthService {

    LoginVo login(String empNo, String password);

    LoginVo me(Long employeeId);

    void changePassword(Long employeeId, String oldPassword, String newPassword);
}
