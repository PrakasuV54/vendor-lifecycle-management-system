package com.vlms.workflow;

import com.vlms.enums.UserRole;
import com.vlms.interfaces.ApprovalHandler;
import com.vlms.model.ApprovalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChainOfResponsibilityTest {

    private ApprovalHandler procurementHandler;
    private ApprovalHandler financeHandler;
    private ApprovalHandler adminHandler;

    @BeforeEach
    void setUp() {
        procurementHandler = new ProcurementApprovalHandler();
        financeHandler = new FinanceApprovalHandler();
        adminHandler = new AdminApprovalHandler();

        procurementHandler.setNext(financeHandler);
        financeHandler.setNext(adminHandler);
    }

    @Test
    void testProcurementManagerApproveVendor() {
        ApprovalRequest request = new ApprovalRequest(
                "VND-100", "VENDOR", 0.0, "APPROVE",
                "USR-1", UserRole.PROCUREMENT_MANAGER, "Procurement check passed"
        );
        procurementHandler.handle(request);

        // Procurement manager cannot grant final approval for vendor onboarding
        // because it must go to the Admin level, but there is no next handler in this request chain
        // that matches since the user role is PROCUREMENT_MANAGER.
        assertFalse(request.isApproved());
        assertTrue(request.isFinished());
    }

    @Test
    void testAdminApproveVendor() {
        ApprovalRequest request = new ApprovalRequest(
                "VND-100", "VENDOR", 0.0, "APPROVE",
                "USR-2", UserRole.ADMIN, "Admin final sign-off"
        );
        procurementHandler.handle(request);

        // Admin has permissions for all levels and is final gatekeeper
        assertTrue(request.isApproved());
        assertTrue(request.isFinished());
        assertEquals("APPROVED", request.getFinalStatus());
    }

    @Test
    void testProcurementRejectVendor() {
        ApprovalRequest request = new ApprovalRequest(
                "VND-100", "VENDOR", 0.0, "REJECT",
                "USR-1", UserRole.PROCUREMENT_MANAGER, "Unreliable vendor"
        );
        procurementHandler.handle(request);

        assertFalse(request.isApproved());
        assertTrue(request.isFinished());
        assertEquals("REJECTED", request.getFinalStatus());
    }

    @Test
    void testProcurementEscalateVendor() {
        ApprovalRequest request = new ApprovalRequest(
                "VND-100", "VENDOR", 0.0, "ESCALATE",
                "USR-1", UserRole.PROCUREMENT_MANAGER, "Needs admin review"
        );
        procurementHandler.handle(request);

        assertEquals("ESCALATED", request.getFinalStatus());
        assertEquals("Admin Group", request.getNextGroup());
    }

    @Test
    void testFinanceManagerApproveInvoiceLowValue() {
        // Finance approval of low value invoice
        ApprovalRequest request = new ApprovalRequest(
                "INV-100", "INVOICE", 50000.0, "APPROVE",
                "USR-3", UserRole.FINANCE_MANAGER, "Low value invoice approved"
        );
        // Direct handling by finance without admin level next
        ApprovalHandler standaloneFinance = new FinanceApprovalHandler();
        standaloneFinance.handle(request);

        // Low value invoice (<5L) doesn't require Admin dual approval
        assertTrue(request.isApproved());
        assertTrue(request.isFinished());
        assertEquals("APPROVED", request.getFinalStatus());
    }

    @Test
    void testFinanceManagerApproveInvoiceHighValue() {
        // Finance approval of high value invoice
        ApprovalRequest request = new ApprovalRequest(
                "INV-101", "INVOICE", 600000.0, "APPROVE",
                "USR-3", UserRole.FINANCE_MANAGER, "High value invoice check"
        );
        // Direct handling by finance
        financeHandler.handle(request);

        // High value invoice requires Admin dual approval (next level), so Finance alone is not enough
        assertFalse(request.isApproved());
        assertTrue(request.isFinished());
    }

    @Test
    void testAdminApproveInvoiceHighValue() {
        // Admin approval of high value invoice
        ApprovalRequest request = new ApprovalRequest(
                "INV-101", "INVOICE", 600000.0, "APPROVE",
                "USR-2", UserRole.ADMIN, "High value admin approval"
        );
        financeHandler.handle(request);

        assertTrue(request.isApproved());
        assertTrue(request.isFinished());
        assertEquals("APPROVED", request.getFinalStatus());
    }
}
