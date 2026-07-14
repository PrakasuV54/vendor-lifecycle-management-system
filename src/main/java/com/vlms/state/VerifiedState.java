package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.model.Vendor;

/**
 * VERIFIED state: all documents have passed review.
 * Allowed transitions:
 *   VERIFIED → APPROVED  (procurement manager approves)
 *   VERIFIED → TERMINATED (rejected post-verification)
 */
public class VerifiedState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.VERIFIED;
    }

    @Override
    public void approve(Vendor vendor) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' approved after verification. VERIFIED → APPROVED");
        vendor.setCurrentState(new ApprovedState());
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' terminated post-verification. VERIFIED → TERMINATED");
        vendor.setTerminationReason(reason);
        vendor.setCurrentState(new TerminatedState());
    }
}
