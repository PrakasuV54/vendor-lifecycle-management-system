package com.vlms.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating unique IDs across all entities.
 * Provides both UUID-based and counter-based ID generation.
 * Demonstrates SRP: single responsibility of ID generation.
 */
public final class IdGenerator {

    private static final AtomicInteger vendorCounter = new AtomicInteger(1000);
    private static final AtomicInteger contractCounter = new AtomicInteger(2000);
    private static final AtomicInteger documentCounter = new AtomicInteger(3000);
    private static final AtomicInteger invoiceCounter = new AtomicInteger(4000);
    private static final AtomicInteger paymentCounter = new AtomicInteger(5000);
    private static final AtomicInteger userCounter = new AtomicInteger(100);
    private static final AtomicInteger notificationCounter = new AtomicInteger(9000);
    private static final AtomicInteger auditCounter = new AtomicInteger(6000);
    private static final AtomicInteger taskCounter = new AtomicInteger(7000);
    private static final AtomicInteger workflowCounter = new AtomicInteger(8000);

    // Private constructor enforces non-instantiability
    private IdGenerator() {
        throw new UnsupportedOperationException("IdGenerator is a utility class.");
    }

    public static String generateVendorId() {
        return "VND-" + vendorCounter.getAndIncrement();
    }

    public static String generateContractId() {
        return "CTR-" + contractCounter.getAndIncrement();
    }

    public static String generateDocumentId() {
        return "DOC-" + documentCounter.getAndIncrement();
    }

    public static String generateInvoiceId() {
        return "INV-" + invoiceCounter.getAndIncrement();
    }

    public static String generatePaymentId() {
        return "PAY-" + paymentCounter.getAndIncrement();
    }

    public static String generateUserId() {
        return "USR-" + userCounter.getAndIncrement();
    }

    public static String generateNotificationId() {
        return "NTF-" + notificationCounter.getAndIncrement();
    }

    public static String generateAuditId() {
        return "AUD-" + auditCounter.getAndIncrement();
    }

    public static String generateTaskId() {
        return "TSK-" + taskCounter.getAndIncrement();
    }

    public static String generateWorkflowId() {
        return "WKF-" + workflowCounter.getAndIncrement();
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
