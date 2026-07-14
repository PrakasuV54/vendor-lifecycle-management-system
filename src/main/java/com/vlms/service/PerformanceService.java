package com.vlms.service;

import com.vlms.interfaces.EvaluationStrategy;
import com.vlms.model.PerformanceEvaluation;
import com.vlms.model.Vendor;
import com.vlms.repository.VendorRepository;
import com.vlms.strategy.StandardEvaluationStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * PerformanceService: evaluates vendor performance using pluggable Strategy Pattern.
 *
 * OCP: New evaluation strategies are injected — no changes to this service.
 * DIP: Depends on EvaluationStrategy interface, not concrete strategy classes.
 */
public class PerformanceService {

    private final VendorRepository vendorRepository;
    private EvaluationStrategy evaluationStrategy;

    public PerformanceService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
        // Default strategy is Standard
        this.evaluationStrategy = new StandardEvaluationStrategy();
    }

    /**
     * Switches the evaluation strategy at runtime (Strategy Pattern).
     */
    public void setEvaluationStrategy(EvaluationStrategy strategy) {
        this.evaluationStrategy = strategy;
        System.out.println("  [PerformanceService] Evaluation strategy set to: " + strategy.getStrategyName());
    }

    /**
     * Evaluates a vendor using the currently active strategy and stores the result.
     */
    public PerformanceEvaluation evaluateVendor(String vendorId, double qualityScore,
                                                 double deliveryScore, double complianceScore) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

        PerformanceEvaluation evaluation = evaluationStrategy.evaluate(
                vendor, qualityScore, deliveryScore, complianceScore);

        vendor.addEvaluation(evaluation);
        vendorRepository.update(vendor);

        System.out.println("  [PerformanceService] Evaluated vendor " + vendorId
                + " | Strategy: " + evaluationStrategy.getStrategyName()
                + " | Overall: " + evaluation.getOverallRating()
                + " (" + evaluation.getPerformanceGrade() + ")");

        return evaluation;
    }

    public Optional<PerformanceEvaluation> getLatestEvaluation(String vendorId) {
        return vendorRepository.findById(vendorId)
                .flatMap(Vendor::getLatestEvaluation);
    }

    public List<PerformanceEvaluation> getAllEvaluationsForVendor(String vendorId) {
        return vendorRepository.findById(vendorId)
                .map(Vendor::getAllEvaluations)
                .orElse(List.of());
    }

    /**
     * Returns vendors sorted by their latest overall rating, descending.
     */
    public List<Vendor> getVendorsRankedByPerformance() {
        List<Vendor> allVendors = new ArrayList<>(vendorRepository.findAll());
        allVendors.sort(Comparator.comparingDouble(v ->
                -v.getLatestEvaluation().map(PerformanceEvaluation::getOverallRating).orElse(0.0)));
        return allVendors;
    }

    public String getCurrentStrategyName() {
        return evaluationStrategy.getStrategyName();
    }
}
