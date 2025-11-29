package com.example.hrms.dto;

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
public class PayrollPreviewResponse {

    private PayPeriodInfo payPeriod;
    private List<EmployeePayrollInfo> employees;
    private BigDecimal totalGrossPay;
    private BigDecimal totalNetPay;
    private Integer employeeCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayPeriodInfo {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeePayrollInfo {
        private String employeeId;
        private String name;
        private BigDecimal hoursWorked;
        private BigDecimal grossPay;
        private BigDecimal taxDeduction;
        private BigDecimal otherDeductions;
        private BigDecimal bonus;
        private BigDecimal netPay;
    }
}
