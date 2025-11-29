package com.example.hrms.dto;

import com.example.hrms.models.TimesheetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetListResponse {

    private Long id;
    private String employeeName;
    private String employeeId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private BigDecimal totalHours;
    private TimesheetStatus status;
    private LocalDateTime submittedAt;
}
