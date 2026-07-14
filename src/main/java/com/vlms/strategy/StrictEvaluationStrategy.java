package com.vlms.strategy;

import com.vlms.interfaces.EvaluationStrategy;
import com.vlms.model.PerformanceEvaluation;
import com.vlms.model.Vendor;

/**
 * Strict evaluation strategy: any score below the minimum threshold
 * caps the overall rating regardless of other scores.
 * Used for high-stakes or regulated vendors.
 */
public class StrictEvaluationStrategy implements EvaluationStrategy {

    private static final double MINIMUM_ACCEPTABLE_SCORE = 6.0;
    private static final double PENALTY_FACTOR = 0.70; // 30% penalty if any score is below minimum

    @Override
    public PerformanceEvaluation evaluate(Vendor vendor, double qualityScore,
                                          double deliveryScore, double complianceScore) {
        double rawAverage = (qualityScore + deliveryScore + complianceScore) / 3.0;

        boolean anyBelowMinimum = qualityScore < MINIMUM_ACCEPTABLE_SCORE
                || deliveryScore < MINIMUM_ACCEPTABLE_SCORE
                || complianceScore < MINIMUM_ACCEPTABLE_SCORE;

        double overallRating = anyBelowMinimum
                ? Math.min(rawAverage * PENALTY_FACTOR, rawAverage)
                : rawAverage;

        overallRating = Math.round(overallRating * 100.0) / 100.0;

        String remarks = buildRemarks(qualityScore, deliveryScore, complianceScore, anyBelowMinimum);

        return new PerformanceEvaluation(
                vendor.getVendorId(),
                qualityScore, deliveryScore, complianceScore,
                overallRating, getStrategyName(), remarks, "SYSTEM"
        );
    }

    private String buildRemarks(double quality, double delivery, double compliance, boolean penalised) {
        StringBuilder sb = new StringBuilder();
        if (penalised) {
            sb.append("PENALTY APPLIED: One or more scores fell below the minimum threshold of ")
              .append(MINIMUM_ACCEPTABLE_SCORE).append(". ");
        }
        if (quality < MINIMUM_ACCEPTABLE_SCORE) sb.append("Quality below minimum. ");
        if (delivery < MINIMUM_ACCEPTABLE_SCORE) sb.append("Delivery below minimum. ");
        if (compliance < MINIMUM_ACCEPTABLE_SCORE) sb.append("Compliance below minimum. ");
        if (!penalised) sb.append("All scores meet strict compliance requirements.");
        return sb.toString().trim();
    }

    @Override
    public String getStrategyName() {
        return "Strict Compliance Evaluation";
    }
}
