package com.example.hrms.models;

public enum Role {
    EMPLOYEE("Regular Employee"),
    MANAGER("Manager"),
    ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

