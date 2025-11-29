package com.example.hrms.dto;

import com.example.hrms.models.PayrollStatus;
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
public class PayrollResponse {

    private Long id;
    private EmployeeInfo employee;
    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private BigDecimal grossPay;
    private BigDecimal netPay;
    private BigDecimal taxDeduction;
    private BigDecimal otherDeductions;
    private BigDecimal bonus;
    private PayrollStatus status;
    private LocalDateTime processedAt;
    private String processedBy;
    private LocalDate paymentDate;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeInfo {
        private Long id;
        private String employeeId;
        private String name;
    }
}

