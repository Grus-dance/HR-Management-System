package org.example.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RedPacketVo {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private BigDecimal amount;
    private String message;
    private String senderName;
    private Boolean readFlag;
    private LocalDateTime createdAt;
}
