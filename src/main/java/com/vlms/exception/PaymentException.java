package com.vlms.exception;

/**
 * Thrown when a payment operation fails due to business rule violations.
 */
public class PaymentException extends RuntimeException {

    private final String invoiceId;
    private final String reason;

    public PaymentException(String invoiceId, String reason) {
        super("Payment failed for invoice [" + invoiceId + "]: " + reason);
        this.invoiceId = invoiceId;
        this.reason = reason;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getReason() {
        return reason;
    }
}
