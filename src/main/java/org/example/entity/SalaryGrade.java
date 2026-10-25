package org.example.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 薪酬等级字典。
 */
@Data
public class SalaryGrade {

    private String gradeCode;
    private String gradeName;
    private BigDecimal baseSalary;
    private String description;
    private Integer sortNo;
}
