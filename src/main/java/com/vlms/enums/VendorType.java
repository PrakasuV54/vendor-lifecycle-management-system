package com.vlms.enums;

/**
 * Represents the type of a vendor for factory-based creation.
 */
public enum VendorType {
    SUPPLIER("Supplier - Provides goods and materials"),
    CONTRACTOR("Contractor - Provides skilled project-based labor"),
    SERVICE_PROVIDER("Service Provider - Provides ongoing services");

    private final String description;

    VendorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
