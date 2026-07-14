package com.vlms.model;

import com.vlms.enums.VendorType;

/**
 * Contractor vendor: provides project-based skilled labor and construction services.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Contractor extends Vendor {

    private final String specialisation;    // e.g., "Civil Engineering", "IT Infrastructure"
    private final int teamSize;
    private final boolean hasInsurance;
    private String insurancePolicyNumber;

    public Contractor(String companyName, String contactPersonName, String email,
                      String phone, String address, String panNumber,
                      String specialisation, int teamSize, boolean hasInsurance, String insurancePolicyNumber) {
        super(companyName, contactPersonName, email, phone, address, VendorType.CONTRACTOR, panNumber);
        this.specialisation = specialisation;
        this.teamSize = teamSize;
        this.hasInsurance = hasInsurance;
        this.insurancePolicyNumber = insurancePolicyNumber;
    }

    @Override
    public String getVendorCategory() {
        return "Contractor";
    }

    @Override
    public String getCategorySpecificDetails() {
        return String.format("Specialisation: %s | Team Size: %d | Insured: %s | Policy: %s",
                specialisation, teamSize, hasInsurance ? "Yes" : "No",
                insurancePolicyNumber != null ? insurancePolicyNumber : "N/A");
    }

    public String getSpecialisation() { return specialisation; }
    public int getTeamSize() { return teamSize; }
    public boolean isHasInsurance() { return hasInsurance; }
    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }

    @Override
    public String toString() {
        return "Contractor[" + getVendorId() + " | " + getCompanyName() + " | " + specialisation + " | Status: " + getStatus() + "]";
    }
}
