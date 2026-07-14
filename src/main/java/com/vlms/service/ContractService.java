package com.vlms.service;

import com.vlms.enums.ContractStatus;
import com.vlms.event.VendorEvent;
import com.vlms.exception.ContractExpiredException;
import com.vlms.model.Contract;
import com.vlms.model.Vendor;
import com.vlms.repository.ContractRepository;
import com.vlms.repository.VendorRepository;
import com.vlms.util.ConsoleLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ContractService: manages the full contract lifecycle.
 * Refactored to depend on repository interfaces and EventBus.
 */
public class ContractService {

    private final ContractRepository contractRepository;
    private final VendorRepository vendorRepository;
    private final EventBus eventBus;

    public ContractService(ContractRepository contractRepository,
                           VendorRepository vendorRepository,
                           EventBus eventBus) {
        this.contractRepository = contractRepository;
        this.vendorRepository = vendorRepository;
        this.eventBus = eventBus;
    }

    public Contract createContract(String vendorId, String contractTitle, String description,
                                   double contractValue, LocalDate startDate, LocalDate endDate) {
        vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

        Contract contract = new Contract(vendorId, contractTitle, description,
                contractValue, startDate, endDate);
        contractRepository.save(contract);
        ConsoleLogger.info("  [ContractService] Created contract: " + contract.getContractId()
                + " for vendor " + vendorId + " | Value: " + contractValue);

        eventBus.publish(new VendorEvent("CONTRACT_CREATED")
                .withParam("entityId", contract.getContractId())
                .withParam("entityType", "Contract")
                .withParam("vendorId", vendorId)
                .withParam("remarks", "Contract value: " + contractValue));

        return contract;
    }

    public void saveContract(Contract contract) {
        vendorRepository.findById(contract.getVendorId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + contract.getVendorId()));
        contractRepository.save(contract);
        ConsoleLogger.info("  [ContractService] Saved contract: " + contract.getContractId()
                + " for vendor " + contract.getVendorId() + " | Value: " + contract.getContractValue());

        eventBus.publish(new VendorEvent("CONTRACT_CREATED")
                .withParam("entityId", contract.getContractId())
                .withParam("entityType", "Contract")
                .withParam("vendorId", contract.getVendorId())
                .withParam("remarks", "Contract value: " + contract.getContractValue()));
    }

    public void activateContract(String contractId) {
        Contract contract = findContractOrThrow(contractId);
        ContractStatus old = contract.getStatus();
        contract.activate();
        contractRepository.update(contract);
        ConsoleLogger.info("  [ContractService] Activated contract: " + contractId);

        eventBus.publish(new VendorEvent("CONTRACT_ACTIVATED")
                .withParam("entityId", contractId)
                .withParam("entityType", "Contract")
                .withParam("oldState", old.name())
                .withParam("newState", contract.getStatus().name()));
    }

    public void renewContract(String contractId, LocalDate newEndDate) {
        Contract contract = findContractOrThrow(contractId);

        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new ContractExpiredException(contractId, "Cannot renew a cancelled contract.");
        }

        ContractStatus old = contract.getStatus();
        contract.renew(newEndDate);
        contractRepository.update(contract);

        ConsoleLogger.info("  [ContractService] Renewed contract: " + contractId
                + " | New end date: " + newEndDate);

        eventBus.publish(new VendorEvent("CONTRACT_RENEWED")
                .withParam("entityId", contractId)
                .withParam("entityType", "Contract")
                .withParam("oldState", old.name())
                .withParam("newState", contract.getStatus().name())
                .withParam("remarks", "Renewed until: " + newEndDate));
    }

    public void expireContract(String contractId) {
        Contract contract = findContractOrThrow(contractId);
        ContractStatus old = contract.getStatus();
        contract.expire();
        contractRepository.update(contract);

        ConsoleLogger.info("  [ContractService] Expired contract: " + contractId);

        eventBus.publish(new VendorEvent("CONTRACT_EXPIRING")
                .withParam("entityId", contractId)
                .withParam("entityType", "Contract")
                .withParam("oldState", old.name())
                .withParam("newState", contract.getStatus().name()));
    }

    public void cancelContract(String contractId, String reason) {
        Contract contract = findContractOrThrow(contractId);

        if (contract.getStatus() == ContractStatus.EXPIRED) {
            throw new ContractExpiredException(contractId);
        }

        ContractStatus old = contract.getStatus();
        contract.cancel(reason);
        contractRepository.update(contract);
        ConsoleLogger.info("  [ContractService] Cancelled contract: " + contractId + " | Reason: " + reason);

        eventBus.publish(new VendorEvent("CONTRACT_CANCELLED")
                .withParam("entityId", contractId)
                .withParam("entityType", "Contract")
                .withParam("oldState", old.name())
                .withParam("newState", contract.getStatus().name())
                .withParam("remarks", reason));
    }

    /**
     * Automatically checks and expires contracts that are past their end date.
     */
    public int processExpiredContracts() {
        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);
        List<Contract> renewedContracts = contractRepository.findByStatus(ContractStatus.RENEWED);

        int expiredCount = 0;
        for (Contract contract : activeContracts) {
            if (contract.isExpired()) {
                expireContract(contract.getContractId());
                expiredCount++;
            }
        }
        for (Contract contract : renewedContracts) {
            if (contract.isExpired()) {
                expireContract(contract.getContractId());
                expiredCount++;
            }
        }
        return expiredCount;
    }

    public List<Contract> getContractsByVendor(String vendorId) {
        return contractRepository.findByVendorId(vendorId);
    }

    public List<Contract> getContractsExpiringWithinDays(int days) {
        return contractRepository.findExpiringWithinDays(days);
    }

    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    public Contract findContractOrThrow(String contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + contractId));
    }
}
