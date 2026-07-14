package com.vlms.model;

import com.vlms.enums.VendorType;

/**
 * Supplier vendor: primarily provides physical goods and raw materials.
 * Demonstrates Inheritance and Liskov Substitution.
 */
public class Supplier extends Vendor {

    private final String productCategory;   // e.g., "Electronics", "Raw Materials"
    private final String warehouseLocation;
    private double annualSupplyCapacity;    // in units

    public Supplier(String companyName, String contactPersonName, String email,
                    String phone, String address, String panNumber,
                    String productCategory, String warehouseLocation, double annualSupplyCapacity) {
        super(companyName, contactPersonName, email, phone, address, VendorType.SUPPLIER, panNumber);
        this.productCategory = productCategory;
        this.warehouseLocation = warehouseLocation;
        this.annualSupplyCapacity = annualSupplyCapacity;
    }

    @Override
    public String getVendorCategory() {
        return "Supplier";
    }

    @Override
    public String getCategorySpecificDetails() {
        return String.format("Product Category: %s | Warehouse: %s | Capacity: %.0f units/year",
                productCategory, warehouseLocation, annualSupplyCapacity);
    }

    public void updateCapacity(double newCapacity) {
        if (newCapacity <= 0) throw new IllegalArgumentException("Capacity must be positive.");
        this.annualSupplyCapacity = newCapacity;
    }

    public String getProductCategory() { return productCategory; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public double getAnnualSupplyCapacity() { return annualSupplyCapacity; }

    @Override
    public String toString() {
        return "Supplier[" + getVendorId() + " | " + getCompanyName() + " | " + productCategory + " | Status: " + getStatus() + "]";
    }
}
