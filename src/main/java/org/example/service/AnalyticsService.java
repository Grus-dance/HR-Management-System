package org.example.service;

import org.example.vo.AnalyticsVo;

import java.util.List;

/**
 * 管理分析服务。
 */
public interface AnalyticsService {

    AnalyticsVo analyze(List<Long> employeeIds, int months);
}
