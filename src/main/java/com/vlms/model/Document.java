package com.vlms.model;

import com.vlms.enums.DocumentStatus;
import com.vlms.util.DateUtil;
import com.vlms.util.IdGenerator;
import com.vlms.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a document uploaded by a vendor (e.g. GST, PAN, ISO cert).
 * Demonstrates Encapsulation: all fields private, controlled mutation through methods.
 */
public class Document {

    private final String documentId;
    private final String vendorId;
    private final String documentType; // e.g., "GST Certificate", "PAN Card", "ISO 9001"
    private final String documentNumber;
    private final LocalDate issueDate;
    private LocalDate expiryDate;
    private DocumentStatus status;
    private String rejectionReason;
    private final LocalDateTime uploadedAt;
    private LocalDateTime verifiedAt;
    private String verifiedByUserId;

    public Document(String vendorId, String documentType, String documentNumber,
                    LocalDate issueDate, LocalDate expiryDate) {
        ValidationUtil.requireNonBlank(vendorId, "Vendor ID");
        ValidationUtil.requireNonBlank(documentType, "Document type");
        ValidationUtil.requireNonBlank(documentNumber, "Document number");

        this.documentId = IdGenerator.generateDocumentId();
        this.vendorId = vendorId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = DocumentStatus.PENDING;
        this.uploadedAt = LocalDateTime.now();
    }

    public void verify(String verifiedByUserId) {
        this.status = DocumentStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedByUserId = verifiedByUserId;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        ValidationUtil.requireNonBlank(reason, "Rejection reason");
        this.status = DocumentStatus.REJECTED;
        this.rejectionReason = reason;
        this.verifiedAt = LocalDateTime.now();
    }

    public void markExpired() {
        this.status = DocumentStatus.EXPIRED;
    }

    public boolean isExpired() {
        return expiryDate != null && DateUtil.isExpired(expiryDate);
    }

    public boolean isExpiringWithinDays(int days) {
        return DateUtil.isExpiringWithinDays(expiryDate, days);
    }

    // --- Getters ---

    public String getDocumentId() { return documentId; }
    public String getVendorId() { return vendorId; }
    public String getDocumentType() { return documentType; }
    public String getDocumentNumber() { return documentNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public DocumentStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public String getVerifiedByUserId() { return verifiedByUserId; }

    @Override
    public String toString() {
        return String.format("Document[%s | %s | %s | Status: %s | Expiry: %s]",
                documentId, documentType, documentNumber, status, DateUtil.format(expiryDate));
    }
}
