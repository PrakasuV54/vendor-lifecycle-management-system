package com.vlms.rules;

import com.vlms.model.Invoice;

/**
 * Business rules related to Invoice processing.
 */
public class InvoiceRules {

    private static final double DUAL_APPROVAL_THRESHOLD = 500000.0; // ₹5,00,000

    public static boolean requiresDualApproval(Invoice invoice) {
        return invoice.getAmount() > DUAL_APPROVAL_THRESHOLD;
    }

    public static boolean requiresDualApproval(double amount) {
        return amount > DUAL_APPROVAL_THRESHOLD;
    }
}
