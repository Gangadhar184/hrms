package com.example.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "payroll",
        indexes = {
                @Index(name = "idx_payroll_status", columnList = "status"),
                @Index(name = "idx_payroll_period", columnList = "pay_period_start, pay_period_end"),
                @Index(name = "idx_payroll_employee", columnList = "employee_id")
        }
)

public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @Column(name = "gross_pay", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossPay;

    @Column(name = "net_pay", nullable = false, precision = 15, scale = 2)
    private BigDecimal netPay;

    @Column(name = "tax_deduction", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxDeduction = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PayrollStatus status = PayrollStatus.PREVIEW;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Employee processedBy;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public void calculateNetPay() {
        this.netPay = grossPay
                .add(bonus)
                .subtract(taxDeduction)
                .subtract(otherDeductions);
    }

    public void process(Employee processor, LocalDate paymentDate) {
        this.status = PayrollStatus.PROCESSED;
        this.processedBy = processor;
        this.processedAt = LocalDateTime.now();
        this.paymentDate = paymentDate;
    }

    public void markAsPaid() {
        this.status = PayrollStatus.PAID;
    }

    @Override
    public String toString() {
        return "Payroll{" +
                "id=" + id +
                ", payPeriodStart=" + payPeriodStart +
                ", payPeriodEnd=" + payPeriodEnd +
                ", grossPay=" + grossPay +
                ", netPay=" + netPay +
                ", status=" + status +
                '}';
    }
}