package com.vlms.repository;

import com.vlms.enums.InvoiceStatus;
import com.vlms.model.Invoice;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for Invoice persistence.
 */
public interface InvoiceRepository {
    void save(Invoice invoice);
    void update(Invoice invoice);
    Optional<Invoice> findById(String invoiceId);
    List<Invoice> findAll();
    List<Invoice> findByVendorId(String vendorId);
    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByContractId(String contractId);
}
