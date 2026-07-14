package com.vlms.interfaces;

import com.vlms.model.PerformanceEvaluation;
import com.vlms.model.Vendor;

/**
 * Strategy Pattern interface for vendor performance evaluation algorithms.
 * Open/Closed Principle: new strategies can be added without modifying existing code.
 */
public interface EvaluationStrategy {

    /**
     * Evaluates the vendor and returns a performance evaluation result.
     *
     * @param vendor          the vendor to evaluate
     * @param qualityScore    score for quality (0-10)
     * @param deliveryScore   score for delivery timeliness (0-10)
     * @param complianceScore score for regulatory compliance (0-10)
     * @return a fully computed PerformanceEvaluation
     */
    PerformanceEvaluation evaluate(Vendor vendor, double qualityScore, double deliveryScore, double complianceScore);

    /**
     * Returns a human-readable name for this strategy.
     */
    String getStrategyName();
}
