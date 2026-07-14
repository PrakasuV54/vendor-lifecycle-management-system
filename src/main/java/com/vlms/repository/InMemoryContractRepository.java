package com.vlms.repository;

import com.vlms.enums.ContractStatus;
import com.vlms.model.Contract;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for Contract entities.
 */
public class InMemoryContractRepository implements ContractRepository {

    private final Map<String, Contract> contractStore = new HashMap<>();

    @Override
    public void save(Contract contract) {
        contractStore.put(contract.getContractId(), contract);
    }

    @Override
    public void update(Contract contract) {
        contractStore.put(contract.getContractId(), contract);
    }

    @Override
    public Optional<Contract> findById(String contractId) {
        return Optional.ofNullable(contractStore.get(contractId));
    }

    @Override
    public List<Contract> searchByName(String title) {
        String lower = title.toLowerCase();
        return contractStore.values().stream()
                .filter(c -> c.getContractTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findAll() {
        return new ArrayList<>(contractStore.values());
    }

    @Override
    public List<Contract> findByVendorId(String vendorId) {
        return contractStore.values().stream()
                .filter(c -> c.getVendorId().equals(vendorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByStatus(ContractStatus status) {
        return contractStore.values().stream()
                .filter(c -> c.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findExpiringWithinDays(int days) {
        return contractStore.values().stream()
                .filter(c -> c.isActive() && c.isExpiringWithinDays(days))
                .collect(Collectors.toList());
    }
}
