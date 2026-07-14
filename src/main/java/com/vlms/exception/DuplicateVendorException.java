package com.vlms.exception;

/**
 * Thrown when attempting to register a vendor that already exists.
 */
public class DuplicateVendorException extends RuntimeException {

    private final String vendorEmail;

    public DuplicateVendorException(String vendorEmail) {
        super("A vendor with email [" + vendorEmail + "] is already registered in the system.");
        this.vendorEmail = vendorEmail;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }
}
