package com.vlms.service;

import com.vlms.enums.VendorStatus;
import com.vlms.enums.VendorType;
import com.vlms.event.VendorEvent;
import com.vlms.exception.VendorNotFoundException;
import com.vlms.model.Vendor;
import com.vlms.repository.VendorRepository;
import com.vlms.util.ConsoleLogger;

import java.util.List;
import java.util.Optional;

/**
 * VendorService: orchestrates all vendor-related operations.
 * Refactored to depend on repository interfaces and EventBus.
 */
public class VendorService {

    private final VendorRepository vendorRepository;
    private final EventBus eventBus;

    public VendorService(VendorRepository vendorRepository, EventBus eventBus) {
        this.vendorRepository = vendorRepository;
        this.eventBus = eventBus;
    }

    // ─── Registration ─────────────────────────────────────────────────────────

    public void registerVendor(Vendor vendor) {
        vendorRepository.save(vendor);
        ConsoleLogger.info("  [VendorService] Registered vendor: " + vendor.getVendorId()
                + " | " + vendor.getCompanyName() + " | Type: " + vendor.getVendorType());
    }

    // ─── Retrieval ─────────────────────────────────────────────────────────────

    public Vendor getVendorById(String vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorNotFoundException(vendorId));
    }

    public Optional<Vendor> findVendorById(String vendorId) {
        return vendorRepository.findById(vendorId);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public List<Vendor> getVendorsByStatus(VendorStatus status) {
        return vendorRepository.findByStatus(status);
    }

    public List<Vendor> getVendorsByType(VendorType type) {
        return vendorRepository.findByType(type);
    }

    public List<Vendor> searchVendorsByName(String name) {
        return vendorRepository.searchByName(name);
    }

    // ─── Update ────────────────────────────────────────────────────────────────

    public void updateVendorContactInfo(String vendorId, String contactPerson,
                                        String phone, String address) {
        Vendor vendor = getVendorById(vendorId);
        vendor.updateContactInfo(contactPerson, phone, address);
        vendorRepository.update(vendor);
        ConsoleLogger.info("  [VendorService] Updated contact info for: " + vendorId);
    }

    public void updateVendorGst(String vendorId, String gstNumber) {
        Vendor vendor = getVendorById(vendorId);
        vendor.updateGstNumber(gstNumber);
        vendorRepository.update(vendor);
    }

    // ─── Remove ────────────────────────────────────────────────────────────────

    public void removeVendor(String vendorId) {
        getVendorById(vendorId); // validates existence
        vendorRepository.remove(vendorId);
        ConsoleLogger.info("  [VendorService] Removed vendor: " + vendorId);
    }

    // ─── Lifecycle Transitions (delegated to State Pattern) ──────────────────

    public void submitForVerification(String vendorId) {
        Vendor vendor = getVendorById(vendorId);
        vendor.getCurrentState().submitForVerification(vendor);
        vendorRepository.update(vendor);
    }

    public void markVerified(String vendorId) {
        Vendor vendor = getVendorById(vendorId);
        vendor.getCurrentState().markVerified(vendor);
        vendorRepository.update(vendor);
    }

    public void approveVendor(String vendorId) {
        Vendor vendor = getVendorById(vendorId);
        VendorStatus old = vendor.getStatus();
        vendor.getCurrentState().approve(vendor);
        vendorRepository.update(vendor);

        eventBus.publish(new VendorEvent("VENDOR_APPROVED")
                .withParam("entityId", vendorId)
                .withParam("entityType", "Vendor")
                .withParam("oldState", old.name())
                .withParam("newState", vendor.getStatus().name())
                .withParam("remarks", "Approved by review process"));
    }

    public void activateVendor(String vendorId) {
        Vendor vendor = getVendorById(vendorId);
        VendorStatus old = vendor.getStatus();
        vendor.getCurrentState().activate(vendor);
        vendorRepository.update(vendor);

        eventBus.publish(new VendorEvent("VENDOR_ACTIVATED")
                .withParam("entityId", vendorId)
                .withParam("entityType", "Vendor")
                .withParam("oldState", old.name())
                .withParam("newState", vendor.getStatus().name()));
    }

    public void suspendVendor(String vendorId, String reason) {
        Vendor vendor = getVendorById(vendorId);
        VendorStatus old = vendor.getStatus();
        vendor.getCurrentState().suspend(vendor, reason);
        vendorRepository.update(vendor);

        eventBus.publish(new VendorEvent("VENDOR_SUSPENDED")
                .withParam("entityId", vendorId)
                .withParam("entityType", "Vendor")
                .withParam("oldState", old.name())
                .withParam("newState", vendor.getStatus().name())
                .withParam("remarks", reason));
    }

    public void reinstateVendor(String vendorId) {
        Vendor vendor = getVendorById(vendorId);
        VendorStatus old = vendor.getStatus();
        vendor.getCurrentState().reinstate(vendor);
        vendorRepository.update(vendor);

        eventBus.publish(new VendorEvent("VENDOR_ACTIVATED")
                .withParam("entityId", vendorId)
                .withParam("entityType", "Vendor")
                .withParam("oldState", old.name())
                .withParam("newState", vendor.getStatus().name())
                .withParam("remarks", "Reinstated from suspended status"));
    }

    public void blacklistVendor(String vendorId, String reason) {
        Vendor vendor = getVendorById(vendorId);
        VendorStatus old = vendor.getStatus();
        vendor.getCurrentState().blacklist(vendor, reason);
        vendorRepository.update(vendor);

        eventBus.publish(new VendorEvent("VENDOR_BLACKLISTED")
                .withParam("entityId", vendorId)
                .withParam("entityType", "Vendor")
                .withParam("oldState", old.name())
                .withParam("newState", vendor.getStatus().name())
                .withParam("remarks", reason));
    }

    public void terminateVendor(String vendorId, String reason) {
        Vendor vendor = getVendorById(vendorId);
        VendorStatus old = vendor.getStatus();
        vendor.getCurrentState().terminate(vendor, reason);
        vendorRepository.update(vendor);

        eventBus.publish(new VendorEvent("VENDOR_TERMINATED")
                .withParam("entityId", vendorId)
                .withParam("entityType", "Vendor")
                .withParam("oldState", old.name())
                .withParam("newState", vendor.getStatus().name())
                .withParam("remarks", reason));
    }
}
