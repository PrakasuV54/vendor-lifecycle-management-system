package com.vlms.model;

import com.vlms.util.IdGenerator;
import com.vlms.util.ValidationUtil;

import java.time.LocalDateTime;

/**
 * Represents a vendor performance evaluation result.
 * Immutable once created (scores cannot be changed after evaluation).
 * Demonstrates the result of the Strategy Pattern evaluation.
 */
public final class PerformanceEvaluation {

    private final String evaluationId;
    private final String vendorId;
    private final double qualityScore;      // 0.0 - 10.0
    private final double deliveryScore;     // 0.0 - 10.0
    private final double complianceScore;   // 0.0 - 10.0
    private final double overallRating;     // computed by strategy
    private final String strategyUsed;
    private final String remarks;
    private final LocalDateTime evaluatedAt;
    private final String evaluatedByUserId;

    public PerformanceEvaluation(String vendorId, double qualityScore, double deliveryScore,
                                 double complianceScore, double overallRating,
                                 String strategyUsed, String remarks, String evaluatedByUserId) {
        ValidationUtil.requireNonBlank(vendorId, "Vendor ID");
        ValidationUtil.requireInRange(qualityScore, 0, 10, "Quality score");
        ValidationUtil.requireInRange(deliveryScore, 0, 10, "Delivery score");
        ValidationUtil.requireInRange(complianceScore, 0, 10, "Compliance score");

        this.evaluationId = IdGenerator.generateUUID();
        this.vendorId = vendorId;
        this.qualityScore = qualityScore;
        this.deliveryScore = deliveryScore;
        this.complianceScore = complianceScore;
        this.overallRating = overallRating;
        this.strategyUsed = strategyUsed;
        this.remarks = remarks;
        this.evaluatedByUserId = evaluatedByUserId;
        this.evaluatedAt = LocalDateTime.now();
    }

    public String getPerformanceGrade() {
        if (overallRating >= 9.0) return "EXCELLENT";
        if (overallRating >= 7.5) return "GOOD";
        if (overallRating >= 6.0) return "SATISFACTORY";
        if (overallRating >= 4.0) return "NEEDS_IMPROVEMENT";
        return "POOR";
    }

    // --- Getters ---

    public String getEvaluationId() { return evaluationId; }
    public String getVendorId() { return vendorId; }
    public double getQualityScore() { return qualityScore; }
    public double getDeliveryScore() { return deliveryScore; }
    public double getComplianceScore() { return complianceScore; }
    public double getOverallRating() { return overallRating; }
    public String getStrategyUsed() { return strategyUsed; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public String getEvaluatedByUserId() { return evaluatedByUserId; }

    @Override
    public String toString() {
        return String.format("Evaluation[Vendor: %s | Quality: %.1f | Delivery: %.1f | Compliance: %.1f | Overall: %.2f (%s) | Strategy: %s]",
                vendorId, qualityScore, deliveryScore, complianceScore, overallRating, getPerformanceGrade(), strategyUsed);
    }
}
