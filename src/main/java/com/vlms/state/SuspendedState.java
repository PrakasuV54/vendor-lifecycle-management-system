package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.model.Vendor;

/**
 * SUSPENDED state: vendor is temporarily halted.
 * Allowed transitions:
 *   SUSPENDED → ACTIVE     (reinstatement)
 *   SUSPENDED → BLACKLISTED (escalation)
 *   SUSPENDED → TERMINATED  (final action)
 */
public class SuspendedState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.SUSPENDED;
    }

    @Override
    public void reinstate(Vendor vendor) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' reinstated. SUSPENDED → ACTIVE");
        vendor.setSuspensionReason(null);
        vendor.setCurrentState(new ActiveState());
    }

    @Override
    public void blacklist(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' blacklisted from suspension. Reason: " + reason + ". SUSPENDED → BLACKLISTED");
        vendor.setBlacklistReason(reason);
        vendor.setCurrentState(new BlacklistedState());
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' terminated from suspension. SUSPENDED → TERMINATED");
        vendor.setTerminationReason(reason);
        vendor.setCurrentState(new TerminatedState());
    }
}
