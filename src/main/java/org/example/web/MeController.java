package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.ApiResponse;
import org.example.common.AuthInterceptor;
import org.example.common.SessionUser;
import org.example.dto.PerformanceRequest;
import org.example.dto.ProfileUpdateRequest;
import org.example.service.BonusService;
import org.example.service.EmployeeService;
import org.example.service.PerformanceService;
import org.example.service.ResumeService;
import org.example.service.SalaryService;
import org.example.vo.EmployeeVo;
import org.example.vo.PerformanceVo;
import org.example.vo.RedPacketVo;
import org.example.vo.SalaryDetailVo;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 个人用户接口：本人信息、薪资、业绩、红包、简历。
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final EmployeeService employeeService;
    private final SalaryService salaryService;
    private final PerformanceService performanceService;
    private final BonusService bonusService;
    private final ResumeService resumeService;

    @GetMapping("/profile")
    public ApiResponse<EmployeeVo> profile(@SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(employeeService.profile(user.getEmployeeId()));
    }

    @PutMapping("/profile")
    public ApiResponse<EmployeeVo> updateProfile(@Valid @RequestBody ProfileUpdateRequest request,
                                                 @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        employeeService.updateProfile(user.getEmployeeId(), request);
        return ApiResponse.ok(employeeService.profile(user.getEmployeeId()));
    }

    @GetMapping("/salary")
    public ApiResponse<SalaryDetailVo> salary(@RequestParam(required = false) String month,
                                              @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(salaryService.detail(user.getEmployeeId(), month));
    }

    @GetMapping("/salary/history")
    public ApiResponse<List<SalaryDetailVo>> salaryHistory(
            @RequestParam(defaultValue = "6") int months,
            @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(salaryService.history(user.getEmployeeId(), months));
    }

    @GetMapping("/performance")
    public ApiResponse<List<PerformanceVo>> performance(@SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(performanceService.listMine(user.getEmployeeId()));
    }

    @PostMapping("/performance")
    public ApiResponse<PerformanceVo> submitPerformance(@Valid @RequestBody PerformanceRequest request,
                                                        @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(performanceService.submit(user.getEmployeeId(), request));
    }

    @PutMapping("/performance/{id}")
    public ApiResponse<PerformanceVo> updatePerformance(@PathVariable Long id,
                                                        @Valid @RequestBody PerformanceRequest request,
                                                        @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(performanceService.update(user.getEmployeeId(), id, request));
    }

    @DeleteMapping("/performance/{id}")
    public ApiResponse<Void> deletePerformance(@PathVariable Long id,
                                               @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        performanceService.delete(user.getEmployeeId(), id);
        return ApiResponse.ok();
    }

    @GetMapping("/red-packets")
    public ApiResponse<List<RedPacketVo>> redPackets(@SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(bonusService.listMine(user.getEmployeeId()));
    }

    @PutMapping("/red-packets/{id}/read")
    public ApiResponse<Void> readRedPacket(@PathVariable Long id,
                                           @SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        bonusService.markRead(user.getEmployeeId(), id);
        return ApiResponse.ok();
    }

    @GetMapping("/resume.pdf")
    public ResponseEntity<byte[]> resume(@SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        byte[] pdf = resumeService.buildPdf(user.getEmployeeId());
        String fileName = resumeService.fileName(user.getEmployeeId());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
