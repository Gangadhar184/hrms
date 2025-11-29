package com.example.hrms.repositories;

import com.example.hrms.models.Payroll;
import com.example.hrms.models.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {


     // Find payroll by employee and pay period

    Optional<Payroll> findByEmployeeIdAndPayPeriodStartAndPayPeriodEnd(
            Long employeeId, LocalDate payPeriodStart, LocalDate payPeriodEnd);


     // Find all payrolls for an employee

    List<Payroll> findByEmployeeIdOrderByPayPeriodStartDesc(Long employeeId);

    /**
     * Find payrolls by status
     */
    List<Payroll> findByStatus(PayrollStatus status);


     // Find payrolls by pay period

    List<Payroll> findByPayPeriodStartAndPayPeriodEnd(
            LocalDate payPeriodStart, LocalDate payPeriodEnd);


     // Find preview payrolls for a specific week

    @Query("SELECT p FROM Payroll p WHERE p.status = 'PREVIEW' " +
            "AND p.payPeriodStart = :weekStartDate")
    List<Payroll> findPreviewPayrollsByWeek(@Param("weekStartDate") LocalDate weekStartDate);


     // Find processed payrolls by date range

    @Query("SELECT p FROM Payroll p WHERE p.status IN ('PROCESSED', 'PAID') " +
            "AND p.payPeriodStart >= :startDate AND p.payPeriodEnd <= :endDate")
    List<Payroll> findProcessedPayrollsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


     // Check if payroll exists for employee and period

    boolean existsByEmployeeIdAndPayPeriodStartAndPayPeriodEnd(
            Long employeeId, LocalDate payPeriodStart, LocalDate payPeriodEnd);


     // Calculate total payroll amount for a period

    @Query("SELECT SUM(p.netPay) FROM Payroll p WHERE p.payPeriodStart = :startDate " +
            "AND p.payPeriodEnd = :endDate AND p.status = :status")
    Double calculateTotalPayrollByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") PayrollStatus status);


     //Find latest payroll for employee

    @Query("SELECT p FROM Payroll p WHERE p.employee.id = :employeeId " +
            "ORDER BY p.payPeriodEnd DESC LIMIT 1")
    Optional<Payroll> findLatestPayrollByEmployeeId(@Param("employeeId") Long employeeId);


     // Count payrolls by status

    long countByStatus(PayrollStatus status);


     // Delete preview payrolls
    void deleteByStatus(PayrollStatus status);

}
