package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.common.MonthUtil;
import org.example.common.SessionUser;
import org.example.mapper.EmployeeMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.RedPacketMapper;
import org.example.service.DashboardService;
import org.example.service.SalaryService;
import org.example.vo.DashboardVo;
import org.example.vo.PerformanceVo;
import org.example.vo.SalaryDetailVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页概览服务实现：按角色返回统计卡与最近记录。
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeMapper employeeMapper;
    private final PerformanceMapper performanceMapper;
    private final RedPacketMapper redPacketMapper;
    private final SalaryService salaryService;

    @Override
    public DashboardVo dashboard(SessionUser user) {
        return switch (user.getRole()) {
            case MANAGER -> managerDashboard();
            case HR -> hrDashboard();
            case EMPLOYEE -> employeeDashboard(user.getEmployeeId());
        };
    }

    private DashboardVo managerDashboard() {
        DashboardVo vo = new DashboardVo();
        List<DashboardVo.StatCardVo> cards = new ArrayList<>();
        long active = employeeMapper.countByStatus("ACTIVE");
        cards.add(new DashboardVo.StatCardVo("active", "在职员工", String.valueOf(active), "人"));
        cards.add(new DashboardVo.StatCardVo("resigned", "离职员工", String.valueOf(employeeMapper.countByStatus("RESIGNED")), "人"));
        cards.add(new DashboardVo.StatCardVo("payroll", "本月薪资总额", format(payrollTotal()), "元"));
        cards.add(new DashboardVo.StatCardVo("pending", "待审业绩", String.valueOf(performanceMapper.countPending()), "条"));
        vo.setCards(cards);
        vo.setDepartmentDistribution(employeeMapper.countByDepartment());
        vo.setRecentPerformances(performanceMapper.selectPage("PENDING", 0, 5));
        vo.setRecentRedPackets(redPacketMapper.selectByEmployeeForManager(null, 5));
        return vo;
    }

    private DashboardVo hrDashboard() {
        DashboardVo vo = new DashboardVo();
        List<DashboardVo.StatCardVo> cards = new ArrayList<>();
        cards.add(new DashboardVo.StatCardVo("active", "在职员工", String.valueOf(employeeMapper.countByStatus("ACTIVE")), "人"));
        YearMonth now = YearMonth.now();
        long hired = employeeMapper.countHiredBetween(now.atDay(1).toString(), now.atEndOfMonth().toString());
        cards.add(new DashboardVo.StatCardVo("hired", "本月入职", String.valueOf(hired), "人"));
        cards.add(new DashboardVo.StatCardVo("pending", "待审业绩", String.valueOf(performanceMapper.countPending()), "条"));
        cards.add(new DashboardVo.StatCardVo("payroll", "本月薪资总额", format(payrollTotal()), "元"));
        vo.setCards(cards);
        vo.setDepartmentDistribution(employeeMapper.countByDepartment());
        vo.setRecentPerformances(performanceMapper.selectPage("PENDING", 0, 5));
        return vo;
    }

    private DashboardVo employeeDashboard(Long employeeId) {
        DashboardVo vo = new DashboardVo();
        SalaryDetailVo salary = salaryService.detail(employeeId, MonthUtil.current());
        List<DashboardVo.StatCardVo> cards = new ArrayList<>();
        cards.add(new DashboardVo.StatCardVo("total", "本月应发", format(salary.getTotal()), "元"));
        cards.add(new DashboardVo.StatCardVo("base", "基本工资", format(salary.getBaseSalary()), "元"));
        cards.add(new DashboardVo.StatCardVo("bonus", "本月绩效奖金", format(salary.getPerformanceBonus()), "元"));
        cards.add(new DashboardVo.StatCardVo("redPacket", "未读红包",
                String.valueOf(redPacketMapper.countUnread(employeeId)), "个"));
        vo.setCards(cards);

        List<PerformanceVo> mine = performanceMapper.selectByEmployee(employeeId);
        vo.setRecentPerformances(mine.size() > 3 ? mine.subList(0, 3) : mine);
        vo.setRecentRedPackets(redPacketMapper.selectByEmployee(employeeId));
        return vo;
    }

    private BigDecimal payrollTotal() {
        BigDecimal total = BigDecimal.ZERO;
        List<org.example.vo.EmployeeVo> actives = employeeMapper.selectPage(null, null, "ACTIVE", 0, 1000);
        for (org.example.vo.EmployeeVo employee : actives) {
            total = total.add(salaryService.detail(employee.getId(), MonthUtil.current()).getTotal());
        }
        return total;
    }

    private String format(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return new java.text.DecimalFormat("#,##0.00").format(value);
    }
}
