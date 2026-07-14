package com.vlms.rules;

import com.vlms.enums.UserRole;
import com.vlms.model.User;

/**
 * Business rules related to Approval tasks and delegation.
 */
public class ApprovalRules {

    public static boolean canApproveVendor(User user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.PROCUREMENT_MANAGER;
    }

    public static boolean canApproveInvoice(User user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.FINANCE_MANAGER;
    }

    public static boolean canProcessPayment(User user) {
        return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.FINANCE_MANAGER;
    }
}
