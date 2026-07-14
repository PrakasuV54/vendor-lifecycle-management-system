package com.vlms.rules;

import com.vlms.model.Contract;

/**
 * Business rules related to Contract approvals and renewals.
 */
public class ContractRules {

    private static final double FINANCE_APPROVAL_THRESHOLD = 1000000.0; // ₹10,00,000

    public static boolean requiresFinanceApproval(Contract contract) {
        return contract.getContractValue() > FINANCE_APPROVAL_THRESHOLD;
    }

    public static boolean requiresFinanceApproval(double contractValue) {
        return contractValue > FINANCE_APPROVAL_THRESHOLD;
    }

    public static boolean canBeRenewed(Contract contract) {
        return contract.isActive() || contract.getStatus() == com.vlms.enums.ContractStatus.RENEWED;
    }
}
