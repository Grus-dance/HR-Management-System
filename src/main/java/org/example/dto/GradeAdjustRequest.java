package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GradeAdjustRequest {

    @NotBlank(message = "请选择薪酬等级")
    private String gradeCode;

    @NotBlank(message = "请填写调整原因")
    @Size(min = 2, max = 200, message = "调整原因需为 2-200 字")
    private String reason;
}
