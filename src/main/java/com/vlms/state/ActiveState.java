package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.model.Vendor;

/**
 * ACTIVE state: vendor is fully operational and engaged.
 * Allowed transitions:
 *   ACTIVE → SUSPENDED
 *   ACTIVE → BLACKLISTED
 *   ACTIVE → TERMINATED
 */
public class ActiveState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.ACTIVE;
    }

    @Override
    public void suspend(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' suspended. Reason: " + reason + ". ACTIVE → SUSPENDED");
        vendor.setSuspensionReason(reason);
        vendor.setCurrentState(new SuspendedState());
    }

    @Override
    public void blacklist(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' blacklisted. Reason: " + reason + ". ACTIVE → BLACKLISTED");
        vendor.setBlacklistReason(reason);
        vendor.setCurrentState(new BlacklistedState());
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' terminated. Reason: " + reason + ". ACTIVE → TERMINATED");
        vendor.setTerminationReason(reason);
        vendor.setCurrentState(new TerminatedState());
    }
}
