package com.vlms.service;

import com.vlms.enums.*;
import com.vlms.model.*;
import com.vlms.repository.*;
import com.vlms.workflow.WorkflowEngine;
import com.vlms.util.ConsoleLogger;

import java.time.Duration;
import java.util.List;

/**
 * DashboardService generates enterprise-level metrics and reports.
 * Direct equivalent of Appian Sites Dashboards.
 */
public class DashboardService {

    private final VendorRepository vendorRepository;
    private final TaskRepository taskRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final WorkflowEngine workflowEngine;

    public DashboardService(VendorRepository vendorRepository, TaskRepository taskRepository,
                            ContractRepository contractRepository, InvoiceRepository invoiceRepository,
                            PaymentRepository paymentRepository, WorkflowEngine workflowEngine) {
        this.vendorRepository = vendorRepository;
        this.taskRepository = taskRepository;
        this.contractRepository = contractRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.workflowEngine = workflowEngine;
    }

    /**
     * Prints a professional dashboard layout on the console.
     */
    public void displayDashboard() {
        List<Vendor> vendors = vendorRepository.findAll();
        List<Task> tasks = taskRepository.findAll();
        List<Contract> contracts = contractRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        List<WorkflowInstance> workflows = workflowEngine.getAllInstances();

        long totalVendors = vendors.size();
        long pendingVendors = vendors.stream().filter(v -> v.getStatus() == VendorStatus.PENDING).count();
        long activeVendors = vendors.stream().filter(v -> v.getStatus() == VendorStatus.ACTIVE).count();
        long suspendedVendors = vendors.stream().filter(v -> v.getStatus() == VendorStatus.SUSPENDED).count();

        long pendingTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.ASSIGNED || t.getStatus() == TaskStatus.CREATED).count();
        long claimedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.CLAIMED || t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long overdueTasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.ESCALATED || t.isEscalated()).count();

        long highRiskVendors = vendors.stream().filter(v -> v.getCurrentRiskLevel() == RiskLevel.HIGH).count();
        long completedWorkflows = workflows.stream().filter(w -> w.getState() == WorkflowState.COMPLETED).count();
        long failedWorkflows = workflows.stream().filter(w -> w.getState() == WorkflowState.FAILED).count();

        // Calculate Average Onboarding Time (Simulated or calculated from history)
        double avgOnboardingHours = 0.0;
        int completedOnboardings = 0;
        for (WorkflowInstance instance : workflows) {
            if ("VENDOR_ONBOARDING".equals(instance.getDefinition().getProcessDefinitionId())
                    && instance.getState() == WorkflowState.COMPLETED) {
                Duration duration = Duration.between(instance.getInitiatedAt(), instance.getEndedAt());
                avgOnboardingHours += duration.toSeconds() / 3600.0; // scale seconds to hours for demo
                completedOnboardings++;
            }
        }
        if (completedOnboardings > 0) {
            avgOnboardingHours /= completedOnboardings;
        } else {
            avgOnboardingHours = 2.4; // Default simulated value
        }

        // Success rate
        double workflowSuccessRate = 100.0;
        if (!workflows.isEmpty()) {
            workflowSuccessRate = ((double) completedWorkflows / workflows.size()) * 100.0;
        }

        ConsoleLogger.info("\n==========================================================================");
        ConsoleLogger.info("                     ENTERPRISE VLMS DASHBOARD (SITES)                    ");
        ConsoleLogger.info("==========================================================================");
        
        ConsoleLogger.info(String.format("  %-30s |  %-30s", "VENDOR METRICS", "TASK METRICS"));
        ConsoleLogger.info("  -------------------------------------------+------------------------------");
        ConsoleLogger.info(String.format("  Total Registered Vendors: %-8d |  Pending Tasks (Queues): %-5d", totalVendors, pendingTasks));
        ConsoleLogger.info(String.format("  Active Vendors:           %-8d |  Claimed/In Progress:   %-5d", activeVendors, claimedTasks));
        ConsoleLogger.info(String.format("  Pending Approval:         %-8d |  Completed Human Tasks: %-5d", pendingVendors, completedTasks));
        ConsoleLogger.info(String.format("  Suspended Vendors:        %-8d |  Overdue / Escalated:   %-5d", suspendedVendors, overdueTasks));
        
        ConsoleLogger.info("\n  -------------------------------------------+------------------------------");
        ConsoleLogger.info(String.format("  %-30s |  %-30s", "WORKFLOW PERFORMANCE", "RISK & COMPLIANCE"));
        ConsoleLogger.info("  -------------------------------------------+------------------------------");
        ConsoleLogger.info(String.format("  Completed Workflows:      %-8d |  High Risk Vendors:     %-5d", completedWorkflows, highRiskVendors));
        ConsoleLogger.info(String.format("  Failed/Cancelled:         %-8d |  Workflow Success Rate: %-5.1f%%", failedWorkflows, workflowSuccessRate));
        ConsoleLogger.info(String.format("  Avg Onboarding Time:      %-8.1f hrs |  Task SLA Breach Rate:  %-5.1f%%", avgOnboardingHours,
                tasks.isEmpty() ? 0.0 : ((double) overdueTasks / tasks.size()) * 100.0));

        ConsoleLogger.info("==========================================================================\n");
    }

    public void printWorkflowReport() {
        ConsoleLogger.info("\n--- WORKFLOW EXECUTION REPORT ---");
        for (WorkflowInstance instance : workflowEngine.getAllInstances()) {
            ConsoleLogger.info(String.format("Instance: %s | Definition: %s | State: %s | Initiated: %s",
                    instance.getWorkflowInstanceId(), instance.getDefinition().getProcessName(),
                    instance.getState(), instance.getInitiatedAt()));
            ConsoleLogger.info("  Execution History:");
            for (WorkflowHistoryEntry entry : instance.getHistory()) {
                ConsoleLogger.info("    " + entry);
            }
        }
    }

    public void printTaskReport() {
        ConsoleLogger.info("\n--- HUMAN TASK REPORT ---");
        for (Task task : taskRepository.findAll()) {
            ConsoleLogger.info(String.format("Task ID: %s | Title: %s | Queue: %s | Assignee: %s | Status: %s | Due: %s | Priority: %s",
                    task.getTaskId(), task.getTitle(), task.getAssignedGroup(),
                    task.getAssignee() != null ? task.getAssignee() : "UNASSIGNED",
                    task.getStatus(), task.getDueDate(), task.getPriority()));
            if (!task.getEscalationHistory().isEmpty()) {
                ConsoleLogger.warn("  Escalation Logs: " + task.getEscalationHistory());
            }
        }
    }
}
