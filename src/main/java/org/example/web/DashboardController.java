package org.example.web;

import lombok.RequiredArgsConstructor;
import org.example.common.ApiResponse;
import org.example.common.AuthInterceptor;
import org.example.common.SessionUser;
import org.example.service.DashboardService;
import org.example.vo.DashboardVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * 首页概览接口。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardVo> dashboard(@SessionAttribute(AuthInterceptor.SESSION_KEY) SessionUser user) {
        return ApiResponse.ok(dashboardService.dashboard(user));
    }
}
