package com.vlms.workflow;

import com.vlms.enums.RiskLevel;
import com.vlms.enums.VendorStatus;
import com.vlms.enums.VendorType;
import com.vlms.factory.VendorBuilder;
import com.vlms.model.Supplier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VendorBuilderTest {

    @Test
    void testVendorBuilderCreation() {
        Supplier vendor = new VendorBuilder()
                .companyName("Test Corp")
                .contactPersonName("John Doe")
                .email("test@corp.com")
                .phone("1234567890")
                .address("123 Tech Lane")
                .panNumber("ABCDE1234F")
                .productCategory("Electronics")
                .warehouseLocation("Bangalore")
                .annualSupplyCapacity(10000)
                .buildSupplier();

        assertNotNull(vendor.getVendorId());
        assertEquals("Test Corp", vendor.getCompanyName());
        assertEquals("test@corp.com", vendor.getEmail());
        assertEquals("1234567890", vendor.getPhone());
        assertEquals("123 Tech Lane", vendor.getAddress());
        assertEquals(VendorType.SUPPLIER, vendor.getVendorType());
        assertEquals(RiskLevel.LOW, vendor.getCurrentRiskLevel());
        assertEquals(VendorStatus.PENDING, vendor.getStatus());
    }

    @Test
    void testVendorBuilderMissingRequiredFieldThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new VendorBuilder()
                    .email("no-company@corp.com")
                    .buildSupplier();
        });
    }
}
