package com.vlms.strategy;

import com.vlms.interfaces.EvaluationStrategy;
import com.vlms.model.PerformanceEvaluation;
import com.vlms.model.Vendor;

/**
 * Lenient evaluation strategy: applies a bonus for consistently high scores
 * across all dimensions, encouraging well-rounded vendor performance.
 */
public class LenientEvaluationStrategy implements EvaluationStrategy {

    private static final double EXCELLENCE_THRESHOLD = 8.0;
    private static final double BONUS_POINTS = 0.5;
    private static final double MAX_RATING = 10.0;

    @Override
    public PerformanceEvaluation evaluate(Vendor vendor, double qualityScore,
                                          double deliveryScore, double complianceScore) {
        double simpleAverage = (qualityScore + deliveryScore + complianceScore) / 3.0;

        // Apply bonus if all scores exceed excellence threshold
        boolean allExcellent = qualityScore >= EXCELLENCE_THRESHOLD
                && deliveryScore >= EXCELLENCE_THRESHOLD
                && complianceScore >= EXCELLENCE_THRESHOLD;

        double overallRating = allExcellent
                ? Math.min(simpleAverage + BONUS_POINTS, MAX_RATING)
                : simpleAverage;

        overallRating = Math.round(overallRating * 100.0) / 100.0;

        String remarks = buildRemarks(allExcellent, overallRating);

        return new PerformanceEvaluation(
                vendor.getVendorId(),
                qualityScore, deliveryScore, complianceScore,
                overallRating, getStrategyName(), remarks, "SYSTEM"
        );
    }

    private String buildRemarks(boolean allExcellent, double rating) {
        if (allExcellent) {
            return String.format("Excellence bonus of %.1f applied. Outstanding performance across all dimensions.", BONUS_POINTS);
        }
        if (rating >= 7.0) return "Good performance. Encourage consistency to qualify for excellence bonus.";
        if (rating >= 5.0) return "Satisfactory performance. Room for improvement in some areas.";
        return "Below expectations. Immediate improvement required.";
    }

    @Override
    public String getStrategyName() {
        return "Lenient Bonus Evaluation";
    }
}
