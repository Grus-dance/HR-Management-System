package org.example.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工档案（一般信息 + 教育信息 + 在职状态）。
 */
@Data
public class Employee {

    private Long id;
    private String empNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private String email;
    private String address;
    private String department;
    private String position;
    private String gradeCode;
    private LocalDate hireDate;
    private String status;
    private LocalDate resignDate;
    private String education;
    private String school;
    private String major;
    private String emergencyContact;
    private String emergencyPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
