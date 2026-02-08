package com.example.hrms.controllers;

import com.example.hrms.dto.MessageResponse;
import com.example.hrms.dto.TimesheetResponse;
import com.example.hrms.dto.UpdateTimesheetRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee/timesheet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Timesheet", description = "Employee timesheet management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TimesheetController {

    private final TimesheetService timesheetService;

    @Operation(summary = "Get current week timesheet", description = "Get or create timesheet for the current week")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TimesheetResponse> getCurrentTimesheet() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Current timesheet request for user: {}", username);
        TimesheetResponse response = timesheetService.getCurrentTimesheet(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get timesheet by ID", description = "Get a specific timesheet by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Timesheet not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/{timesheetId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TimesheetResponse> getTimesheetById(
            @Parameter(description = "Timesheet ID") @PathVariable Long timesheetId) {
        log.debug("Timesheet request for ID: {}", timesheetId);
        TimesheetResponse response = timesheetService.getTimesheetById(timesheetId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get timesheet history", description = "Get all timesheets for the current employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TimesheetResponse>> getTimesheetHistory() {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.debug("Timesheet history request for user: {}", username);
        List<TimesheetResponse> response = timesheetService.getEmployeeTimesheets(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update timesheet", description = "Update timesheet entries (only for DRAFT or DENIED status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or timesheet cannot be edited"),
            @ApiResponse(responseCode = "404", description = "Timesheet not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PutMapping("/{timesheetId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TimesheetResponse> updateTimesheet(
            @Parameter(description = "Timesheet ID") @PathVariable Long timesheetId,
            @Valid @RequestBody UpdateTimesheetRequest request) {
        log.info("Timesheet update request for ID: {}", timesheetId);
        TimesheetResponse response = timesheetService.updateTimesheet(timesheetId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Submit timesheet for approval", description = "Submit timesheet to manager for review")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timesheet submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Timesheet cannot be submitted"),
            @ApiResponse(responseCode = "404", description = "Timesheet not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping("/{timesheetId}/submit")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MessageResponse> submitTimesheet(
            @Parameter(description = "Timesheet ID") @PathVariable Long timesheetId) {
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        log.info("Timesheet submit request for ID: {} by user: {}", timesheetId, username);
        MessageResponse response = timesheetService.submitTimesheet(timesheetId, username);
        return ResponseEntity.ok(response);
    }
}
