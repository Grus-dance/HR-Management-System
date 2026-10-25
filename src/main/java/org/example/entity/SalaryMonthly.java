package org.example.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 月度薪资快照（历史月份）。
 */
@Data
public class SalaryMonthly {

    private Long id;
    private Long employeeId;
    private String month;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private BigDecimal performanceBonus;
    private BigDecimal redPacket;
    private BigDecimal total;
    private LocalDateTime createdAt;
}
