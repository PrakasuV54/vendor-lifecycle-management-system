package com.vlms.repository;

import com.vlms.model.AuditEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for audit entries.
 */
public class InMemoryAuditRepository implements AuditRepository {

    private final List<AuditEntry> auditList = new ArrayList<>();

    @Override
    public void save(AuditEntry entry) {
        auditList.add(entry);
    }

    @Override
    public List<AuditEntry> findAll() {
        return new ArrayList<>(auditList);
    }

    @Override
    public List<AuditEntry> findByEntityId(String entityId) {
        return auditList.stream()
                .filter(e -> entityId.equals(e.getEntityId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditEntry> findByUserId(String userId) {
        return auditList.stream()
                .filter(e -> userId.equals(e.getUserId()))
                .collect(Collectors.toList());
    }
}
