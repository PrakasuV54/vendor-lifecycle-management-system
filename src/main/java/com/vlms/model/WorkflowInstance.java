package com.vlms.model;

import com.vlms.enums.WorkflowState;
import com.vlms.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Models an active process instance (Appian Process Instance).
 * Maintains state, execution history, and process variables (ProcessContext).
 */
public class WorkflowInstance {

    private final String workflowInstanceId;
    private final WorkflowDefinition definition;
    private int currentStepIndex;
    private WorkflowState state;
    
    private final ProcessContext context;
    private final List<WorkflowHistoryEntry> history;
    
    private final String initiatedBy;
    private final LocalDateTime initiatedAt;
    private LocalDateTime endedAt;

    public WorkflowInstance(WorkflowDefinition definition, String initiatedBy) {
        this.workflowInstanceId = IdGenerator.generateWorkflowId();
        this.definition = definition;
        this.currentStepIndex = 0;
        this.state = WorkflowState.CREATED;
        this.context = new ProcessContext();
        this.history = new ArrayList<>();
        this.initiatedBy = initiatedBy;
        this.initiatedAt = LocalDateTime.now();
        
        recordHistory(initiatedBy, "SYSTEM", "Initiated workflow instance", null, "CREATED", "Process started");
    }

    public void recordHistory(String userId, String userRole, String action,
                              String previousStage, String nextStage, String remarks) {
        this.history.add(new WorkflowHistoryEntry(userId, userRole, action, previousStage, nextStage, remarks));
    }

    public void complete(String userId, String userRole) {
        this.state = WorkflowState.COMPLETED;
        this.endedAt = LocalDateTime.now();
        recordHistory(userId, userRole, "WORKFLOW_COMPLETED",
                definition.getSteps().get(definition.getSteps().size() - 1).getName(), "COMPLETED", "Process successfully finished");
    }

    public void fail(String reason, String userId, String userRole) {
        this.state = WorkflowState.FAILED;
        this.endedAt = LocalDateTime.now();
        String currentStepName = currentStepIndex < definition.getSteps().size() ?
                definition.getSteps().get(currentStepIndex).getName() : "END";
        recordHistory(userId, userRole, "WORKFLOW_FAILED", currentStepName, "FAILED", reason);
    }

    public void cancel(String reason, String userId, String userRole) {
        this.state = WorkflowState.CANCELLED;
        this.endedAt = LocalDateTime.now();
        String currentStepName = currentStepIndex < definition.getSteps().size() ?
                definition.getSteps().get(currentStepIndex).getName() : "END";
        recordHistory(userId, userRole, "WORKFLOW_CANCELLED", currentStepName, "CANCELLED", reason);
    }

    // --- Getters & Setters ---

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public WorkflowDefinition getDefinition() { return definition; }
    public int getCurrentStepIndex() { return currentStepIndex; }
    public void setCurrentStepIndex(int currentStepIndex) { this.currentStepIndex = currentStepIndex; }
    public void incrementStepIndex() { this.currentStepIndex++; }
    public WorkflowState getState() { return state; }
    public void setState(WorkflowState state) { this.state = state; }
    public ProcessContext getContext() { return context; }
    public List<WorkflowHistoryEntry> getHistory() { return Collections.unmodifiableList(history); }
    public String getInitiatedBy() { return initiatedBy; }
    public LocalDateTime getInitiatedAt() { return initiatedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }

    @Override
    public String toString() {
        return String.format("WorkflowInstance[%s | %s | State: %s | Step: %d/%d]",
                workflowInstanceId, definition.getProcessName(), state, currentStepIndex + 1, definition.getSteps().size());
    }
}
