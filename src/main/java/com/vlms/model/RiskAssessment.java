package com.vlms.model;

import com.vlms.enums.RiskLevel;
import com.vlms.util.IdGenerator;

import java.time.LocalDateTime;

/**
 * Represents a risk assessment record for a vendor.
 * Demonstrates Composition: RiskAssessment is composed inside Vendor.
 */
public class RiskAssessment {

    private final String assessmentId;
    private final String vendorId;
    private RiskLevel riskLevel;
    private final String rationale;
    private final double riskScore;    // 0.0 - 100.0
    private final LocalDateTime assessedAt;
    private final String assessedByUserId;

    public RiskAssessment(String vendorId, RiskLevel riskLevel, String rationale,
                          double riskScore, String assessedByUserId) {
        this.assessmentId = IdGenerator.generateUUID();
        this.vendorId = vendorId;
        this.riskLevel = riskLevel;
        this.rationale = rationale;
        this.riskScore = riskScore;
        this.assessedByUserId = assessedByUserId;
        this.assessedAt = LocalDateTime.now();
    }

    // --- Getters ---

    public String getAssessmentId() { return assessmentId; }
    public String getVendorId() { return vendorId; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public String getRationale() { return rationale; }
    public double getRiskScore() { return riskScore; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
    public String getAssessedByUserId() { return assessedByUserId; }

    @Override
    public String toString() {
        return String.format("RiskAssessment[Vendor: %s | Level: %s | Score: %.1f | Reason: %s]",
                vendorId, riskLevel, riskScore, rationale);
    }
}
