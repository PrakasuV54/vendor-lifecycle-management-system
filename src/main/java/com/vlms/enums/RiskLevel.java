package com.vlms.enums;

/**
 * Risk classification for a vendor.
 */
public enum RiskLevel {
    LOW("Low Risk - Minimal concern"),
    MEDIUM("Medium Risk - Requires monitoring"),
    HIGH("High Risk - Immediate attention needed");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
