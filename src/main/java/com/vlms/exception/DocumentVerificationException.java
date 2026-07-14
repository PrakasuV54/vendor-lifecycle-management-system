package com.vlms.exception;

/**
 * Thrown when document verification fails due to invalid content or missing information.
 */
public class DocumentVerificationException extends RuntimeException {

    private final String documentId;
    private final String rejectionReason;

    public DocumentVerificationException(String documentId, String rejectionReason) {
        super("Document verification failed for document [" + documentId + "]: " + rejectionReason);
        this.documentId = documentId;
        this.rejectionReason = rejectionReason;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
