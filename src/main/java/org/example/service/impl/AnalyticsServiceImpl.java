package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.common.MonthUtil;
import org.example.entity.PerformanceRecord;
import org.example.mapper.EmployeeMapper;
import org.example.mapper.PerformanceMapper;
import org.example.service.AnalyticsService;
import org.example.service.SalaryService;
import org.example.vo.AnalyticsVo;
import org.example.vo.ChartSeriesVo;
import org.example.vo.EmployeeVo;
import org.example.vo.NamedValueVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理分析服务实现：薪资比例、薪资曲线、业绩曲线。
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int MAX_EMPLOYEES = 10;

    private final EmployeeMapper employeeMapper;
    private final PerformanceMapper performanceMapper;
    private final SalaryService salaryService;

    @Override
    public AnalyticsVo analyze(List<Long> employeeIds, int months) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw BusinessException.badRequest("请至少选择一名员工");
        }
        Set<Long> distinct = new LinkedHashSet<>(employeeIds);
        if (distinct.size() > MAX_EMPLOYEES) {
            throw BusinessException.badRequest("最多同时对比 " + MAX_EMPLOYEES + " 名员工");
        }
        List<EmployeeVo> employees = employeeMapper.findVosByIds(new ArrayList<>(distinct));
        if (employees.isEmpty()) {
            throw BusinessException.notFound("员工不存在");
        }
        int count = Math.min(Math.max(months, 3), 12);
        List<String> monthList = MonthUtil.lastMonths(count);
        String current = MonthUtil.current();

        AnalyticsVo vo = new AnalyticsVo();
        vo.setMonths(monthList);

        List<NamedValueVo> share = new ArrayList<>();
        for (EmployeeVo employee : employees) {
            share.add(new NamedValueVo(employee.getName(),
                    salaryService.detail(employee.getId(), current).getTotal()));
        }
        vo.setSalaryShare(share);

        List<ChartSeriesVo> salarySeries = new ArrayList<>();
        for (EmployeeVo employee : employees) {
            List<BigDecimal> values = new ArrayList<>(count);
            for (String month : monthList) {
                values.add(salaryService.detail(employee.getId(), month).getTotal());
            }
            salarySeries.add(new ChartSeriesVo(employee.getName(), values));
        }
        vo.setSalarySeries(salarySeries);

        Map<Long, Map<String, BigDecimal>> scores = new HashMap<>();
        List<PerformanceRecord> trend = performanceMapper.selectTrend(new ArrayList<>(distinct), monthList.get(0));
        for (PerformanceRecord record : trend) {
            scores.computeIfAbsent(record.getEmployeeId(), key -> new HashMap<>())
                    .put(record.getPeriod(), record.getScore());
        }
        List<ChartSeriesVo> performanceSeries = new ArrayList<>();
        for (EmployeeVo employee : employees) {
            Map<String, BigDecimal> map = scores.getOrDefault(employee.getId(), Map.of());
            List<BigDecimal> values = new ArrayList<>(count);
            for (String month : monthList) {
                values.add(map.get(month));
            }
            performanceSeries.add(new ChartSeriesVo(employee.getName(), values));
        }
        vo.setPerformanceSeries(performanceSeries);
        return vo;
    }
}
