package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.model.Vendor;

/**
 * APPROVED state: vendor is approved and ready to be activated for work.
 * Allowed transitions:
 *   APPROVED → ACTIVE
 *   APPROVED → TERMINATED
 */
public class ApprovedState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.APPROVED;
    }

    @Override
    public void activate(Vendor vendor) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' activated. APPROVED → ACTIVE");
        vendor.setCurrentState(new ActiveState());
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' terminated before activation. APPROVED → TERMINATED");
        vendor.setTerminationReason(reason);
        vendor.setCurrentState(new TerminatedState());
    }
}
