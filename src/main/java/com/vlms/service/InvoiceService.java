package com.vlms.service;

import com.vlms.enums.InvoiceStatus;
import com.vlms.event.VendorEvent;
import com.vlms.exception.PaymentException;
import com.vlms.model.Invoice;
import com.vlms.repository.InvoiceRepository;
import com.vlms.repository.VendorRepository;
import com.vlms.util.ConsoleLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * InvoiceService manages invoice submission, approval, and rejection.
 * Refactored to depend on repository interfaces and EventBus.
 */
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final EventBus eventBus;

    public InvoiceService(InvoiceRepository invoiceRepository, VendorRepository vendorRepository, EventBus eventBus) {
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.eventBus = eventBus;
    }

    /**
     * Vendor submits an invoice for services rendered under a contract.
     */
    public Invoice submitInvoice(String vendorId, String contractId, String description,
                                  double amount, LocalDate invoiceDate, LocalDate dueDate) {
        vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

        Invoice invoice = new Invoice(vendorId, contractId, description, amount, invoiceDate, dueDate);
        invoiceRepository.save(invoice);
        ConsoleLogger.info("  [InvoiceService] Invoice submitted: " + invoice.getInvoiceId()
                + " | Vendor: " + vendorId + " | Amount: " + amount);

        eventBus.publish(new VendorEvent("INVOICE_SUBMITTED")
                .withParam("entityId", invoice.getInvoiceId())
                .withParam("entityType", "Invoice")
                .withParam("vendorId", vendorId)
                .withParam("amount", amount)
                .withParam("remarks", description));

        return invoice;
    }

    public void saveInvoice(Invoice invoice) {
        vendorRepository.findById(invoice.getVendorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + invoice.getVendorId()));
        invoiceRepository.save(invoice);
        ConsoleLogger.info("  [InvoiceService] Invoice saved: " + invoice.getInvoiceId()
                + " | Vendor: " + invoice.getVendorId() + " | Amount: " + invoice.getAmount());

        eventBus.publish(new VendorEvent("INVOICE_SUBMITTED")
                .withParam("entityId", invoice.getInvoiceId())
                .withParam("entityType", "Invoice")
                .withParam("vendorId", invoice.getVendorId())
                .withParam("amount", invoice.getAmount())
                .withParam("remarks", invoice.getDescription()));
    }

    /**
     * Finance manager approves an invoice for payment processing.
     */
    public void approveInvoice(String invoiceId, String approvedByUserId) {
        Invoice invoice = findInvoiceOrThrow(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.SUBMITTED) {
            throw new PaymentException(invoiceId,
                    "Invoice can only be approved if it is in SUBMITTED state. Current: " + invoice.getStatus());
        }

        InvoiceStatus old = invoice.getStatus();
        invoice.approve(approvedByUserId);
        invoiceRepository.update(invoice);
        ConsoleLogger.info("  [InvoiceService] Invoice approved: " + invoiceId + " by " + approvedByUserId);

        eventBus.publish(new VendorEvent("INVOICE_APPROVED")
                .withParam("entityId", invoiceId)
                .withParam("entityType", "Invoice")
                .withParam("oldState", old.name())
                .withParam("newState", invoice.getStatus().name())
                .withParam("userId", approvedByUserId));
    }

    /**
     * Finance manager rejects an invoice with a reason.
     */
    public void rejectInvoice(String invoiceId, String reason, String rejectedByUserId) {
        Invoice invoice = findInvoiceOrThrow(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.SUBMITTED) {
            throw new PaymentException(invoiceId,
                    "Invoice can only be rejected if it is in SUBMITTED state. Current: " + invoice.getStatus());
        }

        InvoiceStatus old = invoice.getStatus();
        invoice.reject(reason, rejectedByUserId);
        invoiceRepository.update(invoice);
        ConsoleLogger.info("  [InvoiceService] Invoice rejected: " + invoiceId + " | Reason: " + reason);

        eventBus.publish(new VendorEvent("INVOICE_REJECTED")
                .withParam("entityId", invoiceId)
                .withParam("entityType", "Invoice")
                .withParam("oldState", old.name())
                .withParam("newState", invoice.getStatus().name())
                .withParam("remarks", reason)
                .withParam("userId", rejectedByUserId));
    }

    public List<Invoice> getInvoicesByVendor(String vendorId) {
        return invoiceRepository.findByVendorId(vendorId);
    }

    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Optional<Invoice> findInvoiceById(String invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }

    public Invoice findInvoiceOrThrow(String invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
    }
}
