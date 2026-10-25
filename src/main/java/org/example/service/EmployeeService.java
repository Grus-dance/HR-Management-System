package org.example.service;

import org.example.common.PageResult;
import org.example.dto.EmployeeCreateRequest;
import org.example.dto.ProfileUpdateRequest;
import org.example.vo.EmployeeDetailVo;
import org.example.vo.EmployeeVo;

import java.util.List;

/**
 * 员工档案服务。
 */
public interface EmployeeService {

    PageResult<EmployeeVo> page(String keyword, String department, String status, int page, int size);

    EmployeeVo profile(Long employeeId);

    void updateProfile(Long employeeId, ProfileUpdateRequest request);

    EmployeeDetailVo detail(Long employeeId);

    EmployeeVo create(EmployeeCreateRequest request);

    void resetPassword(Long employeeId);

    List<String> departments();
}
