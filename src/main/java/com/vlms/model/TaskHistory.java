package com.vlms.model;

import com.vlms.enums.TaskStatus;
import java.time.LocalDateTime;

/**
 * TaskHistory logs state transitions of a human task.
 */
public class TaskHistory {

    private final LocalDateTime timestamp;
    private final TaskStatus oldState;
    private final TaskStatus newState;
    private final String details;

    public TaskHistory(LocalDateTime timestamp, TaskStatus oldState, TaskStatus newState, String details) {
        this.timestamp = timestamp;
        this.oldState = oldState;
        this.newState = newState;
        this.details = details;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public TaskStatus getOldState() { return oldState; }
    public TaskStatus getNewState() { return newState; }
    public String getDetails() { return details; }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s | %s", timestamp, oldState, newState, details);
    }
}
