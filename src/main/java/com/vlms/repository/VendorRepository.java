package com.vlms.repository;

import com.vlms.enums.VendorStatus;
import com.vlms.enums.VendorType;
import com.vlms.interfaces.Searchable;
import com.vlms.model.Vendor;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for Vendor persistence.
 */
public interface VendorRepository extends Searchable<Vendor> {
    void save(Vendor vendor);
    void update(Vendor vendor);
    void remove(String vendorId);
    Optional<Vendor> findById(String vendorId);
    List<Vendor> searchByName(String name);
    List<Vendor> findAll();
    List<Vendor> findByStatus(VendorStatus status);
    List<Vendor> findByType(VendorType type);
    boolean existsByEmail(String email);
    int count();
}
