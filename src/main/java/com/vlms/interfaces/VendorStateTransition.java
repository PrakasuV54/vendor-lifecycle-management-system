package com.vlms.interfaces;

import com.vlms.model.Vendor;

/**
 * Contract for vendor lifecycle state transitions.
 * Implements Interface Segregation: state handling is separate from other concerns.
 */
public interface VendorStateTransition {

    /**
     * Submits the vendor for document verification.
     */
    void submitForVerification(Vendor vendor);

    /**
     * Marks the vendor as verified after document review.
     */
    void markVerified(Vendor vendor);

    /**
     * Approves the vendor for active engagement.
     */
    void approve(Vendor vendor);

    /**
     * Activates the vendor.
     */
    void activate(Vendor vendor);

    /**
     * Suspends the vendor temporarily.
     */
    void suspend(Vendor vendor, String reason);

    /**
     * Lifts suspension and reactivates the vendor.
     */
    void reinstate(Vendor vendor);

    /**
     * Blacklists the vendor due to serious violations.
     */
    void blacklist(Vendor vendor, String reason);

    /**
     * Permanently terminates the vendor.
     */
    void terminate(Vendor vendor, String reason);
}
