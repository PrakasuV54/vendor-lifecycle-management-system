package com.vlms.model;

import com.vlms.enums.RiskLevel;
import com.vlms.enums.VendorStatus;
import com.vlms.enums.VendorType;
import com.vlms.interfaces.Notifiable;
import com.vlms.interfaces.Reportable;
import com.vlms.state.VendorState;
import com.vlms.util.DateUtil;
import com.vlms.util.IdGenerator;
import com.vlms.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class representing a Vendor in the system.
 *
 * Design Patterns:
 *  - State Pattern: delegates lifecycle transitions to VendorState implementations
 *  - Template Method: getVendorCategory() is abstract; common behaviour is here
 *
 * OOP Principles:
 *  - Abstraction: common vendor attributes/methods are abstracted here
 *  - Encapsulation: all fields are private; mutations through controlled methods
 *  - Composition: contains Documents, Contracts, Evaluations (Composition)
 *  - Aggregation: references Notifications (Aggregation)
 */
public abstract class Vendor implements Notifiable, Reportable {

    // --- Core identity ---
    private final String vendorId;
    private String companyName;
    private String contactPersonName;
    private String email;
    private String phone;
    private String address;
    private final VendorType vendorType;

    // --- Tax and compliance identifiers ---
    private final String panNumber;
    private String gstNumber;

    // --- Lifecycle state ---
    private VendorStatus status;
    private VendorState currentState;       // State Pattern delegation
    private String suspensionReason;
    private String terminationReason;
    private String blacklistReason;

    // --- Timestamps ---
    private final LocalDateTime registeredAt;
    private LocalDateTime lastUpdatedAt;

    // --- Aggregated entities (Composition) ---
    private final List<Document> documents;
    private final List<PerformanceEvaluation> evaluations;
    private final List<Notification> notifications;
    private RiskAssessment latestRiskAssessment;

    protected Vendor(String companyName, String contactPersonName, String email,
                     String phone, String address, VendorType vendorType, String panNumber) {
        ValidationUtil.requireNonBlank(companyName, "Company name");
        ValidationUtil.requireNonBlank(contactPersonName, "Contact person name");
        ValidationUtil.requireValidEmail(email);
        ValidationUtil.requireNonBlank(panNumber, "PAN number");

        this.vendorId = IdGenerator.generateVendorId();
        this.companyName = companyName;
        this.contactPersonName = contactPersonName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.vendorType = vendorType;
        this.panNumber = panNumber;
        this.status = VendorStatus.PENDING;
        this.registeredAt = LocalDateTime.now();
        this.lastUpdatedAt = this.registeredAt;
        this.documents = new ArrayList<>();
        this.evaluations = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    // --- Abstract methods (Template Method Pattern) ---

    /**
     * Returns a category-specific label for this vendor type.
     */
    public abstract String getVendorCategory();

    /**
     * Returns category-specific additional details for reporting.
     */
    public abstract String getCategorySpecificDetails();

    // --- State delegation ---

    /**
     * Sets the internal state object (called by the state pattern).
     */
    public void setCurrentState(VendorState state) {
        this.currentState = state;
        this.status = state.getStatus();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public VendorState getCurrentState() {
        return currentState;
    }

    // --- Document management ---

    public void addDocument(Document document) {
        documents.add(document);
        lastUpdatedAt = LocalDateTime.now();
    }

    public List<Document> getDocuments() {
        return Collections.unmodifiableList(documents);
    }

    public boolean hasAllDocumentsVerified() {
        return !documents.isEmpty() &&
                documents.stream().allMatch(d -> d.getStatus() == com.vlms.enums.DocumentStatus.VERIFIED);
    }

    // --- Evaluation management ---

    public void addEvaluation(PerformanceEvaluation evaluation) {
        evaluations.add(evaluation);
        lastUpdatedAt = LocalDateTime.now();
    }

    public Optional<PerformanceEvaluation> getLatestEvaluation() {
        if (evaluations.isEmpty()) return Optional.empty();
        return Optional.of(evaluations.get(evaluations.size() - 1));
    }

    public List<PerformanceEvaluation> getAllEvaluations() {
        return Collections.unmodifiableList(evaluations);
    }

    // --- Risk management ---

    public void setLatestRiskAssessment(RiskAssessment assessment) {
        this.latestRiskAssessment = assessment;
        lastUpdatedAt = LocalDateTime.now();
    }

    public Optional<RiskAssessment> getLatestRiskAssessment() {
        return Optional.ofNullable(latestRiskAssessment);
    }

    public RiskLevel getCurrentRiskLevel() {
        return latestRiskAssessment != null ? latestRiskAssessment.getRiskLevel() : RiskLevel.LOW;
    }

    // --- Notifiable implementation ---

    @Override
    public void receiveNotification(Notification notification) {
        notifications.add(notification);
    }

    @Override
    public String getNotificationTarget() {
        return email;
    }

    public List<Notification> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    // --- Reportable implementation ---

    @Override
    public String toReportSummary() {
        return String.format("%-12s | %-35s | %-20s | %-18s | Risk: %-6s | Registered: %s",
                vendorId, companyName, vendorType, status,
                getCurrentRiskLevel(), DateUtil.format(registeredAt.toLocalDate()));
    }

    // --- Update methods (controlled mutation) ---

    public void updateContactInfo(String contactPersonName, String phone, String address) {
        if (!ValidationUtil.isNullOrBlank(contactPersonName)) this.contactPersonName = contactPersonName;
        if (!ValidationUtil.isNullOrBlank(phone)) this.phone = phone;
        if (!ValidationUtil.isNullOrBlank(address)) this.address = address;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void updateEmail(String email) {
        ValidationUtil.requireValidEmail(email);
        this.email = email;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void updateGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void setSuspensionReason(String reason) { this.suspensionReason = reason; }
    public void setTerminationReason(String reason) { this.terminationReason = reason; }
    public void setBlacklistReason(String reason) { this.blacklistReason = reason; }

    // --- Getters ---

    public String getVendorId() { return vendorId; }
    public String getCompanyName() { return companyName; }
    public String getContactPersonName() { return contactPersonName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public VendorType getVendorType() { return vendorType; }
    public String getPanNumber() { return panNumber; }
    public String getGstNumber() { return gstNumber; }
    public VendorStatus getStatus() { return status; }
    public String getSuspensionReason() { return suspensionReason; }
    public String getTerminationReason() { return terminationReason; }
    public String getBlacklistReason() { return blacklistReason; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }

    @Override
    public String toString() {
        return String.format("Vendor[%s | %s | %s | Status: %s | Risk: %s]",
                vendorId, companyName, vendorType, status, getCurrentRiskLevel());
    }
}
