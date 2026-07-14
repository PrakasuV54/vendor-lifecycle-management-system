package com.vlms.interfaces;

/**
 * Marker interface for entities that can be included in system reports.
 * Demonstrates Interface Segregation: only report-capable entities implement this.
 */
public interface Reportable {

    /**
     * Returns a formatted summary of this entity for reporting purposes.
     */
    String toReportSummary();
}
