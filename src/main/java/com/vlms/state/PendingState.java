package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.model.Vendor;

/**
 * PENDING state: the initial state for every newly registered vendor.
 * Allowed transition: PENDING → UNDER_VERIFICATION
 */
public class PendingState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.PENDING;
    }

    @Override
    public void submitForVerification(Vendor vendor) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' submitted for document verification. PENDING → UNDER_VERIFICATION");
        vendor.setCurrentState(new UnderVerificationState());
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        // Allows rejection at pending stage (e.g., duplicate found after registration)
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' terminated at pending stage. PENDING → TERMINATED");
        vendor.setTerminationReason(reason);
        vendor.setCurrentState(new TerminatedState());
    }
}
