package com.vlms.service;

import com.vlms.enums.InvoiceStatus;
import com.vlms.enums.PaymentStatus;
import com.vlms.event.VendorEvent;
import com.vlms.exception.PaymentException;
import com.vlms.model.Invoice;
import com.vlms.model.Payment;
import com.vlms.model.Vendor;
import com.vlms.repository.InvoiceRepository;
import com.vlms.repository.PaymentRepository;
import com.vlms.repository.VendorRepository;
import com.vlms.util.ConsoleLogger;

import java.util.List;
import java.util.Optional;

/**
 * PaymentService: manages the complete payment lifecycle.
 * Refactored to depend on repository interfaces and EventBus.
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final EventBus eventBus;

    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          VendorRepository vendorRepository,
                          EventBus eventBus) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.eventBus = eventBus;
    }

    /**
     * Initiates a payment for an approved invoice.
     * Validates that the invoice exists and has APPROVED status.
     */
    public Payment initiatePayment(String invoiceId, String paymentMode, String processedByUserId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new PaymentException(invoiceId, "Invoice not found."));

        if (invoice.getStatus() != InvoiceStatus.APPROVED) {
            throw new PaymentException(invoiceId,
                    "Payment can only be initiated for APPROVED invoices. Current status: " + invoice.getStatus());
        }

        // Check for duplicate payment
        Optional<Payment> existingPayment = paymentRepository.findByInvoiceId(invoiceId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.PAID) {
            throw new PaymentException(invoiceId, "This invoice has already been paid.");
        }

        Payment payment = new Payment(invoiceId, invoice.getVendorId(),
                invoice.getAmount(), paymentMode, processedByUserId);
        paymentRepository.save(payment);

        ConsoleLogger.info("  [PaymentService] Payment initiated: " + payment.getPaymentId()
                + " | Invoice: " + invoiceId + " | Amount: " + invoice.getAmount()
                + " | Mode: " + paymentMode);

        eventBus.publish(new VendorEvent("PAYMENT_INITIATED")
                .withParam("entityId", payment.getPaymentId())
                .withParam("entityType", "Payment")
                .withParam("invoiceId", invoiceId)
                .withParam("amount", payment.getAmount())
                .withParam("userId", processedByUserId));

        return payment;
    }

    /**
     * Approves a pending payment.
     */
    public void approvePayment(String paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException(payment.getInvoiceId(),
                    "Payment can only be approved from PENDING state. Current: " + payment.getStatus());
        }

        PaymentStatus old = payment.getStatus();
        payment.approve();
        paymentRepository.update(payment);
        ConsoleLogger.info("  [PaymentService] Payment approved: " + paymentId);

        eventBus.publish(new VendorEvent("PAYMENT_APPROVED")
                .withParam("entityId", paymentId)
                .withParam("entityType", "Payment")
                .withParam("oldState", old.name())
                .withParam("newState", payment.getStatus().name()));
    }

    /**
     * Marks an approved payment as PAID and triggers notifications.
     */
    public void processPayment(String paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            throw new PaymentException(payment.getInvoiceId(),
                    "Payment must be APPROVED before it can be processed. Current: " + payment.getStatus());
        }

        PaymentStatus old = payment.getStatus();
        payment.markPaid();
        paymentRepository.update(payment);

        ConsoleLogger.info("  [PaymentService] Payment processed: " + paymentId
                + " | Amount: " + payment.getAmount() + " | Mode: " + payment.getPaymentMode());

        eventBus.publish(new VendorEvent("PAYMENT_COMPLETED")
                .withParam("entityId", payment.getInvoiceId()) // Notification needs invoice ID to match event types
                .withParam("paymentId", paymentId)
                .withParam("entityType", "Payment")
                .withParam("oldState", old.name())
                .withParam("newState", payment.getStatus().name())
                .withParam("amount", payment.getAmount()));
    }

    /**
     * Marks a payment as FAILED.
     */
    public void failPayment(String paymentId, String reason) {
        Payment payment = findPaymentOrThrow(paymentId);
        PaymentStatus old = payment.getStatus();
        payment.fail(reason);
        paymentRepository.update(payment);
        ConsoleLogger.info("  [PaymentService] Payment failed: " + paymentId + " | Reason: " + reason);

        eventBus.publish(new VendorEvent("PAYMENT_FAILED")
                .withParam("entityId", paymentId)
                .withParam("entityType", "Payment")
                .withParam("oldState", old.name())
                .withParam("newState", payment.getStatus().name())
                .withParam("remarks", reason));
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByVendor(String vendorId) {
        return paymentRepository.findByVendorId(vendorId);
    }

    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public double getTotalPaidAmount() {
        return paymentRepository.getTotalPaidAmount();
    }

    private Payment findPaymentOrThrow(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    }
}
