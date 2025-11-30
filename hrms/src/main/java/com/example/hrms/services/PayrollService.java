package com.example.hrms.services;

import com.example.hrms.dto.PayrollPreviewResponse;
import com.example.hrms.dto.PayrollResponse;
import com.example.hrms.dto.RunPayrollRequest;
import com.example.hrms.dto.RunPayrollResponse;
import com.example.hrms.exceptions.BadRequestException;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.mappers.PayrollMapper;
import com.example.hrms.models.*;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.PayInfoRepository;
import com.example.hrms.repositories.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {
    private final PayrollRepository payrollRepository;
    private final PayInfoRepository payInfoRepository;
    private final EmployeeRepository employeeRepository;
    private final TimesheetService timesheetService;
    private final PayrollMapper payrollMapper;

    // Tax rates
    private static final BigDecimal TAX_RATE = new BigDecimal("0.20"); // 20%
    private static final BigDecimal OTHER_DEDUCTIONS_RATE = new BigDecimal("0.05"); // 5%

    /**
     * Generates a payroll preview for a specific week.
     * <p>
     * Only approved timesheets are included. No data is saved to the database.
     * </p>
     *
     * @param weekStartDate the Monday date representing the start of the payroll week
     * @return a {@link PayrollPreviewResponse} summarizing payroll calculations
     */
    @Transactional(readOnly = true)
    public PayrollPreviewResponse previewPayroll(LocalDate weekStartDate) {
        log.info("Previewing payroll for week starting: {}", weekStartDate);

        LocalDate weekEndDate = weekStartDate.plusDays(6);

        // Get approved timesheets for the week
        List<Timesheet> approvedTimesheets =
                timesheetService.getApprovedTimesheetsForWeek(weekStartDate);

        if (approvedTimesheets.isEmpty()) {
            log.warn("No approved timesheets found for week: {}", weekStartDate);
            return payrollMapper.toPreviewResponse(List.of(), weekStartDate, weekEndDate);
        }

        // Calculate payroll for each employee
        List<Payroll> previewPayrolls = approvedTimesheets.stream()
                .map(timesheet -> calculatePayroll(timesheet, weekStartDate, weekEndDate))
                .collect(Collectors.toList());

        return payrollMapper.toPreviewResponse(previewPayrolls, weekStartDate, weekEndDate);
    }

    /**
     * Runs and finalizes payroll for a specific week.
     * <p>
     * This will:
     * <ul>
     *     <li>Validate approved timesheets</li>
     *     <li>Ensure payroll has not already been processed</li>
     *     <li>Remove previous preview payrolls</li>
     *     <li>Create and save processed payroll records</li>
     * </ul>
     * </p>
     *
     * @param request            contains payroll period and payment date
     * @param processorUsername  username of the employee running payroll
     * @return a {@link RunPayrollResponse} with summary details
     * @throws BadRequestException if payroll is already processed or no approved timesheets exist
     * @throws ResourceNotFoundException if processor user is not found
     */
    @Transactional
    public RunPayrollResponse runPayroll(RunPayrollRequest request, String processorUsername) {
        log.info("Running payroll for week starting: {} by user: {}",
                request.getWeekStartDate(), processorUsername);

        LocalDate weekStartDate = request.getWeekStartDate();
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        Employee processor = employeeRepository.findByUsername(processorUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Processor not found"));

        // Get approved timesheets
        List<Timesheet> approvedTimesheets =
                timesheetService.getApprovedTimesheetsForWeek(weekStartDate);

        if (approvedTimesheets.isEmpty()) {
            throw new BadRequestException("No approved timesheets found for the specified week");
        }

        // Check if payroll already processed for this period
        List<Payroll> existingPayrolls = payrollRepository
                .findByPayPeriodStartAndPayPeriodEnd(weekStartDate, weekEndDate);

        if (!existingPayrolls.isEmpty() &&
                existingPayrolls.stream().anyMatch(p -> p.getStatus() == PayrollStatus.PROCESSED)) {
            throw new BadRequestException("Payroll already processed for this period");
        }

        // Delete preview payrolls if any
        payrollRepository.deleteByStatus(PayrollStatus.PREVIEW);

        // Process payroll for each timesheet
        List<Payroll> processedPayrolls = approvedTimesheets.stream()
                .map(timesheet -> {
                    Payroll payroll = calculatePayroll(timesheet, weekStartDate, weekEndDate);
                    payroll.setStatus(PayrollStatus.PROCESSED);
                    payroll.process(processor, request.getPaymentDate());
                    return payrollRepository.save(payroll);
                })
                .collect(Collectors.toList());

        BigDecimal totalAmount = processedPayrolls.stream()
                .map(Payroll::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Payroll processed successfully. Count: {}, Total: {}",
                processedPayrolls.size(), totalAmount);

        return RunPayrollResponse.builder()
                .message("Payroll processed successfully")
                .processedCount(processedPayrolls.size())
                .totalAmount(totalAmount)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Retrieves payroll history for a specific employee.
     *
     * @param username the username of the employee
     * @return a list of {@link PayrollResponse} ordered by pay period start date
     * @throws ResourceNotFoundException if the employee does not exist
     */
    @Transactional(readOnly = true)
    public List<PayrollResponse> getEmployeePayrollHistory(String username) {
        log.debug("Fetching payroll history for user: {}", username);

        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        List<Payroll> payrolls = payrollRepository
                .findByEmployeeIdOrderByPayPeriodStartDesc(employee.getId());

        return payrollMapper.toResponseList(payrolls);
    }

    /**
     * Retrieves payroll details by payroll ID.
     *
     * @param payrollId the payroll record ID
     * @return a {@link PayrollResponse}
     * @throws ResourceNotFoundException if the payroll record does not exist
     */
    @Transactional(readOnly = true)
    public PayrollResponse getPayrollById(Long payrollId) {
        log.debug("Fetching payroll by ID: {}", payrollId);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payroll not found with ID: " + payrollId));

        return payrollMapper.toResponse(payroll);
    }

    /**
     * Retrieves all processed payrolls within a date range.
     *
     * @param startDate inclusive start date
     * @param endDate   inclusive end date
     * @return list of {@link PayrollResponse}
     */
    @Transactional(readOnly = true)
    public List<PayrollResponse> getPayrollsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching payrolls from {} to {}", startDate, endDate);

        List<Payroll> payrolls = payrollRepository
                .findProcessedPayrollsByDateRange(startDate, endDate);

        return payrollMapper.toResponseList(payrolls);
    }

    /**
     * Calculates a payroll instance based on the given timesheet and pay period.
     * <p>
     * Includes:
     * <ul>
     *     <li>Gross pay calculation</li>
     *     <li>20% tax deduction</li>
     *     <li>5% additional deductions</li>
     *     <li>Net pay calculation</li>
     * </ul>
     * Returned object is not saved to the database.
     * </p>
     *
     * @param timesheet the approved timesheet containing hours worked
     * @param periodStart start of the pay period
     * @param periodEnd   end of the pay period
     * @return a populated {@link Payroll} object with status PREVIEW
     */
    private Payroll calculatePayroll(Timesheet timesheet, LocalDate periodStart, LocalDate periodEnd) {
        Employee employee = timesheet.getEmployee();

        PayInfo payInfo = payInfoRepository.findByEmployeeId(employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pay information not found for employee: " + employee.getUsername()));

        // Calculate gross pay based on hours worked
        BigDecimal hoursWorked = timesheet.getTotalHours();
        BigDecimal hourlyRate = payInfo.getHourlyRate() != null ?
                payInfo.getHourlyRate() : calculateHourlyRateFromSalary(payInfo);

        BigDecimal grossPay = hoursWorked.multiply(hourlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate deductions
        BigDecimal taxDeduction = grossPay.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal otherDeductions = grossPay.multiply(OTHER_DEDUCTIONS_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate net pay
        BigDecimal netPay = grossPay
                .subtract(taxDeduction)
                .subtract(otherDeductions)
                .setScale(2, RoundingMode.HALF_UP);

        return Payroll.builder()
                .employee(employee)
                .payPeriodStart(periodStart)
                .payPeriodEnd(periodEnd)
                .grossPay(grossPay)
                .taxDeduction(taxDeduction)
                .otherDeductions(otherDeductions)
                .bonus(BigDecimal.ZERO)
                .netPay(netPay)
                .status(PayrollStatus.PREVIEW)
                .build();
    }

    /**
     * Converts annual salary to an hourly rate.
     * <p>
     * Uses fixed assumptions:
     * <ul>
     *     <li>52 working weeks / year</li>
     *     <li>40 working hours / week</li>
     * </ul>
     * </p>
     *
     * @param payInfo contains annual salary
     * @return calculated hourly rate
     * @throws IllegalStateException if salary is not defined
     */
    private BigDecimal calculateHourlyRateFromSalary(PayInfo payInfo) {
        if (payInfo.getSalary() == null) {
            throw new IllegalStateException("Neither hourly rate nor salary is defined");
        }

        // Annual salary / 52 weeks / 40 hours
        return payInfo.getSalary()
                .divide(new BigDecimal("52"), 4, RoundingMode.HALF_UP)
                .divide(new BigDecimal("40"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Marks a processed payroll record as fully paid.
     *
     * @param payrollId the payroll record ID
     * @throws ResourceNotFoundException if the payroll record does not exist
     * @throws BadRequestException       if payroll has not been processed yet
     */
    @Transactional
    public void markPayrollAsPaid(Long payrollId) {
        log.info("Marking payroll as paid. ID: {}", payrollId);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payroll not found with ID: " + payrollId));

        if (payroll.getStatus() != PayrollStatus.PROCESSED) {
            throw new BadRequestException("Only processed payrolls can be marked as paid");
        }

        payroll.markAsPaid();
        payrollRepository.save(payroll);

        log.info("Payroll marked as paid. ID: {}", payrollId);
    }
}
