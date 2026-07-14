package com.vlms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines the process model structure (Appian Process Model).
 */
public class WorkflowDefinition {

    private final String processDefinitionId;
    private final String processName;
    private final List<WorkflowStep> steps;

    public WorkflowDefinition(String processDefinitionId, String processName) {
        this.processDefinitionId = processDefinitionId;
        this.processName = processName;
        this.steps = new ArrayList<>();
    }

    public void addStep(WorkflowStep step) {
        steps.add(step);
    }

    public String getProcessDefinitionId() { return processDefinitionId; }
    public String getProcessName() { return processName; }
    public List<WorkflowStep> getSteps() { return Collections.unmodifiableList(steps); }
}
