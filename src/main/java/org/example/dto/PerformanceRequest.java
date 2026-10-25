package org.example.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerformanceRequest {

    @NotBlank(message = "请选择业绩月份")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "月份格式应为 yyyy-MM")
    private String period;

    @NotNull(message = "请输入业绩分")
    @DecimalMin(value = "0.00", message = "业绩分不能小于 0")
    @DecimalMax(value = "100.00", message = "业绩分不能大于 100")
    private BigDecimal score;

    @Size(max = 500, message = "自评说明最多 500 字")
    private String selfDesc;
}
