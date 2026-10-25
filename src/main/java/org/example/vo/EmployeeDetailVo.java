package org.example.vo;

import lombok.Data;

import java.util.List;

/**
 * 员工深度详情：管理者展开行 / 人事详情使用。
 */
@Data
public class EmployeeDetailVo {

    private EmployeeVo employee;
    private SalaryDetailVo salary;
    private List<WorkExperienceVo> experiences;
    private List<PerformanceVo> recentPerformances;
    private List<AdjustmentVo> adjustments;
}
