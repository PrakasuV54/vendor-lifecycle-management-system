package com.vlms.model;

import java.time.LocalDateTime;

/**
 * Immutable log record of an event in a process instance execution.
 * Direct equivalent of Appian Process History.
 */
public class WorkflowHistoryEntry {

    private final LocalDateTime timestamp;
    private final String userId;
    private final String userRole;
    private final String action;
    private final String previousStage;
    private final String nextStage;
    private final String remarks;

    public WorkflowHistoryEntry(String userId, String userRole, String action,
                                String previousStage, String nextStage, String remarks) {
        this.timestamp = LocalDateTime.now();
        this.userId = userId != null ? userId : "SYSTEM";
        this.userRole = userRole != null ? userRole : "SYSTEM";
        this.action = action;
        this.previousStage = previousStage != null ? previousStage : "START";
        this.nextStage = nextStage != null ? nextStage : "N/A";
        this.remarks = remarks != null ? remarks : "";
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
    public String getUserRole() { return userRole; }
    public String getAction() { return action; }
    public String getPreviousStage() { return previousStage; }
    public String getNextStage() { return nextStage; }
    public String getRemarks() { return remarks; }

    @Override
    public String toString() {
        return String.format("[%s] Action: %s | User: %s (%s) | Stage: %s -> %s | Remarks: %s",
                timestamp, action, userId, userRole, previousStage, nextStage, remarks);
    }
}
