package com.vlms.model;

import com.vlms.enums.ContractStatus;
import com.vlms.util.DateUtil;
import com.vlms.util.IdGenerator;
import com.vlms.util.ValidationUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a legal contract between the organisation and a vendor.
 * Demonstrates Encapsulation: controlled state mutations through specific methods.
 */
public class Contract {

    private final String contractId;
    private final String vendorId;
    private final String contractTitle;
    private final String description;
    private final double contractValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private String cancellationReason;
    private int renewalCount;

    public Contract(String vendorId, String contractTitle, String description,
                    double contractValue, LocalDate startDate, LocalDate endDate) {
        ValidationUtil.requireNonBlank(vendorId, "Vendor ID");
        ValidationUtil.requireNonBlank(contractTitle, "Contract title");
        ValidationUtil.requirePositive(contractValue, "Contract value");

        this.contractId = IdGenerator.generateContractId();
        this.vendorId = vendorId;
        this.contractTitle = contractTitle;
        this.description = description;
        this.contractValue = contractValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = ContractStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = this.createdAt;
        this.renewalCount = 0;
    }

    public void activate() {
        this.status = ContractStatus.ACTIVE;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void renew(LocalDate newEndDate) {
        if (this.status == ContractStatus.CANCELLED) {
            throw new IllegalStateException("Cannot renew a cancelled contract: " + contractId);
        }
        this.endDate = newEndDate;
        this.status = ContractStatus.RENEWED;
        this.renewalCount++;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = ContractStatus.EXPIRED;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        ValidationUtil.requireNonBlank(reason, "Cancellation reason");
        this.status = ContractStatus.CANCELLED;
        this.cancellationReason = reason;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return DateUtil.isExpired(endDate);
    }

    public boolean isExpiringWithinDays(int days) {
        return DateUtil.isExpiringWithinDays(endDate, days);
    }

    public boolean isActive() {
        return status == ContractStatus.ACTIVE || status == ContractStatus.RENEWED;
    }

    // --- Getters ---

    public String getContractId() { return contractId; }
    public String getVendorId() { return vendorId; }
    public String getContractTitle() { return contractTitle; }
    public String getDescription() { return description; }
    public double getContractValue() { return contractValue; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ContractStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public String getCancellationReason() { return cancellationReason; }
    public int getRenewalCount() { return renewalCount; }

    @Override
    public String toString() {
        return String.format("Contract[%s | %s | Value: %.2f | Status: %s | End: %s]",
                contractId, contractTitle, contractValue, status, DateUtil.format(endDate));
    }
}
