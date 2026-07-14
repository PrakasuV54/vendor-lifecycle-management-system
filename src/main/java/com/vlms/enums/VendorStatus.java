package com.vlms.enums;

/**
 * Represents the complete lifecycle states of a vendor.
 * Transitions are enforced by the State Pattern in the state package.
 */
public enum VendorStatus {
    PENDING("Pending - Awaiting initial review"),
    UNDER_VERIFICATION("Under Verification - Documents being checked"),
    VERIFIED("Verified - Documents confirmed"),
    APPROVED("Approved - Ready for activation"),
    ACTIVE("Active - Currently engaged"),
    SUSPENDED("Suspended - Temporarily halted"),
    BLACKLISTED("Blacklisted - Flagged for serious violations"),
    TERMINATED("Terminated - Permanently removed");

    private final String description;

    VendorStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
