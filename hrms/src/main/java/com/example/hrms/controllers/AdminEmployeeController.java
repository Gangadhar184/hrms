package com.example.hrms.controllers;

import com.example.hrms.dto.*;
import com.example.hrms.models.Role;
import com.example.hrms.services.EmployeeService;
import com.example.hrms.services.PayInfoService;

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
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final PayInfoService payInfoService;

    /**
     * Get all employees with pagination and filtering
     */
    @GetMapping

    public ResponseEntity<PageResponse<EmployeeListResponse>> getAllEmployees(

            @RequestParam(required = false) String search,

            @RequestParam(required = false) Role role,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @RequestParam(defaultValue = "lastName") String sort,

            @RequestParam(defaultValue = "asc") String direction) {

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

    /**
     * Get employee by ID
     */
    @GetMapping("/{employeeId}")

    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long employeeId) {
        log.debug("Get employee request for ID: {}", employeeId);

        EmployeeResponse response = employeeService.getEmployeeById(employeeId);

        return ResponseEntity.ok(response);
    }

    /**
     * Create new employee
     */
    @PostMapping

    public ResponseEntity<CreateEmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        log.info("Create employee request for username: {}", request.getUsername());

        CreateEmployeeResponse response = employeeService.createEmployee(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update employee personal information
     */
    @PutMapping("/{employeeId}/personal-info")

    public ResponseEntity<MessageResponse> updatePersonalInfo(
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeePersonalInfoRequest request) {
        log.info("Update personal info request for employee ID: {}", employeeId);

        MessageResponse response = employeeService.updateEmployeePersonalInfo(employeeId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Update employee pay information
     */
    @PutMapping("/{employeeId}/pay-info")

    public ResponseEntity<MessageResponse> updatePayInfo(
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdatePayInfoRequest request) {
        log.info("Update pay info request for employee ID: {}", employeeId);

        MessageResponse response = payInfoService.updatePayInfo(employeeId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all active managers (for assignment)
     */
    @GetMapping("/managers")

    public ResponseEntity<List<EmployeeResponse>> getActiveManagers() {
        log.debug("Get active managers request");

        List<EmployeeResponse> response = employeeService.getAllActiveManagers();

        return ResponseEntity.ok(response);
    }

    /**
     * Get employee count by role
     */
    @GetMapping("/statistics/count-by-role")

    public ResponseEntity<EmployeeStatistics> getStatistics() {
        log.debug("Get employee statistics request");

        // This is simplified - you might want to add repository methods for actual counts
        PageResponse<EmployeeListResponse> allEmployees =
                employeeService.getAllEmployees(PageRequest.of(0, 1));

        EmployeeStatistics stats = EmployeeStatistics.builder()
                .totalEmployees(allEmployees.getTotalElements())
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Employee statistics DTO
     */
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