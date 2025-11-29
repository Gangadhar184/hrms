package com.example.hrms.models;

import lombok.Getter;

@Getter
public enum PayFrequency {
    WEEKLY("Weekly", 52),
    BI_WEEKLY("Bi-Weekly", 26),
    SEMI_MONTHLY("Semi-Monthly", 24),
    MONTHLY("Monthly", 12);

    private final String displayName;
    private final int periodsPerYear;

    PayFrequency(String displayName, int periodsPerYear) {
        this.displayName = displayName;
        this.periodsPerYear = periodsPerYear;
    }

}
