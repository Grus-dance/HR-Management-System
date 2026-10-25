package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.common.MonthUtil;
import org.example.dto.GradeAdjustRequest;
import org.example.dto.SalaryAdjustRequest;
import org.example.entity.Employee;
import org.example.entity.EmployeeSalary;
import org.example.entity.PerformanceRecord;
import org.example.entity.SalaryAdjustment;
import org.example.entity.SalaryGrade;
import org.example.entity.SalaryMonthly;
import org.example.mapper.EmployeeMapper;
import org.example.mapper.PerformanceMapper;
import org.example.mapper.RedPacketMapper;
import org.example.mapper.SalaryMapper;
import org.example.service.SalaryService;
import org.example.vo.SalaryDetailVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 薪酬服务实现。
 * 当月薪资实时计算；历史月份优先读取 salary_monthly 快照。
 */
@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private static final BigDecimal DEFAULT_RATIO = new BigDecimal("0.2000");

    private final EmployeeMapper employeeMapper;
    private final SalaryMapper salaryMapper;
    private final PerformanceMapper performanceMapper;
    private final RedPacketMapper redPacketMapper;

    @Override
    public List<SalaryGrade> grades() {
        return salaryMapper.selectGrades();
    }

    @Override
    public SalaryDetailVo detail(Long employeeId, String month) {
        String target = (month == null || month.isBlank()) ? MonthUtil.current() : month;
        if (!MonthUtil.isValid(target)) {
            throw BusinessException.badRequest("月份格式应为 yyyy-MM");
        }
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        SalaryGrade grade = salaryMapper.findGrade(employee.getGradeCode());
        if (grade == null) {
            throw BusinessException.notFound("薪酬等级不存在");
        }

        if (target.compareTo(MonthUtil.current()) < 0) {
            SalaryMonthly snapshot = salaryMapper.findMonthly(employeeId, target);
            if (snapshot != null) {
                SalaryDetailVo vo = new SalaryDetailVo();
                vo.setEmployeeId(employeeId);
                vo.setMonth(target);
                vo.setGradeName(grade.getGradeName());
                vo.setBaseSalary(snapshot.getBaseSalary());
                vo.setAllowance(snapshot.getAllowance());
                vo.setPerformanceBonus(snapshot.getPerformanceBonus());
                vo.setRedPacket(snapshot.getRedPacket());
                vo.setTotal(snapshot.getTotal());
                vo.setFromSnapshot(true);
                return vo;
            }
        }

        EmployeeSalary config = salaryMapper.findConfig(employeeId);
        BigDecimal base = grade.getBaseSalary();
        BigDecimal allowance = config != null && config.getAllowance() != null ? config.getAllowance() : BigDecimal.ZERO;
        BigDecimal ratio = config != null && config.getBonusRatio() != null ? config.getBonusRatio() : DEFAULT_RATIO;

        PerformanceRecord performance = performanceMapper.findByEmployeeAndPeriod(employeeId, target);
        BigDecimal score = performance != null && "APPROVED".equals(performance.getStatus())
                ? performance.getScore() : null;
        BigDecimal bonus = score == null ? BigDecimal.ZERO
                : base.multiply(ratio).multiply(score).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal redPacket = redPacketMapper.sumByMonth(employeeId, target);
        if (redPacket == null) {
            redPacket = BigDecimal.ZERO;
        }

        SalaryDetailVo vo = new SalaryDetailVo();
        vo.setEmployeeId(employeeId);
        vo.setMonth(target);
        vo.setGradeName(grade.getGradeName());
        vo.setBaseSalary(base);
        vo.setAllowance(allowance);
        vo.setBonusRatio(ratio);
        vo.setScore(score);
        vo.setPerformanceBonus(bonus);
        vo.setRedPacket(redPacket);
        vo.setTotal(base.add(allowance).add(bonus).add(redPacket));
        vo.setFromSnapshot(false);
        return vo;
    }

    @Override
    public List<SalaryDetailVo> history(Long employeeId, int months) {
        int count = Math.min(Math.max(months, 1), 12);
        List<SalaryDetailVo> list = new ArrayList<>(count);
        for (String month : MonthUtil.lastMonths(count)) {
            list.add(detail(employeeId, month));
        }
        return list;
    }

    @Override
    @Transactional
    public SalaryDetailVo adjustAllowance(Long employeeId, SalaryAdjustRequest request, Long operatorId) {
        Employee employee = requireActiveEmployee(employeeId);
        EmployeeSalary config = salaryMapper.findConfig(employeeId);
        if (config == null) {
            throw BusinessException.notFound("薪酬参数不存在");
        }
        BigDecimal oldAllowance = config.getAllowance() == null ? BigDecimal.ZERO : config.getAllowance();
        BigDecimal oldRatio = config.getBonusRatio() == null ? DEFAULT_RATIO : config.getBonusRatio();
        boolean allowanceChanged = oldAllowance.compareTo(request.getAllowance()) != 0;
        boolean ratioChanged = oldRatio.compareTo(request.getBonusRatio()) != 0;
        if (!allowanceChanged && !ratioChanged) {
            throw BusinessException.badRequest("津贴与奖金比例均未发生变化");
        }

        salaryMapper.updateConfig(employeeId, request.getAllowance(), request.getBonusRatio(), operatorId);
        if (allowanceChanged) {
            insertAdjustment(employeeId, "ALLOWANCE", oldAllowance.toPlainString(),
                    request.getAllowance().toPlainString(), request.getReason(), operatorId);
        }
        if (ratioChanged) {
            insertAdjustment(employeeId, "BONUS_RATIO", oldRatio.toPlainString(),
                    request.getBonusRatio().toPlainString(), request.getReason(), operatorId);
        }
        return detail(employee.getId(), MonthUtil.current());
    }

    @Override
    @Transactional
    public SalaryDetailVo adjustGrade(Long employeeId, GradeAdjustRequest request, Long operatorId) {
        Employee employee = requireActiveEmployee(employeeId);
        SalaryGrade grade = salaryMapper.findGrade(request.getGradeCode());
        if (grade == null) {
            throw BusinessException.badRequest("薪酬等级不存在");
        }
        if (grade.getGradeCode().equals(employee.getGradeCode())) {
            throw BusinessException.badRequest("薪酬等级未发生变化");
        }
        SalaryGrade oldGrade = salaryMapper.findGrade(employee.getGradeCode());
        employeeMapper.updateGrade(employeeId, grade.getGradeCode());
        insertAdjustment(employeeId, "GRADE",
                oldGrade == null ? employee.getGradeCode() : oldGrade.getGradeName(),
                grade.getGradeName(), request.getReason(), operatorId);
        return detail(employeeId, MonthUtil.current());
    }

    private Employee requireActiveEmployee(Long employeeId) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        if (!"ACTIVE".equals(employee.getStatus())) {
            throw BusinessException.badRequest("离职员工不能调整薪酬");
        }
        return employee;
    }

    private void insertAdjustment(Long employeeId, String type, String oldValue, String newValue,
                                  String reason, Long operatorId) {
        SalaryAdjustment adjustment = new SalaryAdjustment();
        adjustment.setEmployeeId(employeeId);
        adjustment.setType(type);
        adjustment.setOldValue(oldValue);
        adjustment.setNewValue(newValue);
        adjustment.setReason(reason);
        adjustment.setOperatorId(operatorId);
        salaryMapper.insertAdjustment(adjustment);
    }
}
