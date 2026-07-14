package com.vlms.service;

import com.vlms.event.VendorEvent;
import com.vlms.interfaces.EventListener;
import com.vlms.model.AuditEntry;
import com.vlms.repository.AuditRepository;
import com.vlms.util.ConsoleLogger;

import java.util.List;

/**
 * AuditService: processes events and records audit history in-memory.
 * Implements EventListener (Observer Pattern).
 */
public class AuditService implements EventListener {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void log(String userId, String userRole, String action, String entityId,
                    String entityType, String oldState, String newState, String remarks) {
        AuditEntry entry = new AuditEntry(userId, userRole, action, entityId, entityType, oldState, newState, remarks);
        auditRepository.save(entry);
        ConsoleLogger.info("  [Audit Trail] Recorded audit event: " + action + " for " + entityType + " " + entityId);
    }

    @Override
    public void onEvent(VendorEvent event) {
        // Parse event parameters and log them
        String userId = (String) event.getParam("userId");
        String userRole = (String) event.getParam("userRole");
        String action = event.getEventType();
        String entityId = (String) event.getParam("entityId");
        String entityType = (String) event.getParam("entityType");
        String oldState = (String) event.getParam("oldState");
        String newState = (String) event.getParam("newState");
        String remarks = (String) event.getParam("remarks");

        if (userId == null) userId = "SYSTEM";
        if (entityId == null) entityId = "N/A";
        if (entityType == null) entityType = "Event";

        log(userId, userRole, action, entityId, entityType, oldState, newState, remarks);
    }

    public List<AuditEntry> getAllLogs() {
        return auditRepository.findAll();
    }
}
