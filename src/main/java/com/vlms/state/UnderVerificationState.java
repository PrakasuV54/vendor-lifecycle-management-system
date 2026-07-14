package com.vlms.state;

import com.vlms.enums.VendorStatus;
import com.vlms.model.Vendor;

/**
 * UNDER_VERIFICATION state: documents are being reviewed.
 * Allowed transitions:
 *   UNDER_VERIFICATION → VERIFIED  (documents pass)
 *   UNDER_VERIFICATION → PENDING   (documents rejected, needs resubmission)
 */
public class UnderVerificationState extends VendorState {

    @Override
    public VendorStatus getStatus() {
        return VendorStatus.UNDER_VERIFICATION;
    }

    @Override
    public void markVerified(Vendor vendor) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' documents verified. UNDER_VERIFICATION → VERIFIED");
        vendor.setCurrentState(new VerifiedState());
    }

    /**
     * Documents rejected; vendor reverts to PENDING for resubmission.
     */
    @Override
    public void submitForVerification(Vendor vendor) {
        // Resubmission after document rejection re-enters this state
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' resubmitting documents. Remaining in UNDER_VERIFICATION.");
        vendor.setCurrentState(new UnderVerificationState());
    }

    @Override
    public void terminate(Vendor vendor, String reason) {
        System.out.println("  [State Transition] Vendor '" + vendor.getCompanyName()
                + "' terminated during verification. UNDER_VERIFICATION → TERMINATED");
        vendor.setTerminationReason(reason);
        vendor.setCurrentState(new TerminatedState());
    }
}
