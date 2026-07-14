package com.vlms.model;

import com.vlms.enums.PaymentStatus;
import com.vlms.util.IdGenerator;
import com.vlms.util.ValidationUtil;

import java.time.LocalDateTime;

/**
 * Represents a payment transaction for an approved invoice.
 */
public class Payment {

    private final String paymentId;
    private final String invoiceId;
    private final String vendorId;
    private final double amount;
    private PaymentStatus status;
    private String failureReason;
    private final LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private String processedByUserId;
    private final String paymentMode; // e.g., "NEFT", "RTGS", "Cheque"

    public Payment(String invoiceId, String vendorId, double amount, String paymentMode, String processedByUserId) {
        ValidationUtil.requireNonBlank(invoiceId, "Invoice ID");
        ValidationUtil.requireNonBlank(vendorId, "Vendor ID");
        ValidationUtil.requirePositive(amount, "Payment amount");

        this.paymentId = IdGenerator.generatePaymentId();
        this.invoiceId = invoiceId;
        this.vendorId = vendorId;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.processedByUserId = processedByUserId;
        this.status = PaymentStatus.PENDING;
        this.initiatedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = PaymentStatus.APPROVED;
    }

    public void markPaid() {
        this.status = PaymentStatus.PAID;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }

    // --- Getters ---

    public String getPaymentId() { return paymentId; }
    public String getInvoiceId() { return invoiceId; }
    public String getVendorId() { return vendorId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getProcessedByUserId() { return processedByUserId; }
    public String getPaymentMode() { return paymentMode; }

    @Override
    public String toString() {
        return String.format("Payment[%s | Invoice: %s | Amount: %.2f | Mode: %s | Status: %s]",
                paymentId, invoiceId, amount, paymentMode, status);
    }
}
