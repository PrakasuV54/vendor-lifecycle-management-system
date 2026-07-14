package com.vlms.rules;

import com.vlms.model.Vendor;

/**
 * Business rules related to Vendor risk and compliance review triggers.
 */
public class RiskRules {

    private static final double COMPLIANCE_REVIEW_THRESHOLD = 80.0;

    public static boolean requiresComplianceReview(double riskScore) {
        return riskScore > COMPLIANCE_REVIEW_THRESHOLD;
    }

    public static boolean requiresComplianceReview(Vendor vendor) {
        return vendor.getLatestRiskAssessment()
                .map(r -> r.getRiskScore() > COMPLIANCE_REVIEW_THRESHOLD)
                .orElse(false);
    }
}
