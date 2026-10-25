package org.example.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 私发红包。
 */
@Data
public class RedPacket {

    private Long id;
    private Long employeeId;
    private BigDecimal amount;
    private String message;
    private Long senderId;
    private Boolean readFlag;
    private LocalDateTime createdAt;
}
