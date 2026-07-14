package com.vlms.repository;

import com.vlms.enums.PaymentStatus;
import com.vlms.model.Payment;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for Payment persistence.
 */
public interface PaymentRepository {
    void save(Payment payment);
    void update(Payment payment);
    Optional<Payment> findById(String paymentId);
    Optional<Payment> findByInvoiceId(String invoiceId);
    List<Payment> findAll();
    List<Payment> findByVendorId(String vendorId);
    List<Payment> findByStatus(PaymentStatus status);
    double getTotalPaidAmount();
}
