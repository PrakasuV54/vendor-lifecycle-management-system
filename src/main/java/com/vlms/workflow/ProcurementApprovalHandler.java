package com.vlms.workflow;

import com.vlms.enums.UserRole;
import com.vlms.interfaces.ApprovalHandler;
import com.vlms.model.ApprovalRequest;
import com.vlms.util.ConsoleLogger;

/**
 * Level 1 Approval Handler in the Chain of Responsibility.
 * Handles Procurement-level reviews.
 */
public class ProcurementApprovalHandler implements ApprovalHandler {

    private ApprovalHandler next;

    @Override
    public void setNext(ApprovalHandler next) {
        this.next = next;
    }

    @Override
    public void handle(ApprovalRequest request) {
        ConsoleLogger.info("  [CoR Chain] ProcurementApprovalHandler examining request for " + request.getEntityType() + " " + request.getEntityId());

        if (request.getAction().equals("REJECT")) {
            request.setApproved(false);
            request.setFinished(true);
            request.setFinalStatus("REJECTED");
            ConsoleLogger.warn("  [CoR Chain] Request REJECTED at Procurement level.");
            return;
        }

        if (request.getAction().equals("REQUEST_CHANGES")) {
            request.setApproved(false);
            request.setFinished(true);
            request.setFinalStatus("CHANGES_REQUESTED");
            ConsoleLogger.warn("  [CoR Chain] Changes requested at Procurement level.");
            return;
        }

        if (request.getAction().equals("DELEGATE")) {
            request.setFinished(true);
            request.setFinalStatus("DELEGATED");
            return;
        }

        if (request.getAction().equals("ESCALATE")) {
            request.setFinalStatus("ESCALATED");
            request.setNextGroup("Admin Group");
            if (next != null) {
                next.handle(request);
            } else {
                request.setFinished(true);
            }
            return;
        }

        // Action is APPROVE
        if ("VENDOR".equalsIgnoreCase(request.getEntityType()) || "CONTRACT".equalsIgnoreCase(request.getEntityType())) {
            if (request.getUserRole() == UserRole.PROCUREMENT_MANAGER || request.getUserRole() == UserRole.ADMIN) {
                ConsoleLogger.info("  [CoR Chain] Procurement level APPROVED. Passing to next level.");
                if (next != null) {
                    next.handle(request);
                } else {
                    request.setApproved(true);
                    request.setFinished(true);
                    request.setFinalStatus("APPROVED");
                }
            } else {
                // Not authorized or invalid role
                if (next != null) {
                    next.handle(request);
                } else {
                    request.setFinished(true);
                }
            }
        } else {
            // Invoices and Payments bypass Procurement approval directly to Finance
            if (next != null) {
                next.handle(request);
            } else {
                request.setFinished(true);
            }
        }
    }
}
