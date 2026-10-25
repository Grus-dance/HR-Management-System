package org.example.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 曾就职经历（深度信息）。
 */
@Data
public class WorkExperience {

    private Long id;
    private Long employeeId;
    private String company;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private LocalDateTime createdAt;
}
