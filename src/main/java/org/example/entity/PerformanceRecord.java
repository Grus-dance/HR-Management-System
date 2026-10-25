package org.example.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业绩记录。
 */
@Data
public class PerformanceRecord {

    private Long id;
    private Long employeeId;
    private String period;
    private BigDecimal score;
    private String selfDesc;
    private String status;
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}
