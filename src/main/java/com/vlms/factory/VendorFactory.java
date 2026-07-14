package com.vlms.factory;

import com.vlms.model.Contractor;
import com.vlms.model.ServiceProvider;
import com.vlms.model.Supplier;
import com.vlms.model.Vendor;
import com.vlms.state.PendingState;

/**
 * Factory Pattern: creates the correct Vendor subtype based on VendorType.
 * Refactored to remove Object... varargs generic creation for strict type safety.
 */
public class VendorFactory {

    private VendorFactory() {
        throw new UnsupportedOperationException("VendorFactory is a static factory.");
    }

    /**
     * Creates a Supplier vendor.
     */
    public static Supplier createSupplier(String companyName, String contactPersonName,
                                          String email, String phone, String address,
                                          String panNumber, String productCategory,
                                          String warehouseLocation, double annualSupplyCapacity) {
        Supplier supplier = new Supplier(companyName, contactPersonName, email, phone,
                address, panNumber, productCategory, warehouseLocation, annualSupplyCapacity);
        initializeState(supplier);
        return supplier;
    }

    /**
     * Creates a Contractor vendor.
     */
    public static Contractor createContractor(String companyName, String contactPersonName,
                                              String email, String phone, String address,
                                              String panNumber, String specialisation,
                                              int teamSize, boolean hasInsurance,
                                              String insurancePolicyNumber) {
        Contractor contractor = new Contractor(companyName, contactPersonName, email, phone,
                address, panNumber, specialisation, teamSize, hasInsurance, insurancePolicyNumber);
        initializeState(contractor);
        return contractor;
    }

    /**
     * Creates a ServiceProvider vendor.
     */
    public static ServiceProvider createServiceProvider(String companyName, String contactPersonName,
                                                        String email, String phone, String address,
                                                        String panNumber, String serviceType,
                                                        String sla, boolean is24x7,
                                                        double monthlyCharge) {
        ServiceProvider sp = new ServiceProvider(companyName, contactPersonName, email, phone,
                address, panNumber, serviceType, sla, is24x7, monthlyCharge);
        initializeState(sp);
        return sp;
    }

    private static void initializeState(Vendor vendor) {
        vendor.setCurrentState(new PendingState());
    }
}
