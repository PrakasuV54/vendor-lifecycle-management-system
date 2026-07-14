package com.vlms.strategy;

import com.vlms.interfaces.EvaluationStrategy;
import com.vlms.model.PerformanceEvaluation;
import com.vlms.model.Vendor;

/**
 * Standard evaluation strategy using a balanced weighted average.
 * Weights: Quality 40%, Delivery 35%, Compliance 25%.
 */
public class StandardEvaluationStrategy implements EvaluationStrategy {

    private static final double QUALITY_WEIGHT = 0.40;
    private static final double DELIVERY_WEIGHT = 0.35;
    private static final double COMPLIANCE_WEIGHT = 0.25;

    @Override
    public PerformanceEvaluation evaluate(Vendor vendor, double qualityScore,
                                          double deliveryScore, double complianceScore) {
        double overallRating = (qualityScore * QUALITY_WEIGHT)
                + (deliveryScore * DELIVERY_WEIGHT)
                + (complianceScore * COMPLIANCE_WEIGHT);

        overallRating = Math.round(overallRating * 100.0) / 100.0;

        String remarks = buildRemarks(overallRating, qualityScore, deliveryScore, complianceScore);

        return new PerformanceEvaluation(
                vendor.getVendorId(),
                qualityScore, deliveryScore, complianceScore,
                overallRating, getStrategyName(), remarks, "SYSTEM"
        );
    }

    private String buildRemarks(double overall, double quality, double delivery, double compliance) {
        StringBuilder sb = new StringBuilder();
        if (quality < 5.0) sb.append("Quality needs improvement. ");
        if (delivery < 5.0) sb.append("Delivery performance is below par. ");
        if (compliance < 5.0) sb.append("Compliance issues detected. ");
        if (overall >= 8.0) sb.append("Excellent overall performance.");
        else if (overall >= 6.0) sb.append("Satisfactory overall performance.");
        else sb.append("Performance requires corrective action.");
        return sb.toString().trim();
    }

    @Override
    public String getStrategyName() {
        return "Standard Weighted Evaluation";
    }
}
