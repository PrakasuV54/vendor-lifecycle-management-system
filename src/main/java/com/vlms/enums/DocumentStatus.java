package com.vlms.enums;

/**
 * Represents the lifecycle status of a vendor document.
 */
public enum DocumentStatus {
    PENDING("Pending - Awaiting verification"),
    VERIFIED("Verified - Approved by reviewer"),
    REJECTED("Rejected - Failed verification"),
    EXPIRED("Expired - Past validity date");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
