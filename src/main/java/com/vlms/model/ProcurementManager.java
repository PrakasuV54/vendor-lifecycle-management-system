package com.vlms.model;

import com.vlms.enums.UserRole;

/**
 * Procurement Manager responsible for vendor onboarding and contract management.
 */
public class ProcurementManager extends User {

    private final String department;

    public ProcurementManager(String name, String email, String department) {
        super(name, email, UserRole.PROCUREMENT_MANAGER);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public String getRoleDescription() {
        return "Procurement Manager [" + department + "] - Manages vendor registration, document verification, contract creation, and vendor lifecycle transitions.";
    }

    @Override
    public String toString() {
        return "ProcurementManager[" + getUserId() + " | " + getName() + " | " + department + "]";
    }
}
