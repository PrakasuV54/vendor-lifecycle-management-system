package com.vlms.repository;

import com.vlms.model.AuditEntry;
import java.util.List;

/**
 * Interface definition for Audit persistent storage.
 */
public interface AuditRepository {
    void save(AuditEntry entry);
    List<AuditEntry> findAll();
    List<AuditEntry> findByEntityId(String entityId);
    List<AuditEntry> findByUserId(String userId);
}
