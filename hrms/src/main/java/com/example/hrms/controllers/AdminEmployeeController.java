package com.example.hrms.controllers;

import com.example.hrms.dto.*;
import com.example.hrms.models.Role;
import com.example.hrms.services.EmployeeService;
import com.example.hrms.services.PayInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Employees", description = "Admin employee management operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final PayInfoService payInfoService;

    @Operation(summary = "Get all employees", description = "Get all employees with pagination and optional filtering by search term or role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized (Admin only)")
    })
    @GetMapping
    public ResponseEntity<PageResponse<EmployeeListResponse>> getAllEmployees(
            @Parameter(description = "Search by name or email") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role") @RequestParam(required = false) Role role,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "lastName") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        log.debug("Get all employees request - page: {}, size: {}, search: {}, role: {}",
                page, size, search, role);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<EmployeeListResponse> response;

        if (search != null && !search.trim().isEmpty()) {
            response = employeeService.searchEmployees(search, pageable);
        } else if (role != null) {
            response = employeeService.getEmployeesByRole(role, pageable);
        } else {
            response = employeeService.getAllEmployees(pageable);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get employee by ID", description = "Get employee details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId) {
        log.debug("Get employee request for ID: {}", employeeId);
        EmployeeResponse response = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new employee", description = "Create a new employee with credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or username already exists"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        log.info("Create employee request for username: {}", request.getUsername());
        CreateEmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update employee personal info", description = "Update employee's personal information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Personal info updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PutMapping("/{employeeId}/personal-info")
    public ResponseEntity<MessageResponse> updatePersonalInfo(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeePersonalInfoRequest request) {
        log.info("Update personal info request for employee ID: {}", employeeId);
        MessageResponse response = employeeService.updateEmployeePersonalInfo(employeeId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update employee pay info", description = "Update employee's salary and pay information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pay info updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PutMapping("/{employeeId}/pay-info")
    public ResponseEntity<MessageResponse> updatePayInfo(
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Valid @RequestBody UpdatePayInfoRequest request) {
        log.info("Update pay info request for employee ID: {}", employeeId);
        MessageResponse response = payInfoService.updatePayInfo(employeeId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get active managers", description = "Get all active managers for employee assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Managers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/managers")
    public ResponseEntity<List<EmployeeResponse>> getActiveManagers() {
        log.debug("Get active managers request");
        List<EmployeeResponse> response = employeeService.getAllActiveManagers();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get employee statistics", description = "Get employee count statistics by role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/statistics/count-by-role")
    public ResponseEntity<EmployeeStatistics> getStatistics() {
        log.debug("Get employee statistics request");
        PageResponse<EmployeeListResponse> allEmployees =
                employeeService.getAllEmployees(PageRequest.of(0, 1));
        EmployeeStatistics stats = EmployeeStatistics.builder()
                .totalEmployees(allEmployees.getTotalElements())
                .build();
        return ResponseEntity.ok(stats);
    }


    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EmployeeStatistics {
        private long totalEmployees;
        private long totalAdmins;
        private long totalManagers;
        private long totalRegularEmployees;
    }
}