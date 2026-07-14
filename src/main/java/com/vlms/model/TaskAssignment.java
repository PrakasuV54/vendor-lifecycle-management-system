package com.vlms.model;

import java.time.LocalDateTime;

/**
 * TaskAssignment tracks who was assigned a task and when.
 */
public class TaskAssignment {

    private final String taskId;
    private final String userId;
    private final String groupName;
    private final LocalDateTime assignedAt;

    public TaskAssignment(String taskId, String userId, String groupName) {
        this.taskId = taskId;
        this.userId = userId;
        this.groupName = groupName;
        this.assignedAt = LocalDateTime.now();
    }

    public String getTaskId() { return taskId; }
    public String getUserId() { return userId; }
    public String getGroupName() { return groupName; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
}
