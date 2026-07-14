package com.vlms.service;

import com.vlms.enums.ContractStatus;
import com.vlms.enums.PaymentStatus;
import com.vlms.enums.RiskLevel;
import com.vlms.enums.VendorStatus;
import com.vlms.model.*;
import com.vlms.repository.*;
import com.vlms.util.DateUtil;

import java.util.List;

/**
 * ReportService: generates formatted console reports for all system entities.
 *
 * SRP: solely responsible for report generation.
 * Implements Reportable indirectly by calling toReportSummary() on Vendor.
 * DIP: Depends on repository abstractions, not concrete data sources.
 */
public class ReportService {

    private final VendorRepository vendorRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final DocumentRepository documentRepository;
    private final NotificationService notificationService;

    public ReportService(VendorRepository vendorRepository,
                         ContractRepository contractRepository,
                         InvoiceRepository invoiceRepository,
                         PaymentRepository paymentRepository,
                         DocumentRepository documentRepository,
                         NotificationService notificationService) {
        this.vendorRepository = vendorRepository;
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.documentRepository = documentRepository;
        this.notificationService = notificationService;
    }

    // ─── Report: Active Vendors ────────────────────────────────────────────────

    public void printActiveVendorsReport() {
        printSectionHeader("REPORT: ACTIVE VENDORS");
        List<Vendor> activeVendors = vendorRepository.findByStatus(VendorStatus.ACTIVE);
        if (activeVendors.isEmpty()) {
            System.out.println("  No active vendors found.");
        } else {
            activeVendors.forEach(v -> System.out.println("  " + v.toReportSummary()));
        }
        System.out.println("  Total: " + activeVendors.size() + " active vendor(s).");
        printSectionFooter();
    }

    // ─── Report: Pending Vendors ───────────────────────────────────────────────

    public void printPendingVendorsReport() {
        printSectionHeader("REPORT: PENDING VENDORS");
        List<Vendor> pendingVendors = vendorRepository.findByStatus(VendorStatus.PENDING);
        if (pendingVendors.isEmpty()) {
            System.out.println("  No pending vendors found.");
        } else {
            pendingVendors.forEach(v -> System.out.println("  " + v.toReportSummary()));
        }
        System.out.println("  Total: " + pendingVendors.size() + " pending vendor(s).");
        printSectionFooter();
    }

    // ─── Report: Expiring Contracts ────────────────────────────────────────────

    public void printExpiringContractsReport(int withinDays) {
        printSectionHeader("REPORT: CONTRACTS EXPIRING WITHIN " + withinDays + " DAYS");
        List<Contract> expiring = contractRepository.findExpiringWithinDays(withinDays);
        if (expiring.isEmpty()) {
            System.out.println("  No contracts expiring within " + withinDays + " days.");
        } else {
            expiring.forEach(c -> System.out.printf("  %-12s | %-35s | Value: %12.2f | Expires: %s | Days Left: %d%n",
                    c.getContractId(), c.getContractTitle(), c.getContractValue(),
                    DateUtil.format(c.getEndDate()), DateUtil.daysUntil(c.getEndDate())));
        }
        System.out.println("  Total: " + expiring.size() + " expiring contract(s).");
        printSectionFooter();
    }

    // ─── Report: High Risk Vendors ─────────────────────────────────────────────

    public void printHighRiskVendorsReport() {
        printSectionHeader("REPORT: HIGH RISK VENDORS");
        List<Vendor> highRisk = vendorRepository.findAll().stream()
                .filter(v -> v.getCurrentRiskLevel() == RiskLevel.HIGH)
                .toList();
        if (highRisk.isEmpty()) {
            System.out.println("  No high-risk vendors found.");
        } else {
            highRisk.forEach(v -> {
                System.out.printf("  %-12s | %-35s | Status: %-18s | Risk Score: %.1f%n",
                        v.getVendorId(), v.getCompanyName(), v.getStatus(),
                        v.getLatestRiskAssessment()
                                .map(r -> r.getRiskScore())
                                .orElse(0.0));
                v.getLatestRiskAssessment()
                        .ifPresent(r -> System.out.println("             Rationale: " + r.getRationale()));
            });
        }
        System.out.println("  Total: " + highRisk.size() + " high-risk vendor(s).");
        printSectionFooter();
    }

    // ─── Report: Vendor Performance ───────────────────────────────────────────

    public void printVendorPerformanceReport() {
        printSectionHeader("REPORT: VENDOR PERFORMANCE");
        List<Vendor> vendors = vendorRepository.findAll();
        if (vendors.isEmpty()) {
            System.out.println("  No vendors found.");
        } else {
            vendors.stream()
                    .filter(v -> v.getLatestEvaluation().isPresent())
                    .forEach(v -> {
                        PerformanceEvaluation eval = v.getLatestEvaluation().get();
                        System.out.printf("  %-12s | %-35s | Q: %4.1f | D: %4.1f | C: %4.1f | Overall: %5.2f | Grade: %s%n",
                                v.getVendorId(), v.getCompanyName(),
                                eval.getQualityScore(), eval.getDeliveryScore(),
                                eval.getComplianceScore(), eval.getOverallRating(),
                                eval.getPerformanceGrade());
                    });
        }
        printSectionFooter();
    }

    // ─── Report: Payments ─────────────────────────────────────────────────────

    public void printPaymentsReport() {
        printSectionHeader("REPORT: PAYMENTS SUMMARY");
        List<Payment> payments = paymentRepository.findAll();
        if (payments.isEmpty()) {
            System.out.println("  No payments found.");
        } else {
            payments.forEach(p -> System.out.printf("  %-12s | Invoice: %-10s | Vendor: %-12s | Amount: %12.2f | Mode: %-8s | Status: %s%n",
                    p.getPaymentId(), p.getInvoiceId(), p.getVendorId(),
                    p.getAmount(), p.getPaymentMode(), p.getStatus()));
        }
        double totalPaid = paymentRepository.getTotalPaidAmount();
        System.out.printf("  Total Paid Disbursements: ₹%.2f%n", totalPaid);
        System.out.println("  Total Transactions: " + payments.size());
        printSectionFooter();
    }

    // ─── Report: All Vendors ──────────────────────────────────────────────────

    public void printAllVendorsReport() {
        printSectionHeader("REPORT: ALL VENDORS");
        List<Vendor> vendors = vendorRepository.findAll();
        if (vendors.isEmpty()) {
            System.out.println("  No vendors registered.");
        } else {
            vendors.forEach(v -> System.out.println("  " + v.toReportSummary()));
        }
        System.out.println("  Total vendors registered: " + vendors.size());
        printSectionFooter();
    }

    // ─── Report: Notifications ────────────────────────────────────────────────

    public void printNotificationsSummary() {
        printSectionHeader("REPORT: NOTIFICATIONS SUMMARY");
        List<Notification> notifications = notificationService.getAllNotifications();
        System.out.println("  Total notifications generated: " + notifications.size());
        notifications.forEach(n -> System.out.println("  " + n));
        printSectionFooter();
    }

    // ─── Formatting helpers ───────────────────────────────────────────────────

    private void printSectionHeader(String title) {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════╗");
        System.out.printf("  ║  %-56s║%n", title);
        System.out.println("  ╚══════════════════════════════════════════════════════════╝");
    }

    private void printSectionFooter() {
        System.out.println("  ─────────────────────────────────────────────────────────────");
    }
}
