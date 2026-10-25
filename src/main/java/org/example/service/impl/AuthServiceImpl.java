package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.common.PasswordHasher;
import org.example.entity.Employee;
import org.example.entity.UserAccount;
import org.example.mapper.AccountMapper;
import org.example.mapper.EmployeeMapper;
import org.example.service.AuthService;
import org.example.vo.LoginVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final EmployeeMapper employeeMapper;
    private final AccountMapper accountMapper;

    @Override
    public LoginVo login(String empNo, String password) {
        Employee employee = employeeMapper.findByEmpNo(empNo == null ? null : empNo.trim().toUpperCase());
        // 离职与不存在统一提示，避免泄露账号状态
        if (employee == null || !"ACTIVE".equals(employee.getStatus())) {
            throw BusinessException.unauthorized("工号或密码错误");
        }
        UserAccount account = accountMapper.findByEmployeeId(employee.getId());
        if (account == null || !PasswordHasher.matches(password, account.getPasswordHash())) {
            throw BusinessException.unauthorized("工号或密码错误");
        }
        accountMapper.updateLastLogin(employee.getId());
        return toLoginVo(employee, account);
    }

    @Override
    public LoginVo me(Long employeeId) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.unauthorized("登录已失效，请重新登录");
        }
        UserAccount account = accountMapper.findByEmployeeId(employeeId);
        if (account == null) {
            throw BusinessException.unauthorized("账号不存在，请联系人事");
        }
        return toLoginVo(employee, account);
    }

    @Override
    @Transactional
    public void changePassword(Long employeeId, String oldPassword, String newPassword) {
        UserAccount account = accountMapper.findByEmployeeId(employeeId);
        if (account == null) {
            throw BusinessException.notFound("账号不存在");
        }
        if (!PasswordHasher.matches(oldPassword, account.getPasswordHash())) {
            throw BusinessException.badRequest("原密码不正确");
        }
        if (PasswordHasher.matches(newPassword, account.getPasswordHash())) {
            throw BusinessException.badRequest("新密码不能与原密码相同");
        }
        accountMapper.updatePassword(employeeId, PasswordHasher.hash(newPassword), false);
    }

    private LoginVo toLoginVo(Employee employee, UserAccount account) {
        return LoginVo.builder()
                .employeeId(employee.getId())
                .empNo(employee.getEmpNo())
                .name(employee.getName())
                .role(account.getRole())
                .roleLabel(org.example.common.Role.of(account.getRole()).getLabel())
                .mustChangePassword(Boolean.TRUE.equals(account.getMustChangePassword()))
                .build();
    }
}
