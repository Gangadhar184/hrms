package com.example.hrms.repositories;

import com.example.hrms.models.Timesheet;
import com.example.hrms.models.TimesheetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    Optional<Timesheet> findByEmployeeIdAndWeekStartDate(Long employeeId, LocalDate weekStartDate);

    List<Timesheet> findByEmployeeIdOrderByWeekStartDateDesc(Long employeeId);

    //find current/latest timesheet for an employee
    @Query("SELECT t FROM Timesheet t WHERE t.employee.id = :employeeId " + "ORDER BY t.weekStartDate DESC LIMIT 1")
    Optional<Timesheet> findCurrentTimesheetByEmployeeId(@Param("employeeId") Long employeeId);

    //find timesheets by status
    List<Timesheet> findByStatus(TimesheetStatus status);

    //find timesheets by stauts with pagination
    Page<Timesheet> findByStatus(TimesheetStatus status, Pageable pageable);

    //find timesheets for employees reporting to a manager
    @Query("SELECT t FROM Timesheet t WHERE t.employee.manager.id = :managerId " +
            "ORDER BY t.weekStartDate DESC")
    List<Timesheet> findTimesheetsByManagerId(@Param("managerId") Long managerId);


     // Find submitted timesheets for manager's direct reports (paginated)

    @Query("SELECT t FROM Timesheet t WHERE t.employee.manager.id = :managerId " +
            "AND t.status = :status ORDER BY t.submittedAt ASC")
    Page<Timesheet> findTimesheetsByManagerIdAndStatus(
            @Param("managerId") Long managerId,
            @Param("status") TimesheetStatus status,
            Pageable pageable);


     // Find timesheets by date range

    @Query("SELECT t FROM Timesheet t WHERE t.weekStartDate >= :startDate " +
            "AND t.weekEndDate <= :endDate")
    List<Timesheet> findByDateRange(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);


     // Find approved timesheets for payroll processing

    @Query("SELECT t FROM Timesheet t WHERE t.status = 'APPROVED' " +
            "AND t.weekStartDate = :weekStartDate")
    List<Timesheet> findApprovedTimesheetsByWeek(@Param("weekStartDate") LocalDate weekStartDate);


     // Count timesheets by status for an employee

    long countByEmployeeIdAndStatus(Long employeeId, TimesheetStatus status);


     // Count pending timesheets for manager

    @Query("SELECT COUNT(t) FROM Timesheet t WHERE t.employee.manager.id = :managerId " +
            "AND t.status = 'SUBMITTED'")
    long countPendingTimesheetsByManagerId(@Param("managerId") Long managerId);


     // Check if timesheet exists for employee and week

    boolean existsByEmployeeIdAndWeekStartDate(Long employeeId, LocalDate weekStartDate);


     // Find timesheets that need attention (denied or draft)

    @Query("SELECT t FROM Timesheet t WHERE t.employee.id = :employeeId " +
            "AND t.status IN ('DENIED', 'DRAFT') ORDER BY t.weekStartDate DESC")
    List<Timesheet> findTimesheetsNeedingAttention(@Param("employeeId") Long employeeId);


}
