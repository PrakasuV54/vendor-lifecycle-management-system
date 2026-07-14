package com.vlms.service;

import com.vlms.enums.DocumentStatus;
import com.vlms.event.VendorEvent;
import com.vlms.exception.DocumentVerificationException;
import com.vlms.model.Document;
import com.vlms.model.Vendor;
import com.vlms.repository.DocumentRepository;
import com.vlms.repository.VendorRepository;
import com.vlms.util.ConsoleLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DocumentService manages document upload, verification, rejection, and expiry tracking.
 * Refactored to depend on repository interfaces and EventBus.
 */
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final VendorRepository vendorRepository;
    private final EventBus eventBus;

    public DocumentService(DocumentRepository documentRepository,
                           VendorRepository vendorRepository,
                           EventBus eventBus) {
        this.documentRepository = documentRepository;
        this.vendorRepository = vendorRepository;
        this.eventBus = eventBus;
    }

    /**
     * Uploads a document for a vendor and links it to that vendor's document list.
     */
    public Document uploadDocument(String vendorId, String documentType, String documentNumber,
                                   LocalDate issueDate, LocalDate expiryDate) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

        Document document = new Document(vendorId, documentType, documentNumber, issueDate, expiryDate);
        documentRepository.save(document);
        vendor.addDocument(document);
        vendorRepository.update(vendor);

        ConsoleLogger.info("  [DocumentService] Uploaded: " + document.getDocumentType()
                + " [" + document.getDocumentId() + "] for vendor " + vendorId);

        eventBus.publish(new VendorEvent("DOCUMENT_UPLOADED")
                .withParam("entityId", document.getDocumentId())
                .withParam("entityType", "Document")
                .withParam("vendorId", vendorId)
                .withParam("remarks", "Uploaded " + documentType));

        return document;
    }

    /**
     * Verifies a document after procurement manager review.
     */
    public void verifyDocument(String documentId, String verifiedByUserId) {
        Document document = findDocumentOrThrow(documentId);

        if (document.getStatus() == DocumentStatus.REJECTED) {
            throw new DocumentVerificationException(documentId,
                    "Cannot verify a document that has been rejected. It must be re-uploaded.");
        }
        if (document.isExpired()) {
            document.markExpired();
            documentRepository.update(document);
            throw new DocumentVerificationException(documentId,
                    "Document has expired and cannot be verified. Expiry: " + document.getExpiryDate());
        }

        DocumentStatus old = document.getStatus();
        document.verify(verifiedByUserId);
        documentRepository.update(document);
        ConsoleLogger.info("  [DocumentService] Verified: " + documentId + " by user " + verifiedByUserId);

        eventBus.publish(new VendorEvent("DOCUMENT_VERIFIED")
                .withParam("entityId", documentId)
                .withParam("entityType", "Document")
                .withParam("oldState", old.name())
                .withParam("newState", document.getStatus().name())
                .withParam("userId", verifiedByUserId));
    }

    /**
     * Rejects a document with a stated reason.
     */
    public void rejectDocument(String documentId, String reason, String rejectedByUserId) {
        Document document = findDocumentOrThrow(documentId);
        DocumentStatus old = document.getStatus();
        document.reject(reason);
        documentRepository.update(document);
        ConsoleLogger.info("  [DocumentService] Rejected: " + documentId + " | Reason: " + reason);

        eventBus.publish(new VendorEvent("DOCUMENT_REJECTED")
                .withParam("entityId", documentId)
                .withParam("entityType", "Document")
                .withParam("oldState", old.name())
                .withParam("newState", document.getStatus().name())
                .withParam("remarks", reason)
                .withParam("userId", rejectedByUserId));
    }

    /**
     * Scans all documents and marks expired ones; sends notifications.
     */
    public int markExpiredDocuments() {
        List<Document> allDocuments = documentRepository.findAll();
        int expiredCount = 0;
        for (Document doc : allDocuments) {
            if (doc.getStatus() == DocumentStatus.VERIFIED && doc.isExpired()) {
                doc.markExpired();
                documentRepository.update(doc);
                expiredCount++;

                eventBus.publish(new VendorEvent("DOCUMENT_EXPIRING")
                        .withParam("entityId", doc.getDocumentId())
                        .withParam("entityType", "Document")
                        .withParam("remarks", "Expired: " + doc.getDocumentType()));
            }
        }
        ConsoleLogger.info("  [DocumentService] Marked " + expiredCount + " document(s) as expired.");
        return expiredCount;
    }

    /**
     * Returns documents expiring within the given number of days.
     */
    public List<Document> getDocumentsExpiringWithinDays(int days) {
        return documentRepository.findExpiringWithinDays(days);
    }

    public List<Document> getDocumentsByVendor(String vendorId) {
        return documentRepository.findByVendorId(vendorId);
    }

    public Document findDocumentOrThrow(String documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }
}
