package org.example.service;

import org.example.dto.GradeAdjustRequest;
import org.example.dto.SalaryAdjustRequest;
import org.example.entity.SalaryGrade;
import org.example.vo.SalaryDetailVo;

import java.util.List;

/**
 * 薪酬服务：等级、津贴/奖金比例调整、月度薪资。
 */
public interface SalaryService {

    List<SalaryGrade> grades();

    SalaryDetailVo detail(Long employeeId, String month);

    List<SalaryDetailVo> history(Long employeeId, int months);

    SalaryDetailVo adjustAllowance(Long employeeId, SalaryAdjustRequest request, Long operatorId);

    SalaryDetailVo adjustGrade(Long employeeId, GradeAdjustRequest request, Long operatorId);
}
