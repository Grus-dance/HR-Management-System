package org.example.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PerformanceVo {

    private Long id;
    private Long employeeId;
    private String empNo;
    private String employeeName;
    private String department;
    private String period;
    private BigDecimal score;
    private String selfDesc;
    private String status;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
