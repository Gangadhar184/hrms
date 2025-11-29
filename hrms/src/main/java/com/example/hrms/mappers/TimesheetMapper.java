package com.example.hrms.mappers;

import com.example.hrms.dto.TimesheetEntryResponse;
import com.example.hrms.dto.TimesheetListResponse;
import com.example.hrms.dto.TimesheetResponse;
import com.example.hrms.dto.UpdateTimesheetRequest;
import com.example.hrms.models.Employee;
import com.example.hrms.models.Timesheet;
import com.example.hrms.models.TimesheetEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimesheetMapper {

    /**
     * Convert Timesheet entity to TimesheetResponse DTO
     */
    public TimesheetResponse toResponse(Timesheet timesheet) {
        if (timesheet == null) {
            return null;
        }

        return TimesheetResponse.builder()
                .id(timesheet.getId())
                .employee(toEmployeeInfo(timesheet.getEmployee()))
                .weekStartDate(timesheet.getWeekStartDate())
                .weekEndDate(timesheet.getWeekEndDate())
                .totalHours(timesheet.getTotalHours())
                .status(timesheet.getStatus())
                .submittedAt(timesheet.getSubmittedAt())
                .reviewedAt(timesheet.getReviewedAt())
                .reviewedBy(toReviewerInfo(timesheet.getReviewedBy()))
                .denialReason(timesheet.getDenialReason())
                .entries(toEntryResponseList(timesheet.getEntries()))
                .createdAt(timesheet.getCreatedAt())
                .updatedAt(timesheet.getUpdatedAt())
                .build();
    }

    /**
     * Convert Employee to EmployeeInfo
     */
    private TimesheetResponse.EmployeeInfo toEmployeeInfo(Employee employee) {
        if (employee == null) {
            return null;
        }

        return TimesheetResponse.EmployeeInfo.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .name(employee.getFullName())
                .email(employee.getEmail())
                .build();
    }

    /**
     * Convert Employee to ReviewerInfo
     */
    private TimesheetResponse.ReviewerInfo toReviewerInfo(Employee reviewer) {
        if (reviewer == null) {
            return null;
        }

        return TimesheetResponse.ReviewerInfo.builder()
                .id(reviewer.getId())
                .name(reviewer.getFullName())
                .build();
    }

    /**
     * Convert Timesheet entity to TimesheetListResponse DTO
     */
    public TimesheetListResponse toListResponse(Timesheet timesheet) {
        if (timesheet == null) {
            return null;
        }

        return TimesheetListResponse.builder()
                .id(timesheet.getId())
                .employeeName(timesheet.getEmployee().getFullName())
                .employeeId(timesheet.getEmployee().getEmployeeId())
                .weekStartDate(timesheet.getWeekStartDate())
                .weekEndDate(timesheet.getWeekEndDate())
                .totalHours(timesheet.getTotalHours())
                .status(timesheet.getStatus())
                .submittedAt(timesheet.getSubmittedAt())
                .build();
    }

    /**
     * Convert TimesheetEntry entity to TimesheetEntryResponse DTO
     */
    public TimesheetEntryResponse toEntryResponse(TimesheetEntry entry) {
        if (entry == null) {
            return null;
        }

        return TimesheetEntryResponse.builder()
                .id(entry.getId())
                .workDate(entry.getWorkDate())
                .hoursWorked(entry.getHoursWorked())
                .description(entry.getDescription())
                .build();
    }

    /**
     * Convert list of TimesheetEntry to list of TimesheetEntryResponse
     */
    public List<TimesheetEntryResponse> toEntryResponseList(List<TimesheetEntry> entries) {
        if (entries == null) {
            return null;
        }

        return entries.stream()
                .map(this::toEntryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Timesheet to list of TimesheetResponse
     */
    public List<TimesheetResponse> toResponseList(List<Timesheet> timesheets) {
        if (timesheets == null) {
            return null;
        }

        return timesheets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Timesheet to list of TimesheetListResponse
     */
    public List<TimesheetListResponse> toListResponseList(List<Timesheet> timesheets) {
        if (timesheets == null) {
            return null;
        }

        return timesheets.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert UpdateTimesheetRequest.TimesheetEntryRequest to TimesheetEntry entity
     */
    public TimesheetEntry toEntryEntity(
            UpdateTimesheetRequest.TimesheetEntryRequest request) {
        if (request == null) {
            return null;
        }

        return TimesheetEntry.builder()
                .workDate(request.getWorkDate())
                .hoursWorked(request.getHoursWorked())
                .description(request.getDescription())
                .build();
    }

    /**
     * Convert list of TimesheetEntryRequest to list of TimesheetEntry entities
     */
    public List<TimesheetEntry> toEntryEntityList(
            List<UpdateTimesheetRequest.TimesheetEntryRequest> requests) {
        if (requests == null) {
            return null;
        }

        return requests.stream()
                .map(this::toEntryEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update TimesheetEntry entity from request
     */
    public void updateEntryFromRequest(TimesheetEntry entry,
                                       UpdateTimesheetRequest.TimesheetEntryRequest request) {
        if (entry == null || request == null) {
            return;
        }

        entry.setWorkDate(request.getWorkDate());
        entry.setHoursWorked(request.getHoursWorked());
        entry.setDescription(request.getDescription());
    }
}
