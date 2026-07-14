package com.vlms.workflow;

import com.vlms.enums.UserRole;
import com.vlms.interfaces.ApprovalHandler;
import com.vlms.model.ApprovalRequest;
import com.vlms.util.ConsoleLogger;

/**
 * Level 2 Approval Handler in the Chain of Responsibility.
 * Handles Finance-level reviews.
 */
public class FinanceApprovalHandler implements ApprovalHandler {

    private ApprovalHandler next;

    @Override
    public void setNext(ApprovalHandler next) {
        this.next = next;
    }

    @Override
    public void handle(ApprovalRequest request) {
        ConsoleLogger.info("  [CoR Chain] FinanceApprovalHandler examining request for " + request.getEntityType() + " " + request.getEntityId());

        if (request.getAction().equals("REJECT")) {
            request.setApproved(false);
            request.setFinished(true);
            request.setFinalStatus("REJECTED");
            ConsoleLogger.warn("  [CoR Chain] Request REJECTED at Finance level.");
            return;
        }

        if (request.getAction().equals("REQUEST_CHANGES")) {
            request.setApproved(false);
            request.setFinished(true);
            request.setFinalStatus("CHANGES_REQUESTED");
            ConsoleLogger.warn("  [CoR Chain] Changes requested at Finance level.");
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
        if ("INVOICE".equalsIgnoreCase(request.getEntityType()) || "PAYMENT".equalsIgnoreCase(request.getEntityType()) || "CONTRACT".equalsIgnoreCase(request.getEntityType())) {
            if (request.getUserRole() == UserRole.FINANCE_MANAGER || request.getUserRole() == UserRole.ADMIN) {
                ConsoleLogger.info("  [CoR Chain] Finance level APPROVED. Passing to next level.");
                if (next != null) {
                    next.handle(request);
                } else {
                    request.setApproved(true);
                    request.setFinished(true);
                    request.setFinalStatus("APPROVED");
                }
            } else {
                if (next != null) {
                    next.handle(request);
                } else {
                    request.setFinished(true);
                }
            }
        } else {
            // Vendor onboarding passes through Finance to Admin
            if (next != null) {
                next.handle(request);
            } else {
                request.setFinished(true);
            }
        }
    }
}
