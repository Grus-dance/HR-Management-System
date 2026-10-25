package org.example.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RedPacketRequest {

    @NotNull(message = "请选择收件员工")
    private Long employeeId;

    @NotNull(message = "请输入红包金额")
    @DecimalMin(value = "0.01", message = "红包金额需大于 0")
    @DecimalMax(value = "50000.00", message = "单笔红包不能超过 50000 元")
    private BigDecimal amount;

    @Size(max = 200, message = "附言最多 200 字")
    private String message;
}
