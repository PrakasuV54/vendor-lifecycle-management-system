package com.vlms.enums;

/**
 * Appian-style Process Instance execution states.
 */
public enum WorkflowState {
    CREATED,
    RUNNING,
    WAITING_FOR_USER,
    COMPLETED,
    FAILED,
    CANCELLED
}
