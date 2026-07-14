package com.vlms.repository;

import com.vlms.enums.DocumentStatus;
import com.vlms.model.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for Document entities.
 */
public class InMemoryDocumentRepository implements DocumentRepository {

    private final Map<String, Document> documentStore = new HashMap<>();

    @Override
    public void save(Document document) {
        documentStore.put(document.getDocumentId(), document);
    }

    @Override
    public void update(Document document) {
        documentStore.put(document.getDocumentId(), document);
    }

    @Override
    public Optional<Document> findById(String documentId) {
        return Optional.ofNullable(documentStore.get(documentId));
    }

    @Override
    public List<Document> findAll() {
        return new ArrayList<>(documentStore.values());
    }

    @Override
    public List<Document> findByVendorId(String vendorId) {
        return documentStore.values().stream()
                .filter(d -> d.getVendorId().equals(vendorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Document> findByStatus(DocumentStatus status) {
        return documentStore.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Document> findExpiringWithinDays(int days) {
        return documentStore.values().stream()
                .filter(d -> d.getStatus() == DocumentStatus.VERIFIED
                        && d.isExpiringWithinDays(days))
                .collect(Collectors.toList());
    }
}
