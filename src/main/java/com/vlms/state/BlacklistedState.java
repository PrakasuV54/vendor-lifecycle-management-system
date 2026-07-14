package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.model.Vendor;

/**
 * BLACKLISTED state: vendor flagged for serious compliance/fraud violations.
 * Only permitted transition: BLACKLISTED → TERMINATED
 */
public class BlacklistedState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.BLACKLISTED;
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' terminated from blacklist. BLACKLISTED → TERMINATED");
        vendor.setTerminationReason(reason);
        vendor.setCurrentState(new TerminatedState());
    }
}
