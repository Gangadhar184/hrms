package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunPayrollResponse {

    private String message;
    private Integer processedCount;
    private BigDecimal totalAmount;
    private LocalDateTime processedAt;
}
