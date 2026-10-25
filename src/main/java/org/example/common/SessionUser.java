package org.example.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话中的登录用户。
 */
@Data
@AllArgsConstructor
public class SessionUser implements Serializable {

    private Long employeeId;
    private String empNo;
    private String name;
    private Role role;
    private boolean mustChangePassword;
}
