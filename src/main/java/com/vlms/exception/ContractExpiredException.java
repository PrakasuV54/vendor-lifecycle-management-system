package com.vlms.exception;

/**
 * Thrown when an operation is attempted on an expired contract.
 */
public class ContractExpiredException extends RuntimeException {

    private final String contractId;

    public ContractExpiredException(String contractId) {
        super("Contract [" + contractId + "] has already expired and cannot be modified.");
        this.contractId = contractId;
    }

    public ContractExpiredException(String contractId, String message) {
        super(message);
        this.contractId = contractId;
    }

    public String getContractId() {
        return contractId;
    }
}
