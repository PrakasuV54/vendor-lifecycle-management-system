package com.vlms.model;

import com.vlms.enums.InvoiceStatus;
import com.vlms.util.DateUtil;
import com.vlms.util.IdGenerator;
import com.vlms.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an invoice submitted by a vendor for services rendered.
 */
public class Invoice {

    private final String invoiceId;
    private final String vendorId;
    private final String contractId;
    private final String description;
    private final double amount;
    private final LocalDate invoiceDate;
    private final LocalDate dueDate;
    private InvoiceStatus status;
    private String rejectionReason;
    private final LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByUserId;

    public Invoice(String vendorId, String contractId, String description,
                   double amount, LocalDate invoiceDate, LocalDate dueDate) {
        ValidationUtil.requireNonBlank(vendorId, "Vendor ID");
        ValidationUtil.requireNonBlank(contractId, "Contract ID");
        ValidationUtil.requirePositive(amount, "Invoice amount");

        this.invoiceId = IdGenerator.generateInvoiceId();
        this.vendorId = vendorId;
        this.contractId = contractId;
        this.description = description;
        this.amount = amount;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void approve(String reviewedByUserId) {
        this.status = InvoiceStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedByUserId = reviewedByUserId;
        this.rejectionReason = null;
    }

    public void reject(String reason, String reviewedByUserId) {
        ValidationUtil.requireNonBlank(reason, "Rejection reason");
        this.status = InvoiceStatus.REJECTED;
        this.rejectionReason = reason;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedByUserId = reviewedByUserId;
    }

    public boolean isOverdue() {
        return status == InvoiceStatus.SUBMITTED && DateUtil.isExpired(dueDate);
    }

    // --- Getters ---

    public String getInvoiceId() { return invoiceId; }
    public String getVendorId() { return vendorId; }
    public String getContractId() { return contractId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public LocalDate getDueDate() { return dueDate; }
    public InvoiceStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getReviewedByUserId() { return reviewedByUserId; }

    @Override
    public String toString() {
        return String.format("Invoice[%s | Vendor: %s | Amount: %.2f | Status: %s | Due: %s]",
                invoiceId, vendorId, amount, status, DateUtil.format(dueDate));
    }
}
