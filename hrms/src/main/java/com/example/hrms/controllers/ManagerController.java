package com.example.hrms.controllers;

import com.example.hrms.dto.*;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.models.Employee;
import com.example.hrms.models.TimesheetStatus;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.services.EmployeeService;
import com.example.hrms.services.TimesheetService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
@Tag(name = "Manager", description = "Manager operations for team management")
@SecurityRequirement(name = "bearerAuth")
public class ManagerController {

    private final EmployeeService employeeService;
    private final TimesheetService timesheetService;
    private final EmployeeRepository employeeRepository;

    @Operation(summary = "Get direct reports", description = "Get all employees reporting to the current manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Direct reports retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized (not a manager)")
    })
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getDirectReports() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Employee manager = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        log.debug("Direct reports request for manager: {}", username);

        List<EmployeeResponse> response = employeeService.getDirectReports(manager.getId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get team timesheets", description = "Get timesheets for direct reports with optional status filter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheets retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/timesheets")
    public ResponseEntity<PageResponse<TimesheetListResponse>> getTeamTimesheets(
            @Parameter(description = "Filter by status") @RequestParam(required = false, defaultValue = "SUBMITTED") TimesheetStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "submittedAt") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String direction) {

        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Employee manager = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        log.debug("Team timesheets request for manager: {} with status: {}", username, status);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<TimesheetListResponse> response =
                timesheetService.getTimesheetsForManager(manager.getId(), status, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pending timesheets count", description = "Get count of timesheets pending approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/timesheets/pending/count")
    public ResponseEntity<Long> getPendingTimesheetsCount() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Employee manager = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        log.debug("Pending timesheets count request for manager: {}", username);

        long count = timesheetService.getPendingTimesheetsCount(manager.getId());

        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Get timesheet details", description = "Get timesheet details by ID (for direct reports only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Timesheet not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this timesheet"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/timesheets/{timesheetId}")
    public ResponseEntity<TimesheetResponse> getTimesheetById(
            @Parameter(description = "Timesheet ID") @PathVariable Long timesheetId) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Employee manager = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        log.debug("Timesheet detail request for ID: {} by manager: {}", timesheetId, username);

        TimesheetResponse response = timesheetService.getTimesheetByIdForManager(timesheetId, manager.getId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Approve timesheet", description = "Approve a submitted timesheet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet approved successfully"),
            @ApiResponse(responseCode = "400", description = "Timesheet cannot be approved"),
            @ApiResponse(responseCode = "404", description = "Timesheet not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/timesheets/{timesheetId}/approve")
    public ResponseEntity<MessageResponse> approveTimesheet(
            @Parameter(description = "Timesheet ID") @PathVariable Long timesheetId) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Timesheet approval request for ID: {} by manager: {}", timesheetId, username);

        MessageResponse response = timesheetService.approveTimesheet(timesheetId, username);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deny timesheet", description = "Deny a submitted timesheet with a reason")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet denied successfully"),
            @ApiResponse(responseCode = "400", description = "Timesheet cannot be denied or reason not provided"),
            @ApiResponse(responseCode = "404", description = "Timesheet not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/timesheets/{timesheetId}/deny")
    public ResponseEntity<MessageResponse> denyTimesheet(
            @Parameter(description = "Timesheet ID") @PathVariable Long timesheetId,
            @Valid @RequestBody DenyTimesheetRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Timesheet denial request for ID: {} by manager: {}", timesheetId, username);

        MessageResponse response = timesheetService.denyTimesheet(timesheetId, username, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get manager statistics", description = "Get statistics about team timesheets and approvals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @GetMapping("/statistics")
    public ResponseEntity<ManagerStatistics> getStatistics() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Employee manager = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));

        log.debug("Statistics request for manager: {}", username);

        List<EmployeeResponse> directReports = employeeService.getDirectReports(manager.getId());
        long pendingTimesheets = timesheetService.getPendingTimesheetsCount(manager.getId());

        ManagerStatistics stats = ManagerStatistics.builder()
                .directReportsCount(directReports.size())
                .pendingTimesheetsCount(pendingTimesheets)
                .build();

        return ResponseEntity.ok(stats);
    }


    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ManagerStatistics {
        private int directReportsCount;
        private long pendingTimesheetsCount;
    }
}
