package com.example.hrms.controllers;

import com.example.hrms.dto.MessageResponse;
import com.example.hrms.dto.TimesheetResponse;
import com.example.hrms.dto.UpdateTimesheetRequest;
import com.example.hrms.services.TimesheetService;
import com.example.hrms.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/timesheet")
@RequiredArgsConstructor
@Slf4j
public class TimesheetController {

    private final TimesheetService timesheetService;

    /**
     * Get current week timesheet
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")

    public ResponseEntity<TimesheetResponse> getCurrentTimesheet() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Current timesheet request for user: {}", username);

        TimesheetResponse response = timesheetService.getCurrentTimesheet(username);

        return ResponseEntity.ok(response);
    }

    /**
     * Get timesheet by ID
     */
    @GetMapping("/{timesheetId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")

    public ResponseEntity<TimesheetResponse> getTimesheetById(
            @PathVariable Long timesheetId) {
        log.debug("Timesheet request for ID: {}", timesheetId);

        TimesheetResponse response = timesheetService.getTimesheetById(timesheetId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all timesheets for current employee
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TimesheetResponse>> getTimesheetHistory() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.debug("Timesheet history request for user: {}", username);

        List<TimesheetResponse> response = timesheetService.getEmployeeTimesheets(username);

        return ResponseEntity.ok(response);
    }

    /**
     * Update timesheet entries
     */
    @PutMapping("/{timesheetId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TimesheetResponse> updateTimesheet(
            @PathVariable Long timesheetId,
            @Valid @RequestBody UpdateTimesheetRequest request) {
        log.info("Timesheet update request for ID: {}", timesheetId);

        TimesheetResponse response = timesheetService.updateTimesheet(timesheetId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Submit timesheet for approval
     */
    @PostMapping("/{timesheetId}/submit")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> submitTimesheet(@PathVariable Long timesheetId) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        log.info("Timesheet submit request for ID: {} by user: {}", timesheetId, username);

        MessageResponse response = timesheetService.submitTimesheet(timesheetId, username);

        return ResponseEntity.ok(response);
    }

}
