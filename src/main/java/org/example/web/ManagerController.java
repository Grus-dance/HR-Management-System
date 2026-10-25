package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.ApiResponse;
import org.example.common.AuthInterceptor;
import org.example.common.PageResult;
import org.example.common.SessionUser;
import org.example.dto.GradeAdjustRequest;
import org.example.dto.RedPacketRequest;
import org.example.entity.SalaryGrade;
import org.example.service.AnalyticsService;
import org.example.service.BonusService;
import org.example.service.EmployeeService;
import org.example.service.SalaryService;
import org.example.vo.AnalyticsVo;
import org.example.vo.EmployeeDetailVo;
import org.example.vo.EmployeeVo;
import org.example.vo.RedPacketVo;
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
 * 管理者接口：员工档案、薪酬等级调整、红包、数据分析。
 */
@RestController
@RequestMapping("/api/mgr")
@RequiredArgsConstructor
public class ManagerController {

    private final EmployeeService employeeService;
    private final SalaryService salaryService;
    private final BonusService bonusService;
    private final AnalyticsService analyticsService;

    @GetMapping("/employees")
    public ApiResponse<PageResult<EmployeeVo>> employees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(employeeService.page(keyword, department, status, page, size));
    }

    @GetMapping("/employees/{id}")
    public ApiResponse<EmployeeDetailVo> employee(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.detail(id));
    }

    @PutMapping("/employees/{id}/grade")
    public ApiResponse<SalaryDetailVo> adjustGrade(@PathVariable Long id,
                                                   @Valid @RequestBody GradeAdjustRequest request,
                                                   @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(salaryService.adjustGrade(id, request, user.getEmployeeId()));
    }

    @GetMapping("/salary-grades")
    public ApiResponse<List<SalaryGrade>> grades() {
        return ApiResponse.ok(salaryService.grades());
    }

    @GetMapping("/red-packets")
    public ApiResponse<List<RedPacketVo>> redPackets(@RequestParam(required = false) Long employeeId,
                                                     @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(bonusService.listSent(null, employeeId, limit));
    }

    @PostMapping("/red-packets")
    public ApiResponse<RedPacketVo> sendRedPacket(@Valid @RequestBody RedPacketRequest request,
                                                  @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(bonusService.send(user.getEmployeeId(), request));
    }

    @GetMapping("/analytics")
    public ApiResponse<AnalyticsVo> analytics(@RequestParam List<Long> employeeIds,
                                              @RequestParam(defaultValue = "6") int months) {
        return ApiResponse.ok(analyticsService.analyze(employeeIds, months));
    }
}
