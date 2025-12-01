package com.example.hrms.controllers;

import com.example.hrms.dto.PayrollResponse;
import com.example.hrms.services.PayrollService;
import com.example.hrms.utils.SecurityUtils;
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
public class PayrollEmployeeController {

    private final PayrollService payrollService;

    /**
     * Get payroll history for current employee
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")

    public ResponseEntity<List<PayrollResponse>> getPayrollHistory() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Payroll history request for user: {}", username);

        List<PayrollResponse> response = payrollService.getEmployeePayrollHistory(username);

        return ResponseEntity.ok(response);
    }

    /**
     * Get specific payroll by ID
     */
    @GetMapping("/{payrollId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")

    public ResponseEntity<PayrollResponse> getPayrollById(@PathVariable Long payrollId) {
        log.debug("Payroll request for ID: {}", payrollId);

        PayrollResponse response = payrollService.getPayrollById(payrollId);

        return ResponseEntity.ok(response);
    }
}
