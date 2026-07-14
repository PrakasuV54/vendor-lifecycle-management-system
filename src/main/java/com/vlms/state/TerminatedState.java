package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.exception.InvalidStateTransitionException;
import com.vlms.model.Vendor;

/**
 * TERMINATED state: the final, irreversible state of a vendor.
 * No transitions are permitted from this state.
 */
public class TerminatedState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.TERMINATED;
    }

    // All transition methods from base class throw InvalidStateTransitionException.
    // No override needed — the terminal state permits no further transitions.

    @Override
    public void submitForVerification(Vendor vendor) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.UNDER_VERIFICATION);
    }

    @Override
    public void markVerified(Vendor vendor) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.VERIFIED);
    }

    @Override
    public void approve(Vendor vendor) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.APPROVED);
    }

    @Override
    public void activate(Vendor vendor) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.ACTIVE);
    }

    @Override
    public void suspend(Vendor vendor, String reason) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.SUSPENDED);
    }

    @Override
    public void reinstate(Vendor vendor) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.ACTIVE);
    }

    @Override
    public void blacklist(Vendor vendor, String reason) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.BLACKLISTED);
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        throw new InvalidStateTransitionException(VendorStatus.TERMINATED, VendorStatus.TERMINATED);
    }
}
