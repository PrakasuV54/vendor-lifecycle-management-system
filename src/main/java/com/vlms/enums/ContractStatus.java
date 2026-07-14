package com.vlms.enums;

/**
 * Represents the status of a vendor contract.
 */
public enum ContractStatus {
    PENDING("Pending - Awaiting reviews and approvals"),
    ACTIVE("Active - Contract is currently in force"),
    EXPIRED("Expired - Contract period has ended"),
    CANCELLED("Cancelled - Contract was terminated early"),
    RENEWED("Renewed - Contract has been extended");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
