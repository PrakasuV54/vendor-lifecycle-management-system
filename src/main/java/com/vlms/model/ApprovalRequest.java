package com.vlms.model;

import com.vlms.enums.UserRole;

/**
 * Request details passed through the Chain of Responsibility approval routing.
 */
public class ApprovalRequest {

    private final String entityId;
    private final String entityType; // "VENDOR", "CONTRACT", "INVOICE", "PAYMENT"
    private final double value;
    private final String action; // "APPROVE", "REJECT", "REQUEST_CHANGES", "DELEGATE", "ESCALATE"
    private final String userId;
    private final UserRole userRole;
    private final String remarks;

    // Output routing decisions
    private boolean isApproved;
    private boolean isFinished;
    private String nextGroup;
    private String nextAssignee;
    private String finalStatus; // "APPROVED", "REJECTED", "CHANGES_REQUESTED", "DELEGATED", "ESCALATED"

    public ApprovalRequest(String entityId, String entityType, double value, String action,
                           String userId, UserRole userRole, String remarks) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.value = value;
        this.action = action;
        this.userId = userId;
        this.userRole = userRole;
        this.remarks = remarks;
        
        this.isApproved = false;
        this.isFinished = false;
    }

    public String getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public double getValue() { return value; }
    public String getAction() { return action; }
    public String getUserId() { return userId; }
    public UserRole getUserRole() { return userRole; }
    public String getRemarks() { return remarks; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public String getNextGroup() { return nextGroup; }
    public void setNextGroup(String nextGroup) { this.nextGroup = nextGroup; }

    public String getNextAssignee() { return nextAssignee; }
    public void setNextAssignee(String nextAssignee) { this.nextAssignee = nextAssignee; }

    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }
}
