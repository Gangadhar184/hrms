package com.example.hrms.controllers;

import com.example.hrms.dto.*;
import com.example.hrms.services.EmployeeService;
import com.example.hrms.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Employee", description = "Employee profile and personal information APIs")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "Get dashboard data", description = "Get dashboard summary for the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Dashboard request for user: {}", username);
        DashboardResponse response = employeeService.getDashboard(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get personal information", description = "Get personal details of the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Personal info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/personal-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeResponse> getPersonalInfo() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Personal info request for user: {}", username);
        EmployeeResponse response = employeeService.getEmployeeByUsername(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pay information", description = "Get salary and pay details of the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pay info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/pay-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PayInfoResponse> getPayInfo() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Pay info request for user: {}", username);
        PayInfoResponse response = employeeService.getPayInfo(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get contact information", description = "Get contact details of the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact info retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/contact-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ContactInfoResponse> getContactInfo() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Contact info request for user: {}", username);
        ContactInfoResponse response = employeeService.getContactInfo(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update contact information", description = "Update contact details of the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contact info updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping("/contact-info")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> updateContactInfo(@Valid @RequestBody UpdateContactInfoRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.info("Contact info update request for user: {}", username);
        MessageResponse response = employeeService.updateContactInfo(username, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user profile", description = "Get complete profile of the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeResponse> getProfile() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Profile request for user: {}", username);
        EmployeeResponse response = employeeService.getEmployeeByUsername(username);
        return ResponseEntity.ok(response);
    }
}
