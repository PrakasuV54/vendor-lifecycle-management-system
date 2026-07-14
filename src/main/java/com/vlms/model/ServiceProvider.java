package com.vlms.model;

import com.vlms.enums.VendorType;

/**
 * ServiceProvider vendor: delivers ongoing managed services (IT, security, cleaning, etc.).
 * Demonstrates Inheritance and Polymorphism.
 */
public class ServiceProvider extends Vendor {

    private final String serviceType;          // e.g., "IT Managed Services", "Facility Management"
    private final String serviceLevelAgreement;// SLA tier: "Gold", "Silver", "Bronze"
    private final boolean is24x7Support;
    private double monthlyServiceCharge;

    public ServiceProvider(String companyName, String contactPersonName, String email,
                           String phone, String address, String panNumber,
                           String serviceType, String serviceLevelAgreement,
                           boolean is24x7Support, double monthlyServiceCharge) {
        super(companyName, contactPersonName, email, phone, address, VendorType.SERVICE_PROVIDER, panNumber);
        this.serviceType = serviceType;
        this.serviceLevelAgreement = serviceLevelAgreement;
        this.is24x7Support = is24x7Support;
        this.monthlyServiceCharge = monthlyServiceCharge;
    }

    @Override
    public String getVendorCategory() {
        return "Service Provider";
    }

    @Override
    public String getCategorySpecificDetails() {
        return String.format("Service: %s | SLA: %s | 24x7: %s | Monthly Charge: %.2f",
                serviceType, serviceLevelAgreement, is24x7Support ? "Yes" : "No", monthlyServiceCharge);
    }

    public void updateMonthlyCharge(double newCharge) {
        if (newCharge < 0) throw new IllegalArgumentException("Monthly charge cannot be negative.");
        this.monthlyServiceCharge = newCharge;
    }

    public String getServiceType() { return serviceType; }
    public String getServiceLevelAgreement() { return serviceLevelAgreement; }
    public boolean is24x7Support() { return is24x7Support; }
    public double getMonthlyServiceCharge() { return monthlyServiceCharge; }

    @Override
    public String toString() {
        return "ServiceProvider[" + getVendorId() + " | " + getCompanyName() + " | " + serviceType + " | Status: " + getStatus() + "]";
    }
}
