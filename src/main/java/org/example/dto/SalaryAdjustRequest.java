package org.example.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryAdjustRequest {

    @NotNull(message = "请输入岗位津贴")
    @DecimalMin(value = "0.00", message = "岗位津贴不能为负")
    @DecimalMax(value = "100000.00", message = "岗位津贴超出上限")
    private BigDecimal allowance;

    @NotNull(message = "请输入绩效奖金比例")
    @DecimalMin(value = "0.0000", message = "奖金比例不能为负")
    @DecimalMax(value = "1.0000", message = "奖金比例不能超过 1")
    private BigDecimal bonusRatio;

    @NotBlank(message = "请填写调整原因")
    @Size(min = 2, max = 200, message = "调整原因需为 2-200 字")
    private String reason;
}
