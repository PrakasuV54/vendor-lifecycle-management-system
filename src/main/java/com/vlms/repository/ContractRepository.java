package com.vlms.repository;

import com.vlms.enums.ContractStatus;
import com.vlms.interfaces.Searchable;
import com.vlms.model.Contract;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for Contract persistence.
 */
public interface ContractRepository extends Searchable<Contract> {
    void save(Contract contract);
    void update(Contract contract);
    Optional<Contract> findById(String contractId);
    List<Contract> searchByName(String title);
    List<Contract> findAll();
    List<Contract> findByVendorId(String vendorId);
    List<Contract> findByStatus(ContractStatus status);
    List<Contract> findExpiringWithinDays(int days);
}
