package com.vlms.rules;

import com.vlms.model.Vendor;
import com.vlms.enums.VendorStatus;

/**
 * Business rules related to Vendor validation and status checks.
 */
public class VendorRules {

    public static boolean canBeSuspended(Vendor vendor) {
        return vendor.getStatus() == VendorStatus.ACTIVE;
    }

    public static boolean canBeReinstated(Vendor vendor) {
        return vendor.getStatus() == VendorStatus.SUSPENDED;
    }

    public static boolean canBeBlacklisted(Vendor vendor) {
        return vendor.getStatus() != VendorStatus.BLACKLISTED && vendor.getStatus() != VendorStatus.TERMINATED;
    }

    public static boolean canBeTerminated(Vendor vendor) {
        return vendor.getStatus() != VendorStatus.TERMINATED;
    }
}
