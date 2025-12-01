package com.example.hrms.controllers;

import com.example.hrms.dto.PayrollPreviewResponse;
import com.example.hrms.dto.PayrollResponse;
import com.example.hrms.dto.RunPayrollRequest;
import com.example.hrms.dto.RunPayrollResponse;
import com.example.hrms.services.PayrollService;
import com.example.hrms.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/payroll")
@RequiredArgsConstructor
@Slf4j

@PreAuthorize("hasRole('ADMIN')")
public class AdminPayrollController {

    private final PayrollService payrollService;

    /**
     * Preview payroll for a specific week
     */
    @GetMapping("/preview")

    public ResponseEntity<PayrollPreviewResponse> previewPayroll(

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {

        log.info("Payroll preview request for week starting: {}", weekStartDate);

        PayrollPreviewResponse response = payrollService.previewPayroll(weekStartDate);

        return ResponseEntity.ok(response);
    }

    /**
     * Run payroll for a specific week
     */
    @PostMapping("/run")

    public ResponseEntity<RunPayrollResponse> runPayroll(
            @Valid @RequestBody RunPayrollRequest request) {

        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Run payroll request for week: {} by admin: {}",
                request.getWeekStartDate(), username);

        RunPayrollResponse response = payrollService.runPayroll(request, username);

        return ResponseEntity.ok(response);
    }

    /**
     * Get payroll by ID
     */
    @GetMapping("/{payrollId}")
    public ResponseEntity<PayrollResponse> getPayrollById(@PathVariable Long payrollId) {
        log.debug("Get payroll request for ID: {}", payrollId);

        PayrollResponse response = payrollService.getPayrollById(payrollId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get payrolls by date range
     */
    @GetMapping("/history")

    public ResponseEntity<List<PayrollResponse>> getPayrollHistory(

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.debug("Payroll history request from {} to {}", startDate, endDate);

        List<PayrollResponse> response = payrollService.getPayrollsByDateRange(startDate, endDate);

        return ResponseEntity.ok(response);
    }

    /**
     * Mark payroll as paid
     */
    @PatchMapping("/{payrollId}/mark-paid")

    public ResponseEntity<Void> markPayrollAsPaid(@PathVariable Long payrollId) {
        log.info("Mark payroll as paid request for ID: {}", payrollId);

        payrollService.markPayrollAsPaid(payrollId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get current week payroll status
     */
    @GetMapping("/current-week")

    public ResponseEntity<PayrollWeekStatus> getCurrentWeekStatus() {
        LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        log.debug("Current week payroll status request for week: {}", currentWeekStart);

        try {
            PayrollPreviewResponse preview = payrollService.previewPayroll(currentWeekStart);

            PayrollWeekStatus status = PayrollWeekStatus.builder()
                    .weekStartDate(currentWeekStart)
                    .weekEndDate(currentWeekStart.plusDays(6))
                    .employeeCount(preview.getEmployeeCount())
                    .totalAmount(preview.getTotalNetPay())
                    .processed(false)
                    .build();

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            PayrollWeekStatus status = PayrollWeekStatus.builder()
                    .weekStartDate(currentWeekStart)
                    .weekEndDate(currentWeekStart.plusDays(6))
                    .employeeCount(0)
                    .processed(false)
                    .message("No approved timesheets found for this week")
                    .build();

            return ResponseEntity.ok(status);
        }
    }

    /**
     * Payroll week status DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PayrollWeekStatus {
        private LocalDate weekStartDate;
        private LocalDate weekEndDate;
        private Integer employeeCount;
        private java.math.BigDecimal totalAmount;
        private boolean processed;
        private String message;
    }
}
