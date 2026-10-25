package org.example.web;

import lombok.RequiredArgsConstructor;
import org.example.common.ApiResponse;
import org.example.entity.SalaryGrade;
import org.example.service.EmployeeService;
import org.example.service.SalaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公共字典接口。
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final EmployeeService employeeService;
    private final SalaryService salaryService;

    @GetMapping("/departments")
    public ApiResponse<List<String>> departments() {
        return ApiResponse.ok(employeeService.departments());
    }

    @GetMapping("/grades")
    public ApiResponse<List<SalaryGrade>> grades() {
        return ApiResponse.ok(salaryService.grades());
    }
}
