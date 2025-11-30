package com.example.hrms.services;

import com.example.hrms.dto.*;
import com.example.hrms.exceptions.BadRequestException;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.mappers.PageMapper;
import com.example.hrms.mappers.TimesheetMapper;
import com.example.hrms.models.Employee;
import com.example.hrms.models.Timesheet;
import com.example.hrms.models.TimesheetEntry;
import com.example.hrms.models.TimesheetStatus;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.TimesheetEntryRepository;
import com.example.hrms.repositories.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimesheetService {
    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final TimesheetMapper timesheetMapper;
    private final PageMapper pageMapper;

    /**
     * Retrieves the current weekly timesheet for the employee associated with the given username.
     * <p>
     * If a timesheet for the current week does not exist, a new one is automatically created.
     * </p>
     *
     * @param username the username of the employee
     * @return the current week's {@link TimesheetResponse}
     * @throws ResourceNotFoundException if the employee is not found
     */
    @Transactional(readOnly = true)
    public TimesheetResponse getCurrentTimesheet(String username) {
        log.debug("Fetching current timesheet for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Calculate current week start date (Monday)
        LocalDate weekStartDate = getCurrentWeekStartDate();

        Timesheet timesheet = timesheetRepository
                .findByEmployeeIdAndWeekStartDate(employee.getId(), weekStartDate)
                .orElseGet(() -> createNewTimesheet(employee, weekStartDate));

        return timesheetMapper.toResponse(timesheet);
    }

    /**
     * Retrieves a timesheet by its ID.
     *
     * @param timesheetId the ID of the timesheet
     * @return a {@link TimesheetResponse} containing the timesheet details
     * @throws ResourceNotFoundException if the timesheet is not found
     */
    @Transactional(readOnly = true)
    public TimesheetResponse getTimesheetById(Long timesheetId) {
        log.debug("Fetching timesheet by ID: {}", timesheetId);

        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Timesheet not found with ID: " + timesheetId));

        return timesheetMapper.toResponse(timesheet);
    }

    /**
     * Retrieves all timesheets belonging to the specified employee.
     *
     * @param username the username of the employee
     * @return a list of {@link TimesheetResponse} objects sorted by week in descending order
     * @throws ResourceNotFoundException if the employee does not exist
     */
    @Transactional(readOnly = true)
    public List<TimesheetResponse> getEmployeeTimesheets(String username) {
        log.debug("Fetching all timesheets for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        List<Timesheet> timesheets = timesheetRepository
                .findByEmployeeIdOrderByWeekStartDateDesc(employee.getId());

        return timesheetMapper.toResponseList(timesheets);
    }

    /**
     * Updates the entries of an existing timesheet.
     * <p>
     * Existing entries are removed and replaced with the ones supplied in the request.
     * Timesheets can only be updated while in editable statuses (e.g., DRAFT).
     * </p>
     *
     * @param timesheetId the ID of the timesheet to update
     * @param request     the new timesheet details
     * @return the updated {@link TimesheetResponse}
     * @throws ResourceNotFoundException if the timesheet does not exist
     * @throws BadRequestException       if the timesheet cannot be edited or contains invalid entries
     */
    @Transactional
    public TimesheetResponse updateTimesheet(Long timesheetId, UpdateTimesheetRequest request) {
        log.info("Updating timesheet ID: {}", timesheetId);

        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Timesheet not found with ID: " + timesheetId));

        // Check if timesheet can be edited
        if (!timesheet.getStatus().canBeEdited()) {
            throw new BadRequestException(
                    "Timesheet cannot be edited. Status: " + timesheet.getStatus());
        }

        // Clear existing entries
        timesheetEntryRepository.deleteByTimesheetId(timesheetId);
        timesheet.getEntries().clear();

        // Add new entries
        request.getEntries().forEach(entryRequest -> {
            // Validate work date is within timesheet week
            if (entryRequest.getWorkDate().isBefore(timesheet.getWeekStartDate()) ||
                    entryRequest.getWorkDate().isAfter(timesheet.getWeekEndDate())) {
                throw new BadRequestException(
                        "Work date must be within timesheet week: " +
                                timesheet.getWeekStartDate() + " to " + timesheet.getWeekEndDate());
            }

            TimesheetEntry entry = timesheetMapper.toEntryEntity(entryRequest);
            timesheet.addEntry(entry);
        });

        // Calculate total hours
        timesheet.calculateTotalHours();

        Timesheet saved = timesheetRepository.save(timesheet);

        log.info("Timesheet updated successfully. ID: {}, Total hours: {}",
                saved.getId(), saved.getTotalHours());

        return timesheetMapper.toResponse(saved);
    }

    /**
     * Submits a timesheet for manager approval.
     *
     * @param timesheetId the ID of the timesheet being submitted
     * @param username    the username of the employee submitting the timesheet
     * @return a {@link MessageResponse} confirming submission
     * @throws ResourceNotFoundException if the timesheet or employee is not found
     * @throws BadRequestException       if the user is not the owner or the timesheet cannot be submitted
     */
    @Transactional
    public MessageResponse submitTimesheet(Long timesheetId, String username) {
        log.info("Submitting timesheet ID: {} by user: {}", timesheetId, username);

        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Timesheet not found with ID: " + timesheetId));

        // Verify ownership
        if (!timesheet.getEmployee().getUsername().equals(username)) {
            throw new BadRequestException("You can only submit your own timesheets");
        }

        // Check if can be submitted
        if (!timesheet.getStatus().canBeSubmitted()) {
            throw new BadRequestException(
                    "Timesheet cannot be submitted. Status: " + timesheet.getStatus());
        }

        // Validate has entries
        if (timesheet.getEntries().isEmpty()) {
            throw new BadRequestException("Cannot submit empty timesheet");
        }

        timesheet.submit();
        timesheetRepository.save(timesheet);

        log.info("Timesheet submitted successfully. ID: {}", timesheetId);

        return new MessageResponse("Timesheet submitted for approval");
    }

    /**
     * Retrieves a paginated list of timesheets for a manager’s direct reports,
     * filtered by status.
     *
     * @param managerId the manager’s employee ID
     * @param status    the desired timesheet status filter
     * @param pageable  pagination details
     * @return a {@link PageResponse} of {@link TimesheetListResponse}
     */
    @Transactional(readOnly = true)
    public PageResponse<TimesheetListResponse> getTimesheetsForManager(
            Long managerId, TimesheetStatus status, Pageable pageable) {
        log.debug("Fetching timesheets for manager ID: {} with status: {}", managerId, status);

        Page<Timesheet> timesheetPage = timesheetRepository
                .findTimesheetsByManagerIdAndStatus(managerId, status, pageable);

        return pageMapper.toPageResponse(timesheetPage, timesheetMapper::toListResponse);
    }


    /**
     * Retrieves the number of pending timesheets awaiting the manager's review.
     *
     * @param managerId the manager’s employee ID
     * @return the count of pending timesheets
     */
    @Transactional(readOnly = true)
    public long getPendingTimesheetsCount(Long managerId) {
        return timesheetRepository.countPendingTimesheetsByManagerId(managerId);
    }

    /**
     * Approves a timesheet on behalf of a manager.
     *
     * @param timesheetId      the ID of the timesheet being approved
     * @param reviewerUsername the username of the manager reviewing the timesheet
     * @return a {@link MessageResponse} confirming approval
     * @throws ResourceNotFoundException if the timesheet or reviewer does not exist
     * @throws BadRequestException       if the user is not the manager or the timesheet cannot be reviewed
     */
    @Transactional
    public MessageResponse approveTimesheet(Long timesheetId, String reviewerUsername) {
        log.info("Approving timesheet ID: {} by reviewer: {}", timesheetId, reviewerUsername);

        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Timesheet not found with ID: " + timesheetId));

        Employee reviewer = employeeRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        // Check if can be reviewed
        if (!timesheet.getStatus().canBeReviewed()) {
            throw new BadRequestException(
                    "Timesheet cannot be reviewed. Status: " + timesheet.getStatus());
        }

        // Verify reviewer is the manager
        if (timesheet.getEmployee().getManager() == null ||
                !timesheet.getEmployee().getManager().getId().equals(reviewer.getId())) {
            throw new BadRequestException("You can only approve timesheets of your direct reports");
        }

        timesheet.approve(reviewer);
        timesheetRepository.save(timesheet);

        log.info("Timesheet approved. ID: {}", timesheetId);

        return new MessageResponse("Timesheet approved successfully");
    }

    /**
     * Denies a timesheet and records the reason.
     *
     * @param timesheetId      the ID of the timesheet being denied
     * @param reviewerUsername the username of the manager denying the timesheet
     * @param request          contains the denial reason
     * @return a {@link MessageResponse} confirming the denial
     * @throws ResourceNotFoundException if the timesheet or reviewer does not exist
     * @throws BadRequestException       if the user is not the manager or the timesheet cannot be reviewed
     */
    @Transactional
    public MessageResponse denyTimesheet(Long timesheetId, String reviewerUsername,
                                         DenyTimesheetRequest request) {
        log.info("Denying timesheet ID: {} by reviewer: {}", timesheetId, reviewerUsername);

        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Timesheet not found with ID: " + timesheetId));

        Employee reviewer = employeeRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        // Check if can be reviewed
        if (!timesheet.getStatus().canBeReviewed()) {
            throw new BadRequestException(
                    "Timesheet cannot be reviewed. Status: " + timesheet.getStatus());
        }

        // Verify reviewer is the manager
        if (timesheet.getEmployee().getManager() == null ||
                !timesheet.getEmployee().getManager().getId().equals(reviewer.getId())) {
            throw new BadRequestException("You can only deny timesheets of your direct reports");
        }

        timesheet.deny(reviewer, request.getReason());
        timesheetRepository.save(timesheet);

        log.info("Timesheet denied. ID: {}, Reason: {}", timesheetId, request.getReason());

        return new MessageResponse("Timesheet denied");
    }


    /**
     * Retrieves all approved timesheets for the specified payroll week.
     *
     * @param weekStartDate the Monday date representing the start of the payroll week
     * @return a list of approved {@link Timesheet} objects
     */
    @Transactional(readOnly = true)
    public List<Timesheet> getApprovedTimesheetsForWeek(LocalDate weekStartDate) {
        log.debug("Fetching approved timesheets for week starting: {}", weekStartDate);

        return timesheetRepository.findApprovedTimesheetsByWeek(weekStartDate);
    }

    /**
     * Creates a new timesheet for the given employee and weekly period.
     *
     * @param employee      the employee for whom to create the timesheet
     * @param weekStartDate the Monday date of the week
     * @return the newly created {@link Timesheet}
     */
    private Timesheet createNewTimesheet(Employee employee, LocalDate weekStartDate) {
        log.debug("Creating new timesheet for employee: {} for week: {}",
                employee.getUsername(), weekStartDate);

        LocalDate weekEndDate = weekStartDate.plusDays(6); // Sunday

        Timesheet timesheet = Timesheet.builder()
                .employee(employee)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .status(TimesheetStatus.DRAFT)
                .build();

        return timesheetRepository.save(timesheet);
    }

    /**
     * Calculates the Monday of the current week.
     *
     * @return the {@link LocalDate} representing the start of the current week
     */
    private LocalDate getCurrentWeekStartDate() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY);
    }
}
