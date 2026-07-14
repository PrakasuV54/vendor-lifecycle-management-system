package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.exception.InvalidStateTransitionException;
import com.vlms.model.Vendor;

/**
 * Abstract base class for all Vendor lifecycle states.
 * State Pattern: each concrete state implements only valid transitions.
 * Open/Closed Principle: new states are added by extending this class, not modifying it.
 */
public abstract class VendorState {

    /**
     * Returns the VendorStatus enum value this state represents.
     */
    public abstract VendorStatus getStatus();

    // Default implementations throw InvalidStateTransitionException.
    // Concrete states override only the transitions they permit.

    public void submitForVerification(Vendor vendor) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.UNDER_VERIFICATION);
    }

    public void markVerified(Vendor vendor) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.VERIFIED);
    }

    public void approve(Vendor vendor) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.APPROVED);
    }

    public void activate(Vendor vendor) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.ACTIVE);
    }

    public void suspend(Vendor vendor, String reason) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.SUSPENDED);
    }

    public void reinstate(Vendor vendor) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.ACTIVE);
    }

    public void blacklist(Vendor vendor, String reason) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.BLACKLISTED);
    }

    public void terminate(Vendor vendor, String reason) {
        throw new InvalidStateTransitionException(getStatus(), VendorStatus.TERMINATED);
    }

    @Override
    public String toString() {
        return "VendorState[" + getStatus() + "]";
    }
}
