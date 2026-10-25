package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.EmployeeSalary;
import org.example.entity.SalaryAdjustment;
import org.example.entity.SalaryGrade;
import org.example.entity.SalaryMonthly;
import org.example.vo.AdjustmentVo;

import java.util.List;

/**
 * 薪酬相关数据访问：等级、薪酬参数、月度快照、调整流水。
 */
@Mapper
public interface SalaryMapper {

    List<SalaryGrade> selectGrades();

    SalaryGrade findGrade(@Param("gradeCode") String gradeCode);

    EmployeeSalary findConfig(@Param("employeeId") Long employeeId);

    int insertConfig(EmployeeSalary config);

    int updateConfig(@Param("employeeId") Long employeeId,
                     @Param("allowance") java.math.BigDecimal allowance,
                     @Param("bonusRatio") java.math.BigDecimal bonusRatio,
                     @Param("updatedBy") Long updatedBy);

    SalaryMonthly findMonthly(@Param("employeeId") Long employeeId, @Param("month") String month);

    List<SalaryMonthly> selectMonthly(@Param("employeeId") Long employeeId,
                                      @Param("fromMonth") String fromMonth);

    List<SalaryMonthly> selectMonthlyByEmployees(@Param("employeeIds") List<Long> employeeIds,
                                                 @Param("fromMonth") String fromMonth);

    int insertMonthly(SalaryMonthly monthly);

    List<AdjustmentVo> selectAdjustments(@Param("employeeId") Long employeeId,
                                         @Param("limit") int limit);

    int insertAdjustment(SalaryAdjustment adjustment);
}
