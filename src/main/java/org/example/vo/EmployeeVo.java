package org.example.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 员工视图（列表 / 详情通用）。
 */
@Data
public class EmployeeVo {

    private Long id;
    private String empNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String idCardMasked;
    private String phone;
    private String email;
    private String address;
    private String department;
    private String position;
    private String gradeCode;
    private String gradeName;
    private BigDecimal baseSalary;
    private BigDecimal allowance;
    private BigDecimal bonusRatio;
    /** 当月已审核通过的业绩分，无则为 null */
    private BigDecimal currentScore;
    private LocalDate hireDate;
    private String status;
    private LocalDate resignDate;
    private String education;
    private String school;
    private String major;
    private String emergencyContact;
    private String emergencyPhone;
}
