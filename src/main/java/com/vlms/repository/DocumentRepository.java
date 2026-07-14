package com.vlms.repository;

import com.vlms.enums.DocumentStatus;
import com.vlms.model.Document;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for Document persistence.
 */
public interface DocumentRepository {
    void save(Document document);
    void update(Document document);
    Optional<Document> findById(String documentId);
    List<Document> findAll();
    List<Document> findByVendorId(String vendorId);
    List<Document> findByStatus(DocumentStatus status);
    List<Document> findExpiringWithinDays(int days);
}
