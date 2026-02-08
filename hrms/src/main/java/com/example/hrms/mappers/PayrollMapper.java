package com.example.hrms.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.hrms.dto.PayrollPreviewResponse;
import com.example.hrms.dto.PayrollResponse;
import com.example.hrms.models.Employee;
import com.example.hrms.models.Payroll;

@Component
public class PayrollMapper {

    /**
     * Convert Payroll entity to PayrollResponse DTO
     */
    public PayrollResponse toResponse(Payroll payroll) {
        if (payroll == null) {
            return null;
        }

        return PayrollResponse.builder()
                .id(payroll.getId())
                .employee(toEmployeeInfo(payroll.getEmployee()))
                .payPeriodStart(payroll.getPayPeriodStart())
                .payPeriodEnd(payroll.getPayPeriodEnd())
                .grossPay(payroll.getGrossPay())
                .netPay(payroll.getNetPay())
                .taxDeduction(payroll.getTaxDeduction())
                .otherDeductions(payroll.getOtherDeductions())
                .bonus(payroll.getBonus())
                .status(payroll.getStatus())
                .processedAt(payroll.getProcessedAt())
                .processedBy(payroll.getProcessedBy() != null ?
                        payroll.getProcessedBy().getFullName() : null)
                .paymentDate(payroll.getPaymentDate())
                .createdAt(payroll.getCreatedAt())
                .build();
    }

    /**
     * Convert Employee to EmployeeInfo
     */
    private PayrollResponse.EmployeeInfo toEmployeeInfo(Employee employee) {
        if (employee == null) {
            return null;
        }

        return PayrollResponse.EmployeeInfo.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .name(employee.getFullName())
                .build();
    }

    /**
     * Convert list of Payroll to list of PayrollResponse
     */
    public List<PayrollResponse> toResponseList(List<Payroll> payrolls) {
        if (payrolls == null) {
            return null;
        }

        return payrolls.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Payroll entity to PayrollPreviewResponse.EmployeePayrollInfo
     */
    public PayrollPreviewResponse.EmployeePayrollInfo toEmployeePayrollInfo(Payroll payroll) {
        if (payroll == null) {
            return null;
        }

        // Calculate hours worked from gross pay and hourly rate
        BigDecimal hoursWorked = BigDecimal.ZERO;
        if (payroll.getEmployee().getPayInfo() != null &&
                payroll.getEmployee().getPayInfo().getHourlyRate() != null &&
                payroll.getEmployee().getPayInfo().getHourlyRate().compareTo(BigDecimal.ZERO) > 0) {
            hoursWorked = payroll.getGrossPay()
        .divide(
                payroll.getEmployee().getPayInfo().getHourlyRate(),
                2,
                RoundingMode.HALF_UP
        );
        }

        return PayrollPreviewResponse.EmployeePayrollInfo.builder()
                .employeeId(payroll.getEmployee().getEmployeeId())
                .name(payroll.getEmployee().getFullName())
                .hoursWorked(hoursWorked)
                .grossPay(payroll.getGrossPay())
                .taxDeduction(payroll.getTaxDeduction())
                .otherDeductions(payroll.getOtherDeductions())
                .bonus(payroll.getBonus())
                .netPay(payroll.getNetPay())
                .build();
    }

    /**
     * Convert list of Payroll to PayrollPreviewResponse
     */
    public PayrollPreviewResponse toPreviewResponse(List<Payroll> payrolls,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        if (payrolls == null || payrolls.isEmpty()) {
            return PayrollPreviewResponse.builder()
                    .payPeriod(PayrollPreviewResponse.PayPeriodInfo.builder()
                            .startDate(startDate)
                            .endDate(endDate)
                            .build())
                    .employees(List.of())
                    .totalGrossPay(BigDecimal.ZERO)
                    .totalNetPay(BigDecimal.ZERO)
                    .employeeCount(0)
                    .build();
        }

        List<PayrollPreviewResponse.EmployeePayrollInfo> employeePayrolls =
                payrolls.stream()
                        .map(this::toEmployeePayrollInfo)
                        .collect(Collectors.toList());

        BigDecimal totalGross = payrolls.stream()
                .map(Payroll::getGrossPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = payrolls.stream()
                .map(Payroll::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PayrollPreviewResponse.builder()
                .payPeriod(PayrollPreviewResponse.PayPeriodInfo.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .employees(employeePayrolls)
                .totalGrossPay(totalGross)
                .totalNetPay(totalNet)
                .employeeCount(payrolls.size())
                .build();
    }
}
