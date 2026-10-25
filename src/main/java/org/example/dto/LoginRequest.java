package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "请输入员工编号")
    private String empNo;

    @NotBlank(message = "请输入密码")
    private String password;
}
