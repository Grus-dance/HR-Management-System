package org.example.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工薪酬参数：岗位津贴与绩效奖金比例。
 */
@Data
public class EmployeeSalary {

    private Long employeeId;
    private BigDecimal allowance;
    private BigDecimal bonusRatio;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
