package com.example.hrms.models;

import lombok.Getter;

@Getter
public enum TimesheetStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    APPROVED("Approved"),
    DENIED("Denied");

    private final String displayName;

    TimesheetStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean canBeEdited() {
        return this == DRAFT || this == DENIED;
    }

    public boolean canBeSubmitted() {
        return this == DRAFT || this == DENIED;
    }

    public boolean canBeReviewed() {
        return this == SUBMITTED;
    }
}
