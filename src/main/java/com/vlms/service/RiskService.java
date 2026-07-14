package com.vlms.service;

import com.vlms.enums.RiskLevel;
import com.vlms.model.PerformanceEvaluation;
import com.vlms.model.RiskAssessment;
import com.vlms.model.Vendor;
import com.vlms.repository.VendorRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RiskService: computes vendor risk levels based on performance, status, and document health.
 * SRP: solely responsible for risk computation and assessment.
 * OCP: Risk scoring formula can be extended without modifying other services.
 */
public class RiskService {

    private final VendorRepository vendorRepository;

    public RiskService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    /**
     * Assesses risk for a vendor based on multiple risk dimensions.
     * Risk Score (0-100): higher score = higher risk.
     *
     * Risk factors:
     * - Low/missing performance rating: +30
     * - Unverified documents: +25
     * - Suspended status: +20
     * - No contract: +15
     * - Low compliance score: +10
     */
    public RiskAssessment assessRisk(String vendorId, String assessedByUserId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

        double riskScore = 0.0;
        StringBuilder rationale = new StringBuilder();

        // Factor 1: Performance rating
        double overallRating = vendor.getLatestEvaluation()
                .map(PerformanceEvaluation::getOverallRating)
                .orElse(0.0);

        if (overallRating == 0.0) {
            riskScore += 30;
            rationale.append("No performance evaluation on record (+30). ");
        } else if (overallRating < 5.0) {
            riskScore += 25;
            rationale.append("Poor performance rating ").append(overallRating).append(" (+25). ");
        } else if (overallRating < 7.0) {
            riskScore += 10;
            rationale.append("Below-average performance (+10). ");
        }

        // Factor 2: Document health
        long unverifiedDocs = vendor.getDocuments().stream()
                .filter(d -> d.getStatus() != com.vlms.enums.DocumentStatus.VERIFIED)
                .count();
        if (unverifiedDocs > 0) {
            riskScore += 25;
            rationale.append(unverifiedDocs).append(" unverified document(s) (+25). ");
        }

        // Factor 3: Vendor status risk
        switch (vendor.getStatus()) {
            case SUSPENDED -> { riskScore += 20; rationale.append("Vendor is currently suspended (+20). "); }
            case BLACKLISTED -> { riskScore += 40; rationale.append("Vendor is blacklisted (+40). "); }
            case TERMINATED -> { riskScore += 50; rationale.append("Vendor is terminated (+50). "); }
            default -> { /* no additional risk */ }
        }

        // Factor 4: Compliance score
        double complianceScore = vendor.getLatestEvaluation()
                .map(PerformanceEvaluation::getComplianceScore)
                .orElse(0.0);
        if (complianceScore > 0 && complianceScore < 6.0) {
            riskScore += 10;
            rationale.append("Low compliance score ").append(complianceScore).append(" (+10). ");
        }

        riskScore = Math.min(riskScore, 100.0);

        RiskLevel level;
        if (riskScore <= 25.0) {
            level = RiskLevel.LOW;
        } else if (riskScore <= 55.0) {
            level = RiskLevel.MEDIUM;
        } else {
            level = RiskLevel.HIGH;
        }

        String rationaleStr = rationale.length() > 0 ? rationale.toString().trim() : "No significant risk factors detected.";
        RiskAssessment assessment = new RiskAssessment(vendorId, level, rationaleStr, riskScore, assessedByUserId);
        vendor.setLatestRiskAssessment(assessment);
        vendorRepository.update(vendor);

        System.out.println("  [RiskService] Risk assessed for " + vendorId
                + " | Level: " + level + " | Score: " + riskScore);
        return assessment;
    }

    public List<Vendor> getHighRiskVendors() {
        return vendorRepository.findAll().stream()
                .filter(v -> v.getCurrentRiskLevel() == RiskLevel.HIGH)
                .collect(Collectors.toList());
    }

    public List<Vendor> getMediumRiskVendors() {
        return vendorRepository.findAll().stream()
                .filter(v -> v.getCurrentRiskLevel() == RiskLevel.MEDIUM)
                .collect(Collectors.toList());
    }
}
