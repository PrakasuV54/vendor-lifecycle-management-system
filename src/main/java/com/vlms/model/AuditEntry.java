package com.vlms.model;

import com.vlms.util.IdGenerator;
import java.time.LocalDateTime;

/**
 * Immutable log record for enterprise audit logs.
 */
public class AuditEntry {

    private final String auditId;
    private final LocalDateTime timestamp;
    private final String userId;
    private final String userRole;
    private final String action;
    private final String entityId;
    private final String entityType;
    private final String oldState;
    private final String newState;
    private final String remarks;

    public AuditEntry(String userId, String userRole, String action, String entityId,
                      String entityType, String oldState, String newState, String remarks) {
        this.auditId = IdGenerator.generateAuditId();
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.userRole = userRole != null ? userRole : "SYSTEM";
        this.action = action;
        this.entityId = entityId;
        this.entityType = entityType;
        this.oldState = oldState != null ? oldState : "N/A";
        this.newState = newState != null ? newState : "N/A";
        this.remarks = remarks != null ? remarks : "";
    }

    public String getAuditId() { return auditId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
    public String getUserRole() { return userRole; }
    public String getAction() { return action; }
    public String getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public String getOldState() { return oldState; }
    public String getNewState() { return newState; }
    public String getRemarks() { return remarks; }

    @Override
    public String toString() {
        return String.format("[%s] Audit: %s | User: %s (%s) | Entity: %s (%s) | State: %s -> %s | Remarks: %s",
                timestamp, action, userId, userRole, entityId, entityType, oldState, newState, remarks);
    }
}
