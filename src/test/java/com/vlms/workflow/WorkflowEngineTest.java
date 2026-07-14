package com.vlms.workflow;

import com.vlms.enums.*;
import com.vlms.factory.VendorBuilder;
import com.vlms.model.*;
import com.vlms.repository.*;
import com.vlms.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowEngineTest {

    private VendorRepository vendorRepository;
    private ContractRepository contractRepository;
    private InvoiceRepository invoiceRepository;
    private PaymentRepository paymentRepository;
    private DocumentRepository documentRepository;
    private UserRepository userRepository;
    private TaskRepository taskRepository;

    private EventBus eventBus;
    private TaskService taskService;
    private VendorService vendorService;
    private DocumentService documentService;
    private ContractService contractService;
    private InvoiceService invoiceService;
    private PaymentService paymentService;

    private WorkflowEngine workflowEngine;

    private User pm;
    private User admin;
    private User finance;

    @BeforeEach
    void setUp() {
        vendorRepository = new InMemoryVendorRepository();
        contractRepository = new InMemoryContractRepository();
        invoiceRepository = new InMemoryInvoiceRepository();
        paymentRepository = new InMemoryPaymentRepository();
        documentRepository = new InMemoryDocumentRepository();
        userRepository = new InMemoryUserRepository();
        taskRepository = new InMemoryTaskRepository();

        eventBus = new EventBus();
        taskService = new TaskService(taskRepository, userRepository, eventBus);
        vendorService = new VendorService(vendorRepository, eventBus);
        documentService = new DocumentService(documentRepository, vendorRepository, eventBus);
        contractService = new ContractService(contractRepository, vendorRepository, eventBus);
        invoiceService = new InvoiceService(invoiceRepository, vendorRepository, eventBus);
        paymentService = new PaymentService(paymentRepository, invoiceRepository, vendorRepository, eventBus);

        workflowEngine = new WorkflowEngine(
                taskService, vendorService, contractService, invoiceService,
                paymentService, documentService, userRepository, eventBus
        );
        eventBus.subscribe(workflowEngine);

        pm = new ProcurementManager("Priya Sharma", "priya@corp.com", "Procurement");
        admin = new Admin("Arjun Mehta", "arjun@corp.com");
        finance = new FinanceManager("Vikram Rao", "vikram@corp.com", "Finance");

        userRepository.save(pm);
        userRepository.save(admin);
        userRepository.save(finance);
    }

    @Test
    void testVendorOnboardingWorkflowSuccess() {
        Vendor vendor = new VendorBuilder()
                .companyName("TechParts India Ltd")
                .contactPersonName("John Doe")
                .email("info@techparts.in")
                .phone("+91-9988776655")
                .address("Bangalore, India")
                .panNumber("ABCDE1234F")
                .productCategory("Supplies")
                .warehouseLocation("Bangalore")
                .annualSupplyCapacity(100000)
                .buildSupplier();

        WorkflowInstance instance = workflowEngine.startVendorOnboarding(vendor, pm.getUserId());
        assertNotNull(instance);
        assertEquals(WorkflowState.WAITING_FOR_USER, instance.getState());

        // Document Verification human task should be created
        List<Task> pmTasks = taskService.getTasksByGroup("Procurement Group");
        Task verifyTask = pmTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.DOCUMENT_VERIFICATION)
                .findFirst()
                .orElse(null);
        assertNotNull(verifyTask);

        // Upload certificates
        documentService.uploadDocument(vendor.getVendorId(), "GST Certificate", "GST_123", LocalDate.now(), LocalDate.now().plusYears(1));
        documentService.uploadDocument(vendor.getVendorId(), "PAN Card", "PAN_456", LocalDate.now(), LocalDate.now().plusYears(5));

        // Complete verification
        taskService.completeTask(verifyTask.getTaskId(), "GST and PAN documents verified and correct.", pm.getUserId());

        // Level 1 Approval task should be created
        List<Task> pmTasks2 = taskService.getTasksByGroup("Procurement Group");
        Task l1ApprovalTask = pmTasks2.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.VENDOR_APPROVAL)
                .findFirst()
                .orElse(null);
        assertNotNull(l1ApprovalTask);

        // Complete Level 1 Approval
        taskService.completeTask(l1ApprovalTask.getTaskId(), "Procurement check passed. Recommending final approval.", pm.getUserId());

        // Level 2 Approval task should be created for Admin Group
        List<Task> adminTasks = taskService.getTasksByGroup("Admin Group");
        Task l2ApprovalTask = adminTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.VENDOR_APPROVAL)
                .findFirst()
                .orElse(null);
        assertNotNull(l2ApprovalTask);

        // Complete Level 2 Approval
        taskService.completeTask(l2ApprovalTask.getTaskId(), "Final onboarding sign-off granted.", admin.getUserId());

        // Workflow should be completed and vendor status is ACTIVE
        assertEquals(WorkflowState.COMPLETED, instance.getState());
        assertEquals(VendorStatus.ACTIVE, vendorRepository.findById(vendor.getVendorId()).get().getStatus());
    }

    @Test
    void testContractApprovalWorkflowLowValueBypassSuccess() {
        // Prepare active vendor
        Vendor vendor = new VendorBuilder()
                .companyName("Active Vendor")
                .contactPersonName("Jane Smith")
                .email("active@corp.com")
                .phone("1122334455")
                .address("Mumbai, India")
                .panNumber("ABCDE1234F")
                .productCategory("Supplies")
                .warehouseLocation("Mumbai")
                .annualSupplyCapacity(100000)
                .buildSupplier();
        vendorRepository.save(vendor);

        // Contract value <= 10 Lakhs (8 Lakhs) - should bypass Finance review step
        Contract contract = new Contract(vendor.getVendorId(), "Supply Agreement", "Supply parts", 800000.0, LocalDate.now(), LocalDate.now().plusMonths(6));

        WorkflowInstance instance = workflowEngine.startContractApproval(contract, pm.getUserId());
        assertNotNull(instance);
        assertEquals(WorkflowState.WAITING_FOR_USER, instance.getState());

        // Procurement Review human task should be created
        List<Task> pmTasks = taskService.getTasksByGroup("Procurement Group");
        Task l1Task = pmTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.CONTRACT_REVIEW)
                .findFirst()
                .orElse(null);
        assertNotNull(l1Task);

        // Complete L1 Review
        taskService.completeTask(l1Task.getTaskId(), "Procurement review passed.", pm.getUserId());

        // Finance Review step should be bypassed directly to Admin Review task
        List<Task> adminTasks = taskService.getTasksByGroup("Admin Group");
        Task l3Task = adminTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.ASSIGNED && t.getType() == TaskType.CONTRACT_REVIEW)
                .findFirst()
                .orElse(null);
        assertNotNull(l3Task); // Verify Admin Review is created

        // Complete L3 Admin Review
        taskService.completeTask(l3Task.getTaskId(), "Contract approved.", admin.getUserId());

        assertEquals(WorkflowState.COMPLETED, instance.getState());
        assertEquals(ContractStatus.ACTIVE, contractRepository.findById(contract.getContractId()).get().getStatus());
    }
}
