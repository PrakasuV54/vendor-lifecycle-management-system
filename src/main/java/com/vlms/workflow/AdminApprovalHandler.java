package com.vlms.workflow;

import com.vlms.enums.UserRole;
import com.vlms.interfaces.ApprovalHandler;
import com.vlms.model.ApprovalRequest;
import com.vlms.util.ConsoleLogger;

/**
 * Level 3 / Admin Approval Handler in the Chain of Responsibility.
 * Final gatekeeper for onboarding and high-value objects.
 */
public class AdminApprovalHandler implements ApprovalHandler {

    private ApprovalHandler next;

    @Override
    public void setNext(ApprovalHandler next) {
        this.next = next;
    }

    @Override
    public void handle(ApprovalRequest request) {
        ConsoleLogger.info("  [CoR Chain] AdminApprovalHandler examining request for " + request.getEntityType() + " " + request.getEntityId());

        if (request.getAction().equals("REJECT")) {
            request.setApproved(false);
            request.setFinished(true);
            request.setFinalStatus("REJECTED");
            ConsoleLogger.warn("  [CoR Chain] Request REJECTED at Admin level.");
            return;
        }

        if (request.getAction().equals("REQUEST_CHANGES")) {
            request.setApproved(false);
            request.setFinished(true);
            request.setFinalStatus("CHANGES_REQUESTED");
            ConsoleLogger.warn("  [CoR Chain] Changes requested at Admin level.");
            return;
        }

        // Action is APPROVE
        if (request.getUserRole() == UserRole.ADMIN) {
            ConsoleLogger.info("  [CoR Chain] Admin final approval GRANTED.");
            request.setApproved(true);
            request.setFinished(true);
            request.setFinalStatus("APPROVED");
        } else {
            // Not Admin and is final stage -> fail or pass to next (if any)
            if (next != null) {
                next.handle(request);
            } else {
                // Stop, cannot grant final approval if user is not Admin
                request.setFinished(true);
            }
        }
    }
}
