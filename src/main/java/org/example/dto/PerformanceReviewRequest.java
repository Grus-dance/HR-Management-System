package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PerformanceReviewRequest {

    @NotNull(message = "请选择审核结果")
    private Boolean approve;

    @Size(max = 200, message = "审核意见最多 200 字")
    private String comment;
}
