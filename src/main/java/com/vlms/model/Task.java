package com.vlms.model;

import com.vlms.enums.TaskPriority;
import com.vlms.enums.TaskStatus;
import com.vlms.enums.TaskType;
import com.vlms.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Task models a human task in an Appian Process Model.
 * Tracks assignment, state transitions, SLAs, and escalation history.
 */
public class Task implements Comparable<Task> {

    private final String taskId;
    private final String title;
    private final String description;
    private String assignee; // User ID
    private String assignedGroup; // E.g., "Procurement Group", "Finance Group", "Admin Group"
    private final TaskPriority priority;
    private TaskStatus status;
    private final TaskType type;
    
    private final LocalDateTime createdDate;
    private LocalDateTime dueDate;
    private LocalDateTime reminderDate;
    private LocalDateTime escalationDate;
    
    private final String workflowInstanceId;
    private final String relatedEntityId;
    private String remarks;

    private boolean isEscalated;
    private final List<String> escalationHistory;
    private final List<TaskHistory> taskHistory;

    public Task(String title, String description, String assignedGroup, TaskPriority priority,
                TaskType type, String workflowInstanceId, String relatedEntityId, int slaDurationHours) {
        this.taskId = IdGenerator.generateTaskId();
        this.title = title;
        this.description = description;
        this.assignedGroup = assignedGroup;
        this.priority = priority;
        this.status = TaskStatus.CREATED;
        this.type = type;
        this.createdDate = LocalDateTime.now();
        this.dueDate = this.createdDate.plusHours(slaDurationHours);
        // Set reminder 2 hours before due, and escalation 4 hours after due (or similar default hours)
        this.reminderDate = this.dueDate.minusHours(Math.max(1, slaDurationHours / 4));
        this.escalationDate = this.dueDate;
        
        this.workflowInstanceId = workflowInstanceId;
        this.relatedEntityId = relatedEntityId;
        this.isEscalated = false;
        
        this.escalationHistory = new ArrayList<>();
        this.taskHistory = new ArrayList<>();
        
        recordHistory(null, TaskStatus.CREATED, "Task created in group " + assignedGroup);
    }

    public void assign(String assigneeId) {
        this.assignee = assigneeId;
        this.status = TaskStatus.ASSIGNED;
        recordHistory(TaskStatus.CREATED, TaskStatus.ASSIGNED, "Assigned to user " + assigneeId);
    }

    public void claim(String userId) {
        this.assignee = userId;
        this.status = TaskStatus.CLAIMED;
        recordHistory(TaskStatus.ASSIGNED, TaskStatus.CLAIMED, "Claimed by user " + userId);
    }

    public void startProgress() {
        this.status = TaskStatus.IN_PROGRESS;
        recordHistory(TaskStatus.CLAIMED, TaskStatus.IN_PROGRESS, "Task in progress");
    }

    public void complete(String remarks) {
        TaskStatus old = this.status;
        this.status = TaskStatus.COMPLETED;
        this.remarks = remarks;
        recordHistory(old, TaskStatus.COMPLETED, "Completed: " + remarks);
    }

    public void cancel(String remarks) {
        TaskStatus old = this.status;
        this.status = TaskStatus.CANCELLED;
        this.remarks = remarks;
        recordHistory(old, TaskStatus.CANCELLED, "Cancelled: " + remarks);
    }

    public void escalate(String reason) {
        this.isEscalated = true;
        this.status = TaskStatus.ESCALATED;
        this.escalationHistory.add(LocalDateTime.now() + ": " + reason);
        recordHistory(this.status, TaskStatus.ESCALATED, "Escalated: " + reason);
    }

    public void reassignGroup(String newGroup, String reason) {
        String oldGroup = this.assignedGroup;
        this.assignedGroup = newGroup;
        recordHistory(this.status, this.status, "Group changed from " + oldGroup + " to " + newGroup + ". Reason: " + reason);
    }

    private void recordHistory(TaskStatus oldState, TaskStatus newState, String details) {
        this.taskHistory.add(new TaskHistory(LocalDateTime.now(), oldState, newState, details));
    }

    // --- Getters and Setters ---

    public String getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getAssignedGroup() { return assignedGroup; }
    public TaskPriority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public TaskType getType() { return type; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getReminderDate() { return reminderDate; }
    public void setReminderDate(LocalDateTime reminderDate) { this.reminderDate = reminderDate; }
    public LocalDateTime getEscalationDate() { return escalationDate; }
    public void setEscalationDate(LocalDateTime escalationDate) { this.escalationDate = escalationDate; }
    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public String getRelatedEntityId() { return relatedEntityId; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public boolean isEscalated() { return isEscalated; }
    public List<String> getEscalationHistory() { return Collections.unmodifiableList(escalationHistory); }
    public List<TaskHistory> getTaskHistory() { return Collections.unmodifiableList(taskHistory); }

    @Override
    public int compareTo(Task o) {
        // Compare by priority first (HIGH > MEDIUM > LOW), then by dueDate
        int priorityCompare = o.priority.compareTo(this.priority);
        if (priorityCompare != 0) return priorityCompare;
        return this.dueDate.compareTo(o.dueDate);
    }

    @Override
    public String toString() {
        return String.format("Task[%s | %s | Status: %s | Group: %s | Assignee: %s]",
                taskId, title, status, assignedGroup, assignee != null ? assignee : "Unassigned");
    }
}
