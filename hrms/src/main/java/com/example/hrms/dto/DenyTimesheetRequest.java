package com.example.hrms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DenyTimesheetRequest {

    @NotBlank(message = "Denial reason is required")
    @Size(min = 10, max = 500, message = "Denial reason must be between 10 and 500 characters")
    private String reason;
}