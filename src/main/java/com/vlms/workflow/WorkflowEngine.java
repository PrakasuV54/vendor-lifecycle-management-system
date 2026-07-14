package com.vlms.workflow;

import com.vlms.enums.*;
import com.vlms.event.VendorEvent;
import com.vlms.exception.UnauthorizedException;
import com.vlms.interfaces.ApprovalHandler;
import com.vlms.interfaces.EventListener;
import com.vlms.model.*;
import com.vlms.repository.UserRepository;
import com.vlms.rules.*;
import com.vlms.service.*;
import com.vlms.util.ConsoleLogger;

import java.time.LocalDate;
import java.util.*;

/**
 * WorkflowEngine processes sequential workflow steps, pauses for human tasks,
 * routes decisions through Business Rules and Chain of Responsibility,
 * and maintains ProcessContext. Resembles Appian Process Execution engine.
 */
public class WorkflowEngine implements EventListener {

    private final Map<String, WorkflowDefinition> definitions = new HashMap<>();
    private final Map<String, WorkflowInstance> instances = new HashMap<>();

    private final TaskService taskService;
    private final VendorService vendorService;
    private final ContractService contractService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final EventBus eventBus;

    // Chain of Responsibility chains
    private final ApprovalHandler vendorApprovalChain;
    private final ApprovalHandler contractApprovalChain;
    private final ApprovalHandler invoiceApprovalChain;

    public WorkflowEngine(TaskService taskService, VendorService vendorService,
                          ContractService contractService, InvoiceService invoiceService,
                          PaymentService paymentService, DocumentService documentService,
                          UserRepository userRepository, EventBus eventBus) {
        this.taskService = taskService;
        this.vendorService = vendorService;
        this.contractService = contractService;
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.documentService = documentService;
        this.userRepository = userRepository;
        this.eventBus = eventBus;

        // Initialize CoR chains
        ProcurementApprovalHandler procurementL1 = new ProcurementApprovalHandler();
        FinanceApprovalHandler financeL2 = new FinanceApprovalHandler();
        AdminApprovalHandler adminL3 = new AdminApprovalHandler();

        procurementL1.setNext(adminL3); // Vendor: Procurement L1 -> Admin L3
        this.vendorApprovalChain = procurementL1;

        ProcurementApprovalHandler contractProcurement = new ProcurementApprovalHandler();
        FinanceApprovalHandler contractFinance = new FinanceApprovalHandler();
        AdminApprovalHandler contractAdmin = new AdminApprovalHandler();

        contractProcurement.setNext(contractFinance);
        contractFinance.setNext(contractAdmin); // Contract: Procurement L1 -> Finance L2 -> Admin L3
        this.contractApprovalChain = contractProcurement;

        FinanceApprovalHandler invoiceFinance = new FinanceApprovalHandler();
        AdminApprovalHandler invoiceAdmin = new AdminApprovalHandler();
        invoiceFinance.setNext(invoiceAdmin); // Invoice: Finance L2 -> Admin L3
        this.invoiceApprovalChain = invoiceFinance;

        // Initialize Definitions
        setupDefinitions();
    }

    private void setupDefinitions() {
        // 1. Vendor Onboarding Workflow
        WorkflowDefinition onboarding = new WorkflowDefinition("VENDOR_ONBOARDING", "Vendor Onboarding Workflow");
        onboarding.addStep(new RegisterVendorStep());
        onboarding.addStep(new UploadDocumentsStep());
        onboarding.addStep(new VerifyDocumentsStep());
        onboarding.addStep(new VendorL1ApprovalStep());
        onboarding.addStep(new VendorL2ApprovalStep());
        onboarding.addStep(new ActivateVendorStep());
        definitions.put(onboarding.getProcessDefinitionId(), onboarding);

        // 2. Contract Approval Workflow
        WorkflowDefinition contractDef = new WorkflowDefinition("CONTRACT_APPROVAL", "Contract Approval Workflow");
        contractDef.addStep(new CreateContractStep());
        contractDef.addStep(new ContractL1ApprovalStep());
        contractDef.addStep(new ContractL2ApprovalStep()); // Finance - Gated by ContractRules
        contractDef.addStep(new ContractL3ApprovalStep());
        contractDef.addStep(new ContractActivateStep());
        definitions.put(contractDef.getProcessDefinitionId(), contractDef);

        // 3. Invoice Processing Workflow
        WorkflowDefinition invoiceDef = new WorkflowDefinition("INVOICE_PROCESSING", "Invoice Processing Workflow");
        invoiceDef.addStep(new SubmitInvoiceStep());
        invoiceDef.addStep(new InvoiceFinanceReviewStep());
        invoiceDef.addStep(new InvoicePaymentApprovalStep()); // Admin review - Gated by InvoiceRules
        invoiceDef.addStep(new InvoiceProcessPaymentStep());
        invoiceDef.addStep(new InvoiceCompleteStep());
        definitions.put(invoiceDef.getProcessDefinitionId(), invoiceDef);

        // 4. Vendor Suspension Workflow
        WorkflowDefinition suspensionDef = new WorkflowDefinition("VENDOR_SUSPENSION", "Vendor Suspension Workflow");
        suspensionDef.addStep(new ComplaintStep());
        suspensionDef.addStep(new InvestigationStep());
        suspensionDef.addStep(new DecisionStep());
        suspensionDef.addStep(new SuspendStep());
        definitions.put(suspensionDef.getProcessDefinitionId(), suspensionDef);
    }

    // ─── Workflow Starters ───────────────────────────────────────────────────

    public WorkflowInstance startVendorOnboarding(Vendor vendor, String initiatedBy) {
        WorkflowDefinition def = definitions.get("VENDOR_ONBOARDING");
        WorkflowInstance instance = new WorkflowInstance(def, initiatedBy);
        instance.getContext().setVendor(vendor);
        instance.getContext().setCurrentStage("Register Vendor");
        instance.getContext().setCompletionPercentage(0.0);
        
        instances.put(instance.getWorkflowInstanceId(), instance);
        
        eventBus.publish(new VendorEvent("WORKFLOW_STARTED")
                .withParam("entityId", vendor.getVendorId())
                .withParam("entityType", "Vendor")
                .withParam("workflowInstanceId", instance.getWorkflowInstanceId()));

        executeWorkflow(instance.getWorkflowInstanceId(), initiatedBy);
        return instance;
    }

    public WorkflowInstance startContractApproval(Contract contract, String initiatedBy) {
        WorkflowDefinition def = definitions.get("CONTRACT_APPROVAL");
        WorkflowInstance instance = new WorkflowInstance(def, initiatedBy);
        instance.getContext().setContract(contract);
        instance.getContext().setCurrentStage("Create Contract");
        instance.getContext().setCompletionPercentage(0.0);

        instances.put(instance.getWorkflowInstanceId(), instance);

        eventBus.publish(new VendorEvent("WORKFLOW_STARTED")
                .withParam("entityId", contract.getContractId())
                .withParam("entityType", "Contract")
                .withParam("workflowInstanceId", instance.getWorkflowInstanceId()));

        executeWorkflow(instance.getWorkflowInstanceId(), initiatedBy);
        return instance;
    }

    public WorkflowInstance startInvoiceProcessing(Invoice invoice, String initiatedBy) {
        WorkflowDefinition def = definitions.get("INVOICE_PROCESSING");
        WorkflowInstance instance = new WorkflowInstance(def, initiatedBy);
        instance.getContext().setInvoice(invoice);
        instance.getContext().setCurrentStage("Submit Invoice");
        instance.getContext().setCompletionPercentage(0.0);

        instances.put(instance.getWorkflowInstanceId(), instance);

        eventBus.publish(new VendorEvent("WORKFLOW_STARTED")
                .withParam("entityId", invoice.getInvoiceId())
                .withParam("entityType", "Invoice")
                .withParam("workflowInstanceId", instance.getWorkflowInstanceId()));

        executeWorkflow(instance.getWorkflowInstanceId(), initiatedBy);
        return instance;
    }

    public WorkflowInstance startVendorSuspension(Vendor vendor, String complaintReason, String initiatedBy) {
        WorkflowDefinition def = definitions.get("VENDOR_SUSPENSION");
        WorkflowInstance instance = new WorkflowInstance(def, initiatedBy);
        instance.getContext().setVendor(vendor);
        instance.getContext().setComments(complaintReason);
        instance.getContext().setCurrentStage("Complaint Raised");
        instance.getContext().setCompletionPercentage(0.0);

        instances.put(instance.getWorkflowInstanceId(), instance);

        eventBus.publish(new VendorEvent("WORKFLOW_STARTED")
                .withParam("entityId", vendor.getVendorId())
                .withParam("entityType", "Vendor")
                .withParam("remarks", complaintReason)
                .withParam("workflowInstanceId", instance.getWorkflowInstanceId()));

        executeWorkflow(instance.getWorkflowInstanceId(), initiatedBy);
        return instance;
    }

    // ─── Execution Engine ────────────────────────────────────────────────────

    public void executeWorkflow(String instanceId, String callerUserId) {
        WorkflowInstance instance = instances.get(instanceId);
        if (instance == null) return;

        if (instance.getState() == WorkflowState.COMPLETED ||
                instance.getState() == WorkflowState.FAILED ||
                instance.getState() == WorkflowState.CANCELLED) {
            return;
        }

        instance.setState(WorkflowState.RUNNING);
        List<WorkflowStep> steps = instance.getDefinition().getSteps();

        while (instance.getCurrentStepIndex() < steps.size()) {
            WorkflowStep step = steps.get(instance.getCurrentStepIndex());
            instance.getContext().setCurrentStage(step.getName());
            
            // Calculate completion percentage
            double percent = ((double) instance.getCurrentStepIndex() / steps.size()) * 100.0;
            instance.getContext().setCompletionPercentage(percent);

            boolean completedImmediately = step.execute(instance, callerUserId);
            if (!completedImmediately) {
                // Paused for human task
                instance.setState(WorkflowState.WAITING_FOR_USER);
                return;
            }

            instance.incrementStepIndex();
        }

        // Completed
        instance.getContext().setCompletionPercentage(100.0);
        User user = userRepository.findById(callerUserId).orElse(null);
        String roleStr = user != null ? user.getRole().name() : "SYSTEM";
        instance.complete(callerUserId, roleStr);

        // Extract entity ID for event parameters
        String refId = "N/A";
        String refType = "N/A";
        if (instance.getContext().getVendor() != null) {
            refId = instance.getContext().getVendor().getVendorId();
            refType = "Vendor";
        } else if (instance.getContext().getContract() != null) {
            refId = instance.getContext().getContract().getContractId();
            refType = "Contract";
        } else if (instance.getContext().getInvoice() != null) {
            refId = instance.getContext().getInvoice().getInvoiceId();
            refType = "Invoice";
        }

        eventBus.publish(new VendorEvent("WORKFLOW_COMPLETED")
                .withParam("entityId", refId)
                .withParam("entityType", refType)
                .withParam("workflowInstanceId", instanceId));
    }

    @Override
    public void onEvent(VendorEvent event) {
        // Monitor task completions to resume workflows automatically
        if ("TASK_COMPLETED".equals(event.getEventType())) {
            String workflowInstanceId = (String) event.getParam("workflowInstanceId");
            String userId = (String) event.getParam("userId");
            String remarks = (String) event.getParam("remarks");

            if (workflowInstanceId != null) {
                ConsoleLogger.info("  [WorkflowEngine] Resuming workflow instance: " + workflowInstanceId + " after human task completion.");
                
                WorkflowInstance instance = instances.get(workflowInstanceId);
                if (instance != null) {
                    // Update process context comments
                    instance.getContext().setComments(remarks);
                    instance.getContext().setApprover(userId);

                    // Move past the human task step
                    instance.incrementStepIndex();
                    // Resume execution
                    executeWorkflow(workflowInstanceId, userId);
                }
            }
        }
    }

    public WorkflowInstance findInstance(String instanceId) {
        return instances.get(instanceId);
    }

    public List<WorkflowInstance> getAllInstances() {
        return new ArrayList<>(instances.values());
    }

    // ─── Concrete Workflow Steps ─────────────────────────────────────────────

    // --- 1. Vendor Onboarding Step Impls ---
    private class RegisterVendorStep extends WorkflowStep {
        RegisterVendorStep() { super("Register Vendor", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            vendorService.registerVendor(vendor);
            return true;
        }
    }

    private class UploadDocumentsStep extends WorkflowStep {
        UploadDocumentsStep() { super("Upload Documents", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            // Assume documents are already linked to the vendor during simulated registration
            Vendor vendor = instance.getContext().getVendor();
            ConsoleLogger.info("  [Step] " + getName() + ": Verifying uploaded docs for " + vendor.getCompanyName());
            return true;
        }
    }

    private class VerifyDocumentsStep extends WorkflowStep {
        VerifyDocumentsStep() { super("Document Verification", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            // Transition state to verification
            vendorService.submitForVerification(vendor.getVendorId());

            Task task = taskService.createTask(
                    "Verify Documents for " + vendor.getCompanyName(),
                    "Check GST, PAN and compliance documents.",
                    "Procurement Group",
                    TaskPriority.HIGH,
                    TaskType.DOCUMENT_VERIFICATION,
                    instance.getWorkflowInstanceId(),
                    vendor.getVendorId(),
                    24
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Procurement Group");
            return false;
        }
    }

    private class VendorL1ApprovalStep extends WorkflowStep {
        VendorL1ApprovalStep() { super("Procurement Approval", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            
            // Mark documents verified based on previous step success
            List<Document> docs = documentService.getDocumentsByVendor(vendor.getVendorId());
            for (Document doc : docs) {
                if (doc.getStatus() == DocumentStatus.PENDING) {
                    documentService.verifyDocument(doc.getDocumentId(), callerUserId);
                }
            }
            vendorService.markVerified(vendor.getVendorId());

            Task task = taskService.createTask(
                    "Level 1 Approval: " + vendor.getCompanyName(),
                    "Review vendor onboarding and verify procurement compliance.",
                    "Procurement Group",
                    TaskPriority.MEDIUM,
                    TaskType.VENDOR_APPROVAL,
                    instance.getWorkflowInstanceId(),
                    vendor.getVendorId(),
                    48
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Procurement Group");
            return false;
        }
    }

    private class VendorL2ApprovalStep extends WorkflowStep {
        VendorL2ApprovalStep() { super("Admin Approval", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            User user = userRepository.findById(callerUserId).orElseThrow();
            
            // Route L1 decision through Chain of Responsibility
            ApprovalRequest request = new ApprovalRequest(
                    vendor.getVendorId(), "VENDOR", 0.0, "APPROVE",
                    callerUserId, user.getRole(), instance.getContext().getComments()
            );
            vendorApprovalChain.handle(request);

            if ("REJECTED".equals(request.getFinalStatus()) || "CHANGES_REQUESTED".equals(request.getFinalStatus())) {
                instance.fail("L1 Procurement Approval Rejected: " + request.getFinalStatus(), callerUserId, user.getRole().name());
                return false;
            }

            Task task = taskService.createTask(
                    "Level 2 Approval: " + vendor.getCompanyName(),
                    "Final Admin sign-off for onboarding vendor.",
                    "Admin Group",
                    TaskPriority.HIGH,
                    TaskType.VENDOR_APPROVAL,
                    instance.getWorkflowInstanceId(),
                    vendor.getVendorId(),
                    24
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Admin Group");
            return false;
        }
    }

    private class ActivateVendorStep extends WorkflowStep {
        ActivateVendorStep() { super("Vendor Activated", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            User user = userRepository.findById(callerUserId).orElseThrow();

            // Run CoR for L2 final approval
            ApprovalRequest request = new ApprovalRequest(
                    vendor.getVendorId(), "VENDOR", 0.0, "APPROVE",
                    callerUserId, user.getRole(), instance.getContext().getComments()
            );
            vendorApprovalChain.handle(request);

            if (request.isApproved()) {
                vendorService.approveVendor(vendor.getVendorId());
                vendorService.activateVendor(vendor.getVendorId());
                ConsoleLogger.info("  [Step] " + getName() + ": Vendor " + vendor.getCompanyName() + " is now ACTIVE.");
                return true;
            } else {
                instance.fail("Final Admin approval failed: " + request.getFinalStatus(), callerUserId, user.getRole().name());
                return false;
            }
        }
    }

    // --- 2. Contract Approval Step Impls ---
    private class CreateContractStep extends WorkflowStep {
        CreateContractStep() { super("Create Contract", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Contract contract = instance.getContext().getContract();
            contractService.saveContract(contract);
            return true;
        }
    }

    private class ContractL1ApprovalStep extends WorkflowStep {
        ContractL1ApprovalStep() { super("Procurement Approval", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Contract contract = instance.getContext().getContract();
            Task task = taskService.createTask(
                    "Contract Procurement Review: " + contract.getContractTitle(),
                    "Review contract legal clauses and deliverables.",
                    "Procurement Group",
                    TaskPriority.MEDIUM,
                    TaskType.CONTRACT_REVIEW,
                    instance.getWorkflowInstanceId(),
                    contract.getContractId(),
                    48
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Procurement Group");
            return false;
        }
    }

    private class ContractL2ApprovalStep extends WorkflowStep {
        ContractL2ApprovalStep() { super("Finance Approval", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Contract contract = instance.getContext().getContract();
            User user = userRepository.findById(callerUserId).orElseThrow();

            // Run CoR for L1 decision
            ApprovalRequest request = new ApprovalRequest(
                    contract.getContractId(), "CONTRACT", contract.getContractValue(), "APPROVE",
                    callerUserId, user.getRole(), instance.getContext().getComments()
            );
            contractApprovalChain.handle(request);

            if ("REJECTED".equals(request.getFinalStatus()) || "CHANGES_REQUESTED".equals(request.getFinalStatus())) {
                instance.fail("Procurement approval rejected: " + request.getFinalStatus(), callerUserId, user.getRole().name());
                return false;
            }

            // Gated by Business Rules: if contract value <= 10L, bypass Finance approval
            if (!ContractRules.requiresFinanceApproval(contract)) {
                ConsoleLogger.info("  [Business Rules] Value <= 10L. Bypassing Finance Approval step.");
                return true; // Skip to next step
            }

            Task task = taskService.createTask(
                    "Contract Finance Review: " + contract.getContractTitle(),
                    "Financial verification for high-value contract.",
                    "Finance Group",
                    TaskPriority.HIGH,
                    TaskType.CONTRACT_REVIEW,
                    instance.getWorkflowInstanceId(),
                    contract.getContractId(),
                    24
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Finance Group");
            return false;
        }
    }

    private class ContractL3ApprovalStep extends WorkflowStep {
        ContractL3ApprovalStep() { super("Admin Approval", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Contract contract = instance.getContext().getContract();
            User user = userRepository.findById(callerUserId).orElseThrow();

            ApprovalRequest request = new ApprovalRequest(
                    contract.getContractId(), "CONTRACT", contract.getContractValue(), "APPROVE",
                    callerUserId, user.getRole(), instance.getContext().getComments()
            );
            contractApprovalChain.handle(request);

            if ("REJECTED".equals(request.getFinalStatus()) || "CHANGES_REQUESTED".equals(request.getFinalStatus())) {
                instance.fail("Finance approval failed: " + request.getFinalStatus(), callerUserId, user.getRole().name());
                return false;
            }

            Task task = taskService.createTask(
                    "Contract Admin Review: " + contract.getContractTitle(),
                    "Final administrative sign-off for contract execution.",
                    "Admin Group",
                    TaskPriority.HIGH,
                    TaskType.CONTRACT_REVIEW,
                    instance.getWorkflowInstanceId(),
                    contract.getContractId(),
                    24
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Admin Group");
            return false;
        }
    }

    private class ContractActivateStep extends WorkflowStep {
        ContractActivateStep() { super("Contract Activated", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Contract contract = instance.getContext().getContract();
            User user = userRepository.findById(callerUserId).orElseThrow();

            ApprovalRequest request = new ApprovalRequest(
                    contract.getContractId(), "CONTRACT", contract.getContractValue(), "APPROVE",
                    callerUserId, user.getRole(), instance.getContext().getComments()
            );
            contractApprovalChain.handle(request);

            if (request.isApproved()) {
                contractService.activateContract(contract.getContractId());
                ConsoleLogger.info("  [Step] " + getName() + ": Contract " + contract.getContractTitle() + " is ACTIVE.");
                return true;
            } else {
                instance.fail("Final Admin approval failed: " + request.getFinalStatus(), callerUserId, user.getRole().name());
                return false;
            }
        }
    }

    // --- 3. Invoice Processing Step Impls ---
    private class SubmitInvoiceStep extends WorkflowStep {
        SubmitInvoiceStep() { super("Submit Invoice", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Invoice invoice = instance.getContext().getInvoice();
            invoiceService.saveInvoice(invoice);
            return true;
        }
    }

    private class InvoiceFinanceReviewStep extends WorkflowStep {
        InvoiceFinanceReviewStep() { super("Finance Review", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Invoice invoice = instance.getContext().getInvoice();
            Task task = taskService.createTask(
                    "Invoice Finance Audit: " + invoice.getInvoiceId(),
                    "Verify invoice details, invoice date, and deliverables mapping.",
                    "Finance Group",
                    TaskPriority.MEDIUM,
                    TaskType.INVOICE_APPROVAL,
                    instance.getWorkflowInstanceId(),
                    invoice.getInvoiceId(),
                    24
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Finance Group");
            return false;
        }
    }

    private class InvoicePaymentApprovalStep extends WorkflowStep {
        InvoicePaymentApprovalStep() { super("Payment Approval", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Invoice invoice = instance.getContext().getInvoice();
            User user = userRepository.findById(callerUserId).orElseThrow();

            ApprovalRequest request = new ApprovalRequest(
                    invoice.getInvoiceId(), "INVOICE", invoice.getAmount(), "APPROVE",
                    callerUserId, user.getRole(), instance.getContext().getComments()
            );
            invoiceApprovalChain.handle(request);

            if ("REJECTED".equals(request.getFinalStatus()) || "CHANGES_REQUESTED".equals(request.getFinalStatus())) {
                instance.fail("Finance Review Rejected: " + request.getFinalStatus(), callerUserId, user.getRole().name());
                return false;
            }

            // Gated by Business Rules: if invoice > 5L, requires Dual Approval (Admin signoff)
            if (!InvoiceRules.requiresDualApproval(invoice)) {
                ConsoleLogger.info("  [Business Rules] Value <= 5L. Bypassing Admin Dual Approval step.");
                invoiceService.approveInvoice(invoice.getInvoiceId(), callerUserId);
                return true;
            }

            Task task = taskService.createTask(
                    "Invoice Admin Approval: " + invoice.getInvoiceId(),
                    "Dual authorization for high-value invoice approval.",
                    "Admin Group",
                    TaskPriority.HIGH,
                    TaskType.INVOICE_APPROVAL,
                    instance.getWorkflowInstanceId(),
                    invoice.getInvoiceId(),
                    12
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Admin Group");
            return false;
        }
    }

    private class InvoiceProcessPaymentStep extends WorkflowStep {
        InvoiceProcessPaymentStep() { super("Payment Processing", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Invoice invoice = instance.getContext().getInvoice();
            User user = userRepository.findById(callerUserId).orElseThrow();

            if (InvoiceRules.requiresDualApproval(invoice)) {
                ApprovalRequest request = new ApprovalRequest(
                        invoice.getInvoiceId(), "INVOICE", invoice.getAmount(), "APPROVE",
                        callerUserId, user.getRole(), instance.getContext().getComments()
                );
                invoiceApprovalChain.handle(request);

                if (request.isApproved()) {
                    invoiceService.approveInvoice(invoice.getInvoiceId(), callerUserId);
                } else {
                    instance.fail("Dual approval failed: " + request.getFinalStatus(), callerUserId, user.getRole().name());
                    return false;
                }
            }

            // Initiate payment
            Payment payment = paymentService.initiatePayment(invoice.getInvoiceId(), "BANK_TRANSFER", callerUserId);
            instance.getContext().setPayment(payment);
            paymentService.approvePayment(payment.getPaymentId());
            paymentService.processPayment(payment.getPaymentId());

            return true;
        }
    }

    private class InvoiceCompleteStep extends WorkflowStep {
        InvoiceCompleteStep() { super("Payment Completed", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Payment payment = instance.getContext().getPayment();
            ConsoleLogger.info("  [Step] " + getName() + ": Payment successfully dispatched for " + payment.getAmount());
            
            eventBus.publish(new VendorEvent("PAYMENT_COMPLETED")
                    .withParam("entityId", payment.getInvoiceId())
                    .withParam("entityType", "Invoice")
                    .withParam("remarks", "Dispatched to vendor"));
            return true;
        }
    }

    // --- 4. Vendor Suspension Step Impls ---
    private class ComplaintStep extends WorkflowStep {
        ComplaintStep() { super("Complaint Raised", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            ConsoleLogger.info("  [Step] Complaint logged for vendor " + vendor.getCompanyName() + ". Reason: " + instance.getContext().getComments());
            return true;
        }
    }

    private class InvestigationStep extends WorkflowStep {
        InvestigationStep() { super("Investigation", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            Task task = taskService.createTask(
                    "Investigate Complaint: " + vendor.getCompanyName(),
                    "Review complaint details: " + instance.getContext().getComments(),
                    "Compliance Group",
                    TaskPriority.HIGH,
                    TaskType.COMPLAINT_INVESTIGATION,
                    instance.getWorkflowInstanceId(),
                    vendor.getVendorId(),
                    72
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Compliance Group");
            return false;
        }
    }

    private class DecisionStep extends WorkflowStep {
        DecisionStep() { super("Decision", true); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            Task task = taskService.createTask(
                    "Review Investigation Findings: " + vendor.getCompanyName(),
                    "Decide whether to Suspend Vendor or Reject Complaint.",
                    "Admin Group",
                    TaskPriority.HIGH,
                    TaskType.COMPLAINT_DECISION,
                    instance.getWorkflowInstanceId(),
                    vendor.getVendorId(),
                    24
            );
            instance.getContext().setCurrentTask(task);
            instance.getContext().setCurrentGroup("Admin Group");
            return false;
        }
    }

    private class SuspendStep extends WorkflowStep {
        SuspendStep() { super("Suspend", false); }
        @Override
        public boolean execute(WorkflowInstance instance, String callerUserId) {
            Vendor vendor = instance.getContext().getVendor();
            String decision = instance.getContext().getComments();

            if (decision != null && decision.toLowerCase().contains("suspend")) {
                vendorService.suspendVendor(vendor.getVendorId(), "Suspended via workflow: " + decision);
                ConsoleLogger.info("  [Step] Vendor " + vendor.getCompanyName() + " SUSPENDED.");
            } else {
                ConsoleLogger.info("  [Step] Complaint rejected. Vendor " + vendor.getCompanyName() + " remains active.");
            }
            return true;
        }
    }
}
