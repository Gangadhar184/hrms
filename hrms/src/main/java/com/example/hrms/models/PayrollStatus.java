package com.example.hrms.models;

public enum PayrollStatus {
    PREVIEW("Preview"),
    PROCESSED("Processed"),
    PAID("Paid");

    private final String displayName;

    PayrollStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}