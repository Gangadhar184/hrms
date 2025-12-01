package com.example.hrms.controllers;

import com.example.hrms.dto.*;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.models.Employee;
import com.example.hrms.models.TimesheetStatus;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.services.EmployeeService;
import com.example.hrms.services.TimesheetService;
import com.example.hrms.utils.SecurityUtils;
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
public class ManagerController {

    private final EmployeeService employeeService;
    private final TimesheetService timesheetService;
    private final EmployeeRepository employeeRepository;

    /**
     * Get all employees reporting to manager
     */
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

    /**
     * Get timesheets for direct reports
     */
    @GetMapping("/timesheets")

    public ResponseEntity<PageResponse<TimesheetListResponse>> getTeamTimesheets(

            @RequestParam(required = false, defaultValue = "SUBMITTED") TimesheetStatus status,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @RequestParam(defaultValue = "submittedAt") String sort,

            @RequestParam(defaultValue = "asc") String direction) {

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

    /**
     * Get pending timesheets count
     */
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

    /**
     * Approve timesheet
     */
    @PostMapping("/timesheets/{timesheetId}/approve")

    public ResponseEntity<MessageResponse> approveTimesheet(@PathVariable Long timesheetId) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Timesheet approval request for ID: {} by manager: {}", timesheetId, username);

        MessageResponse response = timesheetService.approveTimesheet(timesheetId, username);

        return ResponseEntity.ok(response);
    }

    /**
     * Deny timesheet with reason
     */
    @PostMapping("/timesheets/{timesheetId}/deny")

    public ResponseEntity<MessageResponse> denyTimesheet(
            @PathVariable Long timesheetId,
            @Valid @RequestBody DenyTimesheetRequest request) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Timesheet denial request for ID: {} by manager: {}", timesheetId, username);

        MessageResponse response = timesheetService.denyTimesheet(timesheetId, username, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Get manager statistics
     */
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

    /**
     * Manager statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ManagerStatistics {
        private int directReportsCount;
        private long pendingTimesheetsCount;
    }
}
