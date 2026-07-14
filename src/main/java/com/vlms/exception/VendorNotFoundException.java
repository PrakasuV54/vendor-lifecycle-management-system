package com.vlms.exception;

/**
 * Thrown when a vendor is not found in the repository.
 */
public class VendorNotFoundException extends RuntimeException {

    private final String vendorId;

    public VendorNotFoundException(String vendorId) {
        super("Vendor not found with ID: " + vendorId);
        this.vendorId = vendorId;
    }

    public VendorNotFoundException(String vendorId, String message) {
        super(message);
        this.vendorId = vendorId;
    }

    public String getVendorId() {
        return vendorId;
    }
}
