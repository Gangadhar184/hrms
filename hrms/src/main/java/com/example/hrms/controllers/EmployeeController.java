package com.example.hrms.controllers;


import com.example.hrms.dto.*;
import com.example.hrms.services.EmployeeService;
import com.example.hrms.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee", description = "Common employee endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get dashboard", description = "Get employee dashboard with personal info and stats")
    public ResponseEntity<DashboardResponse> getDashboard() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Dashboard request for user: {}", username);

        DashboardResponse response = employeeService.getDashboard(username);

        return ResponseEntity.ok(response);
    }

    /**
     * Get personal information
     */
    @GetMapping("/personal-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get personal information",
            description = "Get current user's personal information")
    public ResponseEntity<EmployeeResponse> getPersonalInfo() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Personal info request for user: {}", username);

        EmployeeResponse response = employeeService.getEmployeeByUsername(username);

        return ResponseEntity.ok(response);
    }

    /**
     * Get pay information
     */
    @GetMapping("/pay-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get pay information",
            description = "Get current user's pay information")
    public ResponseEntity<PayInfoResponse> getPayInfo() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Pay info request for user: {}", username);

        PayInfoResponse response = employeeService.getPayInfo(username);

        return ResponseEntity.ok(response);
    }

    /**
     * Get contact information
     */
    @GetMapping("/contact-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get contact information",
            description = "Get current user's contact information")
    public ResponseEntity<ContactInfoResponse> getContactInfo() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Contact info request for user: {}", username);

        ContactInfoResponse response = employeeService.getContactInfo(username);

        return ResponseEntity.ok(response);
    }

    /**
     * Update contact information
     */
    @PutMapping("/contact-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Update contact information",
            description = "Update current user's contact information")
    public ResponseEntity<MessageResponse> updateContactInfo(
            @Valid @RequestBody UpdateContactInfoRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Contact info update request for user: {}", username);

        MessageResponse response = employeeService.updateContactInfo(username, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile (alternative to personal-info)
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get user profile", description = "Get current user's complete profile")
    public ResponseEntity<EmployeeResponse> getProfile() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Profile request for user: {}", username);

        EmployeeResponse response = employeeService.getEmployeeByUsername(username);

        return ResponseEntity.ok(response);
    }

}
