package org.example.service;

import org.example.common.SessionUser;
import org.example.vo.DashboardVo;

/**
 * 首页概览服务。
 */
public interface DashboardService {

    DashboardVo dashboard(SessionUser user);
}
