package com.vlms.repository;

import com.vlms.enums.InvoiceStatus;
import com.vlms.model.Invoice;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for Invoice entities.
 */
public class InMemoryInvoiceRepository implements InvoiceRepository {

    private final Map<String, Invoice> invoiceStore = new HashMap<>();

    @Override
    public void save(Invoice invoice) {
        invoiceStore.put(invoice.getInvoiceId(), invoice);
    }

    @Override
    public void update(Invoice invoice) {
        invoiceStore.put(invoice.getInvoiceId(), invoice);
    }

    @Override
    public Optional<Invoice> findById(String invoiceId) {
        return Optional.ofNullable(invoiceStore.get(invoiceId));
    }

    @Override
    public List<Invoice> findAll() {
        return new ArrayList<>(invoiceStore.values());
    }

    @Override
    public List<Invoice> findByVendorId(String vendorId) {
        return invoiceStore.values().stream()
                .filter(i -> i.getVendorId().equals(vendorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return invoiceStore.values().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByContractId(String contractId) {
        return invoiceStore.values().stream()
                .filter(i -> i.getContractId().equals(contractId))
                .collect(Collectors.toList());
    }
}
