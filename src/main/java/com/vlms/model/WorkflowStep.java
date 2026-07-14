package com.vlms.model;

/**
 * Represents a single node/step in an Appian Process Model.
 * Can be automated (script task) or human (user task).
 */
public abstract class WorkflowStep {

    private final String name;
    private final boolean isHumanTask;

    protected WorkflowStep(String name, boolean isHumanTask) {
        this.name = name;
        this.isHumanTask = isHumanTask;
    }

    public String getName() { return name; }
    public boolean isHumanTask() { return isHumanTask; }

    /**
     * Executes the step in the context of a process instance.
     * @return true if the step completes immediately (automated).
     *         false if the workflow should pause (waiting for user input).
     */
    public abstract boolean execute(WorkflowInstance instance, String callerUserId);
}
