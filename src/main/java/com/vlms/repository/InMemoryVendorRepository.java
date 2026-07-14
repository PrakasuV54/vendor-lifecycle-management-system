package com.vlms.repository;

import com.vlms.enums.VendorStatus;
import com.vlms.enums.VendorType;
import com.vlms.exception.DuplicateVendorException;
import com.vlms.model.Vendor;
import com.vlms.util.ConsoleLogger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for Vendor entities.
 */
public class InMemoryVendorRepository implements VendorRepository {

    private final Map<String, Vendor> vendorStore = new HashMap<>();
    private final Set<String> registeredEmails = new HashSet<>();

    @Override
    public void save(Vendor vendor) {
        if (registeredEmails.contains(vendor.getEmail())) {
            throw new DuplicateVendorException(vendor.getEmail());
        }
        vendorStore.put(vendor.getVendorId(), vendor);
        registeredEmails.add(vendor.getEmail());
        ConsoleLogger.info("  [Repository] Vendor saved: " + vendor.getVendorId() + " - " + vendor.getCompanyName());
    }

    @Override
    public void update(Vendor vendor) {
        if (!vendorStore.containsKey(vendor.getVendorId())) {
            throw new NoSuchElementException("Vendor not found: " + vendor.getVendorId());
        }
        vendorStore.put(vendor.getVendorId(), vendor);
    }

    @Override
    public void remove(String vendorId) {
        Vendor removed = vendorStore.remove(vendorId);
        if (removed != null) {
            registeredEmails.remove(removed.getEmail());
        }
    }

    @Override
    public Optional<Vendor> findById(String vendorId) {
        return Optional.ofNullable(vendorStore.get(vendorId));
    }

    @Override
    public List<Vendor> searchByName(String name) {
        String lowerName = name.toLowerCase();
        return vendorStore.values().stream()
                .filter(v -> v.getCompanyName().toLowerCase().contains(lowerName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Vendor> findAll() {
        return new ArrayList<>(vendorStore.values());
    }

    @Override
    public List<Vendor> findByStatus(VendorStatus status) {
        return vendorStore.values().stream()
                .filter(v -> v.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Vendor> findByType(VendorType type) {
        return vendorStore.values().stream()
                .filter(v -> v.getVendorType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return registeredEmails.contains(email);
    }

    @Override
    public int count() {
        return vendorStore.size();
    }
}
