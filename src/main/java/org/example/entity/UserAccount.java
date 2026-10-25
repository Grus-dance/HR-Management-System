package org.example.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录账号。
 */
@Data
public class UserAccount {

    private Long id;
    private Long employeeId;
    private String passwordHash;
    private String role;
    private Boolean mustChangePassword;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
