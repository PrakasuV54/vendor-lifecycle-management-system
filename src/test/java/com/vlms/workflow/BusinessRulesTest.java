package com.vlms.workflow;

import com.vlms.model.Contract;
import com.vlms.model.Invoice;
import com.vlms.rules.ContractRules;
import com.vlms.rules.InvoiceRules;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BusinessRulesTest {

    @Test
    void testContractRequiresFinanceApproval() {
        // Threshold is 10 Lakhs (1,000,000)
        Contract lowValue = new Contract("VND-1", "Low Value Agreement", "desc", 800000.0, LocalDate.now(), LocalDate.now().plusMonths(6));
        Contract highValue = new Contract("VND-1", "High Value Agreement", "desc", 1200000.0, LocalDate.now(), LocalDate.now().plusMonths(6));

        assertFalse(ContractRules.requiresFinanceApproval(lowValue));
        assertTrue(ContractRules.requiresFinanceApproval(highValue));
    }

    @Test
    void testInvoiceRequiresDualApproval() {
        // Threshold is 5 Lakhs (500,000)
        Invoice lowInvoice = new Invoice("VND-1", "CTR-1", "low invoice", 300000.0, LocalDate.now(), LocalDate.now().plusDays(30));
        Invoice highInvoice = new Invoice("VND-1", "CTR-1", "high invoice", 700000.0, LocalDate.now(), LocalDate.now().plusDays(30));

        assertFalse(InvoiceRules.requiresDualApproval(lowInvoice));
        assertTrue(InvoiceRules.requiresDualApproval(highInvoice));
    }
}
