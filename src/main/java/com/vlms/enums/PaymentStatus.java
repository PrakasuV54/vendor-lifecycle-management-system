package com.vlms.enums;

/**
 * Represents the status of a payment transaction.
 */
public enum PaymentStatus {
    PENDING("Pending - Awaiting approval"),
    APPROVED("Approved - Cleared for disbursement"),
    PAID("Paid - Successfully transferred"),
    FAILED("Failed - Transaction unsuccessful");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
