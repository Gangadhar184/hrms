package com.example.hrms.dto;

import com.example.hrms.models.TimesheetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimesheetResponse {

    private Long id;
    private EmployeeInfo employee;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private BigDecimal totalHours;
    private TimesheetStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private ReviewerInfo reviewedBy;
    private String denialReason;
    private List<TimesheetEntryResponse> entries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeInfo {
        private Long id;
        private String employeeId;
        private String name;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewerInfo {
        private Long id;
        private String name;
    }
}
