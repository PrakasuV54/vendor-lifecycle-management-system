package com.vlms.main;

import com.vlms.enums.TaskPriority;
import com.vlms.enums.TaskStatus;
import com.vlms.enums.TaskType;
import com.vlms.enums.UserRole;
import com.vlms.exception.UnauthorizedException;
import com.vlms.factory.VendorBuilder;
import com.vlms.model.*;
import com.vlms.repository.*;
import com.vlms.service.*;
import com.vlms.workflow.WorkflowEngine;
import com.vlms.util.ConsoleLogger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Main entry point demonstrating the workflow-oriented architecture of VLMS.
 * Simulates real enterprise BPM operations, human tasks, SLA monitoring, and CoR routing.
 */
public class Main {

    public static void main(String[] args) {

        ConsoleLogger.info("╔══════════════════════════════════════════════════════════════════╗");
        ConsoleLogger.info("║      ENTERPRISE VENDOR LIFECYCLE MANAGEMENT SYSTEM (VLMS)       ║");
        ConsoleLogger.info("║                 WORKFLOW-ORIENTED ENGINE DEMO                   ║");
        ConsoleLogger.info("╚══════════════════════════════════════════════════════════════════╝");

        // ─── 1. Bootstrap Repository Implementations (DIP) ────────────────────
        VendorRepository vendorRepo = new InMemoryVendorRepository();
        ContractRepository contractRepo = new InMemoryContractRepository();
        InvoiceRepository invoiceRepo = new InMemoryInvoiceRepository();
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();
        DocumentRepository documentRepo = new InMemoryDocumentRepository();
        UserRepository userRepo = new InMemoryUserRepository();
        TaskRepository taskRepo = new InMemoryTaskRepository();
        AuditRepository auditRepo = new InMemoryAuditRepository();

        // ─── 2. Bootstrap Core Services ───────────────────────────────────────
        EventBus eventBus = new EventBus();
        AuditService auditService = new AuditService(auditRepo);
        NotificationService notificationService = new NotificationService();

        // Register event-driven observers
        eventBus.subscribe(auditService);
        eventBus.subscribe(notificationService);

        UserService userService = new UserService(userRepo);
        VendorService vendorService = new VendorService(vendorRepo, eventBus);
        DocumentService documentService = new DocumentService(documentRepo, vendorRepo, eventBus);
        ContractService contractService = new ContractService(contractRepo, vendorRepo, eventBus);
        PerformanceService performanceService = new PerformanceService(vendorRepo);
        InvoiceService invoiceService = new InvoiceService(invoiceRepo, vendorRepo, eventBus);
        PaymentService paymentService = new PaymentService(paymentRepo, invoiceRepo, vendorRepo, eventBus);
        RiskService riskService = new RiskService(vendorRepo);
        
        TaskService taskService = new TaskService(taskRepo, userRepo, eventBus);

        // Bootstrap WorkflowEngine and register as event observer
        WorkflowEngine workflowEngine = new WorkflowEngine(
                taskService, vendorService, contractService, invoiceService,
                paymentService, documentService, userRepo, eventBus
        );
        eventBus.subscribe(workflowEngine);

        DashboardService dashboardService = new DashboardService(
                vendorRepo, taskRepo, contractRepo, invoiceRepo, paymentRepo, workflowEngine
        );

        // ═══════════════════════════════════════════════════════════════════════
        // INITIAL SETUP: USERS
        // ═══════════════════════════════════════════════════════════════════════
        printModule("STEP 1: CREATING ENTERPRISE USERS");

        Admin admin = userService.createAdmin("Arjun Mehta", "arjun.mehta@corp.com");
        ProcurementManager pm = userService.createProcurementManager(
                "Priya Sharma", "priya.sharma@corp.com", "Procurement");
        FinanceManager fm = userService.createFinanceManager(
                "Vikram Rao", "vikram.rao@corp.com", "CC-Finance-001");

        ConsoleLogger.info("  Users initialized successfully in User Registry.");

        // ═══════════════════════════════════════════════════════════════════════
        // WORKFLOW 1: VENDOR ONBOARDING WORKFLOW (HUMAN TASK ORCHESTRATION)
        // ═══════════════════════════════════════════════════════════════════════
        printModule("WORKFLOW 1: VENDOR ONBOARDING PROCESS (TechParts India Ltd)");

        // Fluent Construction using VendorBuilder
        Supplier supplier = new VendorBuilder()
                .companyName("TechParts India Ltd")
                .contactPersonName("Rohit Kumar")
                .email("rohit@techparts.in")
                .phone("+919876543210")
                .address("12 Industrial Area, Pune")
                .panNumber("ABCTY1234D")
                .productCategory("Electronics Components")
                .warehouseLocation("Pune Warehouse Zone-A")
                .annualSupplyCapacity(500000)
                .buildSupplier();

        // Start onboarding workflow (triggers steps 1 & 2 automated, then pauses at human step 3)
        WorkflowInstance onboardingInstance = workflowEngine.startVendorOnboarding(supplier, pm.getUserId());
        String onboardingId = onboardingInstance.getWorkflowInstanceId();
        
        // Simulating document uploads which are required for onboarding
        documentService.uploadDocument(supplier.getVendorId(), "GST Certificate",
                "GST27ABCTY1234D1ZS", LocalDate.of(2023, 4, 1), LocalDate.now().plusMonths(8));
        documentService.uploadDocument(supplier.getVendorId(), "PAN Card",
                "ABCTY1234D", LocalDate.of(2010, 1, 1), LocalDate.now().plusYears(10));

        ConsoleLogger.info("  Workflow State: " + onboardingInstance.getState());
        ConsoleLogger.info("  Workflow Stage: " + onboardingInstance.getContext().getCurrentStage());

        // Locate outstanding human task in the Procurement queue
        List<Task> pmTasks = taskService.getTasksByGroup("Procurement Group");
        ConsoleLogger.info("  Procurement Queue Tasks Count: " + pmTasks.size());
        Task verifyTask = pmTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.DOCUMENT_VERIFICATION)
                .findFirst()
                .orElseThrow();

        ConsoleLogger.info("  Claiming & Completing task: " + verifyTask.getTitle() + " by Priya Sharma");
        taskService.claimTask(verifyTask.getTaskId(), pm.getUserId());
        taskService.completeTask(verifyTask.getTaskId(), "GST and PAN documents verified and correct.", pm.getUserId());

        // Workflow automatically resumed and transitioned to the Level 1 Procurement Approval task
        pmTasks = taskService.getTasksByGroup("Procurement Group");
        Task l1ApprovalTask = pmTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.VENDOR_APPROVAL)
                .findFirst()
                .orElseThrow();

        ConsoleLogger.info("  Claiming & Completing task: " + l1ApprovalTask.getTitle() + " by Priya Sharma");
        taskService.claimTask(l1ApprovalTask.getTaskId(), pm.getUserId());
        taskService.completeTask(l1ApprovalTask.getTaskId(), "Procurement check passed. Recommending final approval.", pm.getUserId());

        // Workflow automatically resumed and transitioned to the Level 2 Admin Approval task
        List<Task> adminTasks = taskService.getTasksByGroup("Admin Group");
        Task l2ApprovalTask = adminTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.VENDOR_APPROVAL)
                .findFirst()
                .orElseThrow();

        ConsoleLogger.info("  Claiming & Completing task: " + l2ApprovalTask.getTitle() + " by Arjun Mehta (Admin)");
        taskService.claimTask(l2ApprovalTask.getTaskId(), admin.getUserId());
        taskService.completeTask(l2ApprovalTask.getTaskId(), "Final onboarding sign-off granted.", admin.getUserId());

        // Check if the vendor is now ACTIVE
        Vendor onboardedVendor = vendorService.getVendorById(supplier.getVendorId());
        ConsoleLogger.info("  Onboarding Completed! Vendor Current Status: " + onboardedVendor.getStatus());

        // ═══════════════════════════════════════════════════════════════════════
        // WORKFLOW 2: CONTRACT APPROVAL WITH BUSINESS RULES & COR ROUTING
        // ═══════════════════════════════════════════════════════════════════════
        printModule("WORKFLOW 2: CONTRACT APPROVAL PROCESS");

        // Case A: High-value contract (₹12,00,000) -> Needs Procurement, Finance, Admin approval
        ConsoleLogger.info("  [Case A] Creating Contract of ₹12,00,000 (Requires Finance Approval)");
        Contract highContract = new Contract(supplier.getVendorId(), "High-Value Supply Agreement",
                "Supply electronics", 1200000.0, LocalDate.now(), LocalDate.now().plusMonths(12));

        WorkflowInstance c1Instance = workflowEngine.startContractApproval(highContract, pm.getUserId());

        // 1. Procurement Approval Task
        Task c1PmTask = taskService.getTasksByGroup("Procurement Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(c1Instance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        taskService.completeTask(c1PmTask.getTaskId(), "Procurement review passed.", pm.getUserId());

        // 2. Finance Approval Task (Should exist because 12L > 10L)
        Task c1FinTask = taskService.getTasksByGroup("Finance Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(c1Instance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        ConsoleLogger.info("  ✓ Finance Approval required and present.");
        taskService.completeTask(c1FinTask.getTaskId(), "Finance budget verified.", fm.getUserId());

        // 3. Admin Approval Task
        Task c1AdminTask = taskService.getTasksByGroup("Admin Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(c1Instance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        taskService.completeTask(c1AdminTask.getTaskId(), "Contract approved.", admin.getUserId());

        ConsoleLogger.info("  Contract Status: " + contractService.findContractOrThrow(highContract.getContractId()).getStatus());

        // Case B: Low-value contract (₹8,00,000) -> Bypasses Finance approval based on Business Rules
        ConsoleLogger.info("\n  [Case B] Creating Contract of ₹8,00,000 (Should BYPASS Finance Approval)");
        Contract lowContract = new Contract(supplier.getVendorId(), "Low-Value Services Agreement",
                "Maintenance", 800000.0, LocalDate.now(), LocalDate.now().plusMonths(6));

        WorkflowInstance c2Instance = workflowEngine.startContractApproval(lowContract, pm.getUserId());

        // 1. Procurement Approval Task
        Task c2PmTask = taskService.getTasksByGroup("Procurement Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(c2Instance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        taskService.completeTask(c2PmTask.getTaskId(), "Procurement review passed.", pm.getUserId());

        // Verify Finance Group has no tasks for this contract instance
        boolean hasFinanceTask = taskService.getTasksByGroup("Finance Group").stream()
                .anyMatch(t -> t.getWorkflowInstanceId().equals(c2Instance.getWorkflowInstanceId()));
        ConsoleLogger.info("  ✓ Finance Review bypassed: " + (!hasFinanceTask));

        // 2. Admin Approval Task (Should be active next)
        Task c2AdminTask = taskService.getTasksByGroup("Admin Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(c2Instance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        taskService.completeTask(c2AdminTask.getTaskId(), "Contract approved.", admin.getUserId());

        ConsoleLogger.info("  Contract Status: " + contractService.findContractOrThrow(lowContract.getContractId()).getStatus());

        // ═══════════════════════════════════════════════════════════════════════
        // WORKFLOW 3: INVOICE PROCESSING & AUTO-PAYMENT
        // ═══════════════════════════════════════════════════════════════════════
        printModule("WORKFLOW 3: INVOICE & AUTO-PAYMENT");

        ConsoleLogger.info("  Submitting high-value invoice of ₹7,50,000 (Requires Admin Dual Approval)");
        Invoice invoice = new Invoice(supplier.getVendorId(), highContract.getContractId(), "Milestone 1 invoice",
                750000.0, LocalDate.now(), LocalDate.now().plusDays(30));

        WorkflowInstance invInstance = workflowEngine.startInvoiceProcessing(invoice, pm.getUserId());

        // 1. Finance Review
        Task invFinTask = taskService.getTasksByGroup("Finance Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(invInstance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        taskService.completeTask(invFinTask.getTaskId(), "Invoice audit check passed.", fm.getUserId());

        // 2. Admin Dual Approval Task (Should exist since amount > 5L)
        Task invAdminTask = taskService.getTasksByGroup("Admin Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(invInstance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        ConsoleLogger.info("  ✓ Admin Dual Approval triggered.");
        taskService.completeTask(invAdminTask.getTaskId(), "High-value invoice approved by Admin.", admin.getUserId());

        // Payment processing should have finished automatically
        ConsoleLogger.info("  Invoice Current Status: " + invoiceService.findInvoiceOrThrow(invoice.getInvoiceId()).getStatus());
        ConsoleLogger.info("  Workflow State: " + invInstance.getState());

        // ═══════════════════════════════════════════════════════════════════════
        // WORKFLOW 4: VENDOR SUSPENSION (COMPLAINT ORCHESTRATION)
        // ═══════════════════════════════════════════════════════════════════════
        printModule("WORKFLOW 4: VENDOR SUSPENSION");

        WorkflowInstance suspInstance = workflowEngine.startVendorSuspension(supplier, "Late deliveries and quality concerns in Q2.", pm.getUserId());

        // 1. Investigation Task (Compliance Group)
        Task complianceTask = taskService.getTasksByGroup("Compliance Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(suspInstance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        taskService.completeTask(complianceTask.getTaskId(), "Investigation confirms 3 consecutive SLA violations.", pm.getUserId());

        // 2. Decision Task (Admin Group)
        Task decisionTask = taskService.getTasksByGroup("Admin Group").stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getWorkflowInstanceId().equals(suspInstance.getWorkflowInstanceId()))
                .findFirst().orElseThrow();
        taskService.completeTask(decisionTask.getTaskId(), "Suspend vendor for 30 days.", admin.getUserId());

        // Verify state is suspended
        Vendor suspendedVendor = vendorService.getVendorById(supplier.getVendorId());
        ConsoleLogger.info("  Suspension Completed! Vendor Current Status: " + suspendedVendor.getStatus());

        // ═══════════════════════════════════════════════════════════════════════
        // SECURITY & ROLE-BASED ACCESS CONTROL (RBAC) ENFORCEMENT DEMO
        // ═══════════════════════════════════════════════════════════════════════
        printModule("ROLE-BASED ACCESS CONTROL (RBAC) VERIFICATION");

        // Try to have Finance Manager claim a task in Admin Group
        Task dummyTask = taskService.createTask("Admin Task", "Restricted", "Admin Group",
                TaskPriority.MEDIUM, TaskType.VENDOR_APPROVAL, "dummyWorkflow", "dummyEntity", 24);

        try {
            ConsoleLogger.info("  Attempting to claim Admin task by Finance Manager (Vikram Rao)...");
            taskService.claimTask(dummyTask.getTaskId(), fm.getUserId());
        } catch (UnauthorizedException e) {
            ConsoleLogger.info("  ✓ Correctly caught UnauthorizedException: " + e.getMessage());
        }

        // ═══════════════════════════════════════════════════════════════════════
        // SLA ESCALATION MONITORING DEMO
        // ═══════════════════════════════════════════════════════════════════════
        printModule("SLA BREACH MONITORING & AUTO-ESCALATION");

        // Create a task that was due in the past
        Task overdueTask = taskService.createTask(
                "Urgent Procurement Review",
                "A task that has expired its SLA",
                "Procurement Group",
                TaskPriority.HIGH,
                TaskType.DOCUMENT_VERIFICATION,
                "dummyWf",
                "dummyEnt",
                2
        );
        // Force the creation date/due date to be in the past to trigger escalation
        overdueTask.setDueDate(LocalDateTime.now().minusHours(10));
        overdueTask.setEscalationDate(LocalDateTime.now().minusHours(8));

        ConsoleLogger.info("  Triggering SLA Monitor scan...");
        taskService.monitorSLAs();

        // Check if task is escalated and group changed to Admin Group
        Task checkedTask = taskService.findTaskOrThrow(overdueTask.getTaskId());
        ConsoleLogger.info("  Overdue Task Status: " + checkedTask.getStatus()
                + " | Escalated: " + checkedTask.isEscalated()
                + " | Assigned Queue: " + checkedTask.getAssignedGroup());

        // ═══════════════════════════════════════════════════════════════════════
        // REPORTS & DASHBOARDS
        // ═══════════════════════════════════════════════════════════════════════
        printModule("VLMS ANALYTICS & DASHBOARD");
        dashboardService.displayDashboard();
        
        dashboardService.printWorkflowReport();

        ConsoleLogger.info("\n╔══════════════════════════════════════════════════════════════════╗");
        ConsoleLogger.info("║         VLMS DEMO COMPLETE — ALL MODULES EXERCISED               ║");
        ConsoleLogger.info("╚══════════════════════════════════════════════════════════════════╝");
    }

    private static void printModule(String title) {
        ConsoleLogger.info("\n══════════════════════════════════════════════════════════════════");
        ConsoleLogger.info("  ▶ " + title);
        ConsoleLogger.info("══════════════════════════════════════════════════════════════════");
    }
}
