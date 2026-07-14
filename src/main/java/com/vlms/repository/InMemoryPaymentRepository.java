package com.vlms.repository;

import com.vlms.enums.PaymentStatus;
import com.vlms.model.Payment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for Payment entities.
 */
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> paymentStore = new HashMap<>();

    @Override
    public void save(Payment payment) {
        paymentStore.put(payment.getPaymentId(), payment);
    }

    @Override
    public void update(Payment payment) {
        paymentStore.put(payment.getPaymentId(), payment);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(paymentStore.get(paymentId));
    }

    @Override
    public Optional<Payment> findByInvoiceId(String invoiceId) {
        return paymentStore.values().stream()
                .filter(p -> p.getInvoiceId().equals(invoiceId))
                .findFirst();
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(paymentStore.values());
    }

    @Override
    public List<Payment> findByVendorId(String vendorId) {
        return paymentStore.values().stream()
                .filter(p -> p.getVendorId().equals(vendorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentStore.values().stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public double getTotalPaidAmount() {
        return paymentStore.values().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}
