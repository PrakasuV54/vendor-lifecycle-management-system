package com.vlms.model;

import java.util.HashMap;
import java.util.Map;

/**
 * ProcessContext models Appian Process Variables (pv!).
 * Stores state, business entities, routing decisions, and execution metadata.
 */
public class ProcessContext {

    private Vendor vendor;
    private Contract contract;
    private Invoice invoice;
    private Payment payment;
    
    private String currentStage;
    private String approver;
    private String comments;
    private int approvalCount;
    private double riskScore;
    
    private Task currentTask;
    private String currentGroup;
    private String rejectionReason;
    private int escalationCount;
    private double completionPercentage;

    private final Map<String, Object> processVariables = new HashMap<>();

    public void setVariable(String key, Object value) {
        processVariables.put(key, value);
    }

    public Object getVariable(String key) {
        return processVariables.get(key);
    }

    // --- Strongly-typed Getters and Setters ---

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public int getApprovalCount() { return approvalCount; }
    public void setApprovalCount(int approvalCount) { this.approvalCount = approvalCount; }
    public void incrementApprovalCount() { this.approvalCount++; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    public Task getCurrentTask() { return currentTask; }
    public void setCurrentTask(Task currentTask) { this.currentTask = currentTask; }

    public String getCurrentGroup() { return currentGroup; }
    public void setCurrentGroup(String currentGroup) { this.currentGroup = currentGroup; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public int getEscalationCount() { return escalationCount; }
    public void setEscalationCount(int escalationCount) { this.escalationCount = escalationCount; }
    public void incrementEscalationCount() { this.escalationCount++; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }

    @Override
    public String toString() {
        return String.format("ProcessContext[Stage: %s | Vendor: %s | Task: %s | Approvals: %d | Progress: %.1f%%]",
                currentStage, vendor != null ? vendor.getVendorId() : "None",
                currentTask != null ? currentTask.getTaskId() : "None",
                approvalCount, completionPercentage);
    }
}
