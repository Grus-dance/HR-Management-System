package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdjustmentVo {

    private Long id;
    private Long employeeId;
    private String type;
    private String typeLabel;
    private String oldValue;
    private String newValue;
    private String reason;
    private String operatorName;
    private LocalDateTime createdAt;
}
