package com.example.hrms.controllers;

import com.example.hrms.dto.PayrollResponse;
import com.example.hrms.services.PayrollService;
import com.example.hrms.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/payroll")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payroll", description = "Employee payroll history APIs")
@SecurityRequirement(name = "bearerAuth")
public class PayrollEmployeeController {

    private final PayrollService payrollService;

    @Operation(summary = "Get payroll history", description = "Get all payroll records for the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<PayrollResponse>> getPayrollHistory() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Payroll history request for user: {}", username);
        List<PayrollResponse> response = payrollService.getEmployeePayrollHistory(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payroll by ID", description = "Get a specific payroll record by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Payroll not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{payrollId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PayrollResponse> getPayrollById(
            @Parameter(description = "Payroll ID") @PathVariable Long payrollId) {
        log.debug("Payroll request for ID: {}", payrollId);
        PayrollResponse response = payrollService.getPayrollById(payrollId);
        return ResponseEntity.ok(response);
    }
}
