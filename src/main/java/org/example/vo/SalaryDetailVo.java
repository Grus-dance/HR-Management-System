package org.example.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 某员工某月的薪资明细。
 */
@Data
public class SalaryDetailVo {

    private Long employeeId;
    private String month;
    private String gradeName;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private BigDecimal bonusRatio;
    /** 当月有效业绩分，无有效业绩时为 null */
    private BigDecimal score;
    private BigDecimal performanceBonus;
    private BigDecimal redPacket;
    private BigDecimal total;
    private boolean fromSnapshot;
}
