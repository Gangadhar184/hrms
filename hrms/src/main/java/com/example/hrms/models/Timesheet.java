package com.example.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timesheets",
        indexes = {
                @Index(name = "idx_timesheet_status", columnList = "status"),
                @Index(name = "idx_timesheet_employee", columnList = "employee_id"),
                @Index(name = "idx_timesheet_week", columnList = "week_start_date, week_end_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_employee_week", columnNames = {"employee_id", "week_start_date"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "total_hours", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Employee reviewedBy;

    @Column(name = "denial_reason", columnDefinition = "TEXT")
    private String denialReason;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TimesheetEntry> entries = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public void addEntry(TimesheetEntry entry) {
        entries.add(entry);
        entry.setTimesheet(this);
        calculateTotalHours();
    }

    public void removeEntry(TimesheetEntry entry) {
        entries.remove(entry);
        entry.setTimesheet(null);
        calculateTotalHours();
    }

    public void calculateTotalHours() {
        this.totalHours = entries.stream()
                .map(TimesheetEntry::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void submit() {
        this.status = TimesheetStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void approve(Employee reviewer) {
        this.status = TimesheetStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.denialReason = null;
    }

    public void deny(Employee reviewer, String reason) {
        this.status = TimesheetStatus.DENIED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.denialReason = reason;
    }

    @Override
    public String toString() {
        return "Timesheet{" +
                "id=" + id +
                ", weekStartDate=" + weekStartDate +
                ", weekEndDate=" + weekEndDate +
                ", totalHours=" + totalHours +
                ", status=" + status +
                '}';
    }
}