package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.ApiResponse;
import org.example.common.AuthInterceptor;
import org.example.common.PageResult;
import org.example.common.SessionUser;
import org.example.dto.EmployeeCreateRequest;
import org.example.dto.PerformanceReviewRequest;
import org.example.dto.SalaryAdjustRequest;
import org.example.entity.SalaryGrade;
import org.example.service.EmployeeService;
import org.example.service.PerformanceService;
import org.example.service.SalaryService;
import org.example.vo.EmployeeDetailVo;
import org.example.vo.EmployeeVo;
import org.example.vo.PerformanceVo;
import org.example.vo.SalaryDetailVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

/**
 * 一般用户接口：员工管理、薪酬调整、业绩审核。
 */
@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrController {

    private final EmployeeService employeeService;
    private final SalaryService salaryService;
    private final PerformanceService performanceService;

    @GetMapping("/employees")
    public ApiResponse<PageResult<EmployeeVo>> employees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 一般用户只能看到在职员工
        return ApiResponse.ok(employeeService.page(keyword, department, "ACTIVE", page, size));
    }

    @GetMapping("/employees/{id}")
    public ApiResponse<EmployeeDetailVo> employee(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.detail(id));
    }

    @PostMapping("/employees")
    public ApiResponse<EmployeeVo> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return ApiResponse.ok(employeeService.create(request));
    }

    @PostMapping("/employees/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        employeeService.resetPassword(id);
        return ApiResponse.ok();
    }

    @PutMapping("/employees/{id}/salary")
    public ApiResponse<SalaryDetailVo> adjustSalary(
            @PathVariable Long id,
            @Valid @RequestBody SalaryAdjustRequest request,
            @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(salaryService.adjustAllowance(id, request, user.getEmployeeId()));
    }

    @GetMapping("/performance")
    public ApiResponse<PageResult<PerformanceVo>> performance(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(performanceService.page(status, page, size));
    }

    @PostMapping("/performance/{id}/review")
    public ApiResponse<Void> review(@PathVariable Long id,
                                    @Valid @RequestBody PerformanceReviewRequest request,
                                    @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        performanceService.review(user.getEmployeeId(), id, request.getApprove(), request.getComment());
        return ApiResponse.ok();
    }

    @GetMapping("/salary-grades")
    public ApiResponse<List<SalaryGrade>> grades() {
        return ApiResponse.ok(salaryService.grades());
    }
}
