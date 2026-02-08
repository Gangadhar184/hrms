package com.example.hrms.controllers;

import com.example.hrms.dto.PayrollPreviewResponse;
import com.example.hrms.dto.PayrollResponse;
import com.example.hrms.dto.RunPayrollRequest;
import com.example.hrms.dto.RunPayrollResponse;
import com.example.hrms.services.PayrollService;
import com.example.hrms.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin - Payroll", description = "Admin payroll processing operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminPayrollController {

    private final PayrollService payrollService;

    @Operation(summary = "Preview payroll", description = "Preview payroll calculations for a specific week before running")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll preview generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid week start date"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/preview")
    public ResponseEntity<PayrollPreviewResponse> previewPayroll(
            @Parameter(description = "Week start date (Monday)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        log.info("Payroll preview request for week starting: {}", weekStartDate);
        PayrollPreviewResponse response = payrollService.previewPayroll(weekStartDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Run payroll", description = "Execute payroll for a specific week, generating pay records for all employees with approved timesheets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or payroll already run for this week"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping("/run")
    public ResponseEntity<RunPayrollResponse> runPayroll(@Valid @RequestBody RunPayrollRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.info("Run payroll request for week: {} by admin: {}", request.getWeekStartDate(), username);
        RunPayrollResponse response = payrollService.runPayroll(request, username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payroll by ID", description = "Get payroll details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Payroll not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/{payrollId}")
    public ResponseEntity<PayrollResponse> getPayrollById(
            @Parameter(description = "Payroll ID") @PathVariable Long payrollId) {
        log.debug("Get payroll request for ID: {}", payrollId);
        PayrollResponse response = payrollService.getPayrollById(payrollId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payroll history", description = "Get all payrolls within a date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/history")
    public ResponseEntity<List<PayrollResponse>> getPayrollHistory(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("Payroll history request from {} to {}", startDate, endDate);
        List<PayrollResponse> response = payrollService.getPayrollsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark payroll as paid", description = "Mark a payroll record as paid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Payroll marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Payroll not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PatchMapping("/{payrollId}/mark-paid")
    public ResponseEntity<Void> markPayrollAsPaid(
            @Parameter(description = "Payroll ID") @PathVariable Long payrollId) {
        log.info("Mark payroll as paid request for ID: {}", payrollId);
        payrollService.markPayrollAsPaid(payrollId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current week payroll status", description = "Get the payroll status for the current week")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
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
