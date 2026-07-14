package com.vlms.enums;

/**
 * Represents the status of a vendor invoice.
 */
public enum InvoiceStatus {
    SUBMITTED("Submitted - Invoice received, awaiting review"),
    APPROVED("Approved - Invoice cleared for payment"),
    REJECTED("Rejected - Invoice denied");

    private final String description;

    InvoiceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
