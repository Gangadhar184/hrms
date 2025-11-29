package com.example.hrms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTimesheetRequest {

    @NotNull(message = "Entries are required")
    @Valid
    private List<TimesheetEntryRequest> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimesheetEntryRequest {

        @NotNull(message = "Work date is required")
        private LocalDate workDate;

        @NotNull(message = "Hours worked is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hours worked must be greater than 0")
        @DecimalMax(value = "24.0", message = "Hours worked cannot exceed 24 hours")
        private BigDecimal hoursWorked;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
    }
}

