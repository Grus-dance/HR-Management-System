package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.common.MonthUtil;
import org.example.common.PageResult;
import org.example.common.PasswordHasher;
import org.example.dto.EmployeeCreateRequest;
import org.example.dto.ProfileUpdateRequest;
import org.example.entity.Employee;
import org.example.entity.EmployeeSalary;
import org.example.entity.SalaryGrade;
import org.example.entity.UserAccount;
import org.example.entity.WorkExperience;
import org.example.mapper.AccountMapper;
import org.example.mapper.EmployeeMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.SalaryMapper;
import org.example.service.EmployeeService;
import org.example.service.SalaryService;
import org.example.vo.AdjustmentVo;
import org.example.vo.EmployeeDetailVo;
import org.example.vo.EmployeeVo;
import org.example.vo.PerformanceVo;
import org.example.vo.WorkExperienceVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工档案服务实现。
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final String ACTIVE = "ACTIVE";
    private static final String INITIAL_PASSWORD = "123456";

    private final EmployeeMapper employeeMapper;
    private final AccountMapper accountMapper;
    private final SalaryMapper salaryMapper;
    private final PerformanceMapper performanceMapper;
    private final SalaryService salaryService;

    @Override
    public PageResult<EmployeeVo> page(String keyword, String department, String status, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;
        List<EmployeeVo> list = employeeMapper.selectPage(keyword, department, status, offset, safeSize);
        long total = employeeMapper.countPage(keyword, department, status);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    @Override
    public EmployeeVo profile(Long employeeId) {
        EmployeeVo vo = employeeMapper.findVoById(employeeId);
        if (vo == null) {
            throw BusinessException.notFound("员工不存在");
        }
        return vo;
    }

    @Override
    @Transactional
    public void updateProfile(Long employeeId, ProfileUpdateRequest request) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        // 只更新可编辑列，锁定字段不在 SQL 中，无法被篡改
        employeeMapper.updateProfile(employeeId, request.getPhone(), request.getEmail(), request.getAddress(),
                request.getEmergencyContact(), request.getEmergencyPhone());
    }

    @Override
    public EmployeeDetailVo detail(Long employeeId) {
        EmployeeVo employee = employeeMapper.findVoById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        EmployeeDetailVo detail = new EmployeeDetailVo();
        detail.setEmployee(employee);
        detail.setSalary(salaryService.detail(employeeId, MonthUtil.current()));

        List<WorkExperienceVo> experiences = new ArrayList<>();
        for (WorkExperience item : employeeMapper.selectExperiences(employeeId)) {
            WorkExperienceVo vo = new WorkExperienceVo();
            vo.setId(item.getId());
            vo.setCompany(item.getCompany());
            vo.setPosition(item.getPosition());
            vo.setStartDate(item.getStartDate());
            vo.setEndDate(item.getEndDate());
            vo.setDescription(item.getDescription());
            experiences.add(vo);
        }
        detail.setExperiences(experiences);

        List<PerformanceVo> performances = performanceMapper.selectByEmployee(employeeId);
        detail.setRecentPerformances(performances.size() > 3 ? performances.subList(0, 3) : performances);

        List<AdjustmentVo> adjustments = salaryMapper.selectAdjustments(employeeId, 5);
        adjustments.forEach(this::labelAdjustment);
        detail.setAdjustments(adjustments);
        return detail;
    }

    @Override
    @Transactional
    public EmployeeVo create(EmployeeCreateRequest request) {
        String empNo = request.getEmpNo().trim().toUpperCase();
        if (employeeMapper.findByEmpNo(empNo) != null) {
            throw BusinessException.conflict("员工编号 " + empNo + " 已存在");
        }
        SalaryGrade grade = salaryMapper.findGrade(request.getGradeCode());
        if (grade == null) {
            throw BusinessException.badRequest("薪酬等级不存在");
        }

        Employee employee = new Employee();
        employee.setEmpNo(empNo);
        employee.setName(request.getName().trim());
        employee.setGender(request.getGender());
        employee.setBirthDate(request.getBirthDate());
        employee.setIdCard(request.getIdCard());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setAddress(request.getAddress());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setGradeCode(request.getGradeCode());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(ACTIVE);
        employee.setEducation(request.getEducation());
        employee.setSchool(request.getSchool());
        employee.setMajor(request.getMajor());
        employee.setEmergencyContact(request.getEmergencyContact());
        employee.setEmergencyPhone(request.getEmergencyPhone());
        employeeMapper.insert(employee);

        EmployeeSalary config = new EmployeeSalary();
        config.setEmployeeId(employee.getId());
        config.setAllowance(BigDecimal.ZERO);
        config.setBonusRatio(new BigDecimal("0.2000"));
        salaryMapper.insertConfig(config);

        UserAccount account = new UserAccount();
        account.setEmployeeId(employee.getId());
        account.setPasswordHash(PasswordHasher.hash(INITIAL_PASSWORD));
        account.setRole(org.example.common.Role.EMPLOYEE.name());
        account.setMustChangePassword(true);
        accountMapper.insert(account);

        return employeeMapper.findVoById(employee.getId());
    }

    @Override
    @Transactional
    public void resetPassword(Long employeeId) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        if (accountMapper.findByEmployeeId(employeeId) == null) {
            throw BusinessException.notFound("该员工没有登录账号");
        }
        accountMapper.updatePassword(employeeId, PasswordHasher.hash(INITIAL_PASSWORD), true);
    }

    @Override
    public List<String> departments() {
        return employeeMapper.selectDepartments();
    }

    private void labelAdjustment(AdjustmentVo vo) {
        vo.setTypeLabel(switch (vo.getType()) {
            case "GRADE" -> "薪酬等级";
            case "ALLOWANCE" -> "岗位津贴";
            case "BONUS_RATIO" -> "奖金比例";
            default -> vo.getType();
        });
    }
}
