package com.vlms.factory;

import com.vlms.model.Contractor;
import com.vlms.model.ServiceProvider;
import com.vlms.model.Supplier;

/**
 * VendorBuilder: Fluent builder for creating Vendor concrete instances.
 * Simplifies construction of complex objects with many fields.
 */
public class VendorBuilder {

    // Common fields
    private String companyName;
    private String contactPersonName;
    private String email;
    private String phone;
    private String address;
    private String panNumber;

    // Supplier specific
    private String productCategory;
    private String warehouseLocation;
    private double annualSupplyCapacity;

    // Contractor specific
    private String specialisation;
    private int teamSize;
    private boolean hasInsurance;
    private String insurancePolicyNumber;

    // ServiceProvider specific
    private String serviceType;
    private String sla;
    private boolean is24x7;
    private double monthlyCharge;

    public VendorBuilder companyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public VendorBuilder contactPersonName(String contactPersonName) {
        this.contactPersonName = contactPersonName;
        return this;
    }

    public VendorBuilder email(String email) {
        this.email = email;
        return this;
    }

    public VendorBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public VendorBuilder address(String address) {
        this.address = address;
        return this;
    }

    public VendorBuilder panNumber(String panNumber) {
        this.panNumber = panNumber;
        return this;
    }

    // Supplier Setters
    public VendorBuilder productCategory(String productCategory) {
        this.productCategory = productCategory;
        return this;
    }

    public VendorBuilder warehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
        return this;
    }

    public VendorBuilder annualSupplyCapacity(double annualSupplyCapacity) {
        this.annualSupplyCapacity = annualSupplyCapacity;
        return this;
    }

    // Contractor Setters
    public VendorBuilder specialisation(String specialisation) {
        this.specialisation = specialisation;
        return this;
    }

    public VendorBuilder teamSize(int teamSize) {
        this.teamSize = teamSize;
        return this;
    }

    public VendorBuilder hasInsurance(boolean hasInsurance) {
        this.hasInsurance = hasInsurance;
        return this;
    }

    public VendorBuilder insurancePolicyNumber(String insurancePolicyNumber) {
        this.insurancePolicyNumber = insurancePolicyNumber;
        return this;
    }

    // ServiceProvider Setters
    public VendorBuilder serviceType(String serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public VendorBuilder sla(String sla) {
        this.sla = sla;
        return this;
    }

    public VendorBuilder is24x7(boolean is24x7) {
        this.is24x7 = is24x7;
        return this;
    }

    public VendorBuilder monthlyCharge(double monthlyCharge) {
        this.monthlyCharge = monthlyCharge;
        return this;
    }

    // Build Methods
    public Supplier buildSupplier() {
        return VendorFactory.createSupplier(companyName, contactPersonName, email, phone, address,
                panNumber, productCategory, warehouseLocation, annualSupplyCapacity);
    }

    public Contractor buildContractor() {
        return VendorFactory.createContractor(companyName, contactPersonName, email, phone, address,
                panNumber, specialisation, teamSize, hasInsurance, insurancePolicyNumber);
    }

    public ServiceProvider buildServiceProvider() {
        return VendorFactory.createServiceProvider(companyName, contactPersonName, email, phone, address,
                panNumber, serviceType, sla, is24x7, monthlyCharge);
    }
}
