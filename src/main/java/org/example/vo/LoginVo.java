package org.example.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVo {

    private Long employeeId;
    private String empNo;
    private String name;
    private String role;
    private String roleLabel;
    private boolean mustChangePassword;
}
