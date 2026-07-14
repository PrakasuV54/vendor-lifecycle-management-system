package com.vlms.enums;

/**
 * Roles for system users that control access to features.
 */
public enum UserRole {
    ADMIN("Admin - Full system access"),
    PROCUREMENT_MANAGER("Procurement Manager - Vendor and contract management"),
    FINANCE_MANAGER("Finance Manager - Invoice and payment management");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
