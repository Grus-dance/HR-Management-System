package org.example.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkExperienceVo {

    private Long id;
    private String company;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
