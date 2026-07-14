package com.vlms.model;

import com.vlms.enums.UserRole;

/**
 * Finance Manager responsible for invoice and payment processing.
 */
public class FinanceManager extends User {

    private final String costCenter;

    public FinanceManager(String name, String email, String costCenter) {
        super(name, email, UserRole.FINANCE_MANAGER);
        this.costCenter = costCenter;
    }

    public String getCostCenter() {
        return costCenter;
    }

    @Override
    public String getRoleDescription() {
        return "Finance Manager [Cost Center: " + costCenter + "] - Manages invoice approval, payment processing, and financial compliance for vendor transactions.";
    }

    @Override
    public String toString() {
        return "FinanceManager[" + getUserId() + " | " + getName() + " | CC:" + costCenter + "]";
    }
}
