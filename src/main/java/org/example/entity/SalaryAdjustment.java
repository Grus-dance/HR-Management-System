package org.example.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 薪酬调整流水。
 */
@Data
public class SalaryAdjustment {

    private Long id;
    private Long employeeId;
    private String type;
    private String oldValue;
    private String newValue;
    private String reason;
    private Long operatorId;
    private LocalDateTime createdAt;
}
