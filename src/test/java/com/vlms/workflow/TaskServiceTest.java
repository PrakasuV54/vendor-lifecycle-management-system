package com.vlms.workflow;

import com.vlms.enums.TaskPriority;
import com.vlms.enums.TaskStatus;
import com.vlms.enums.TaskType;
import com.vlms.enums.UserRole;
import com.vlms.exception.UnauthorizedException;
import com.vlms.model.*;
import com.vlms.repository.InMemoryTaskRepository;
import com.vlms.repository.InMemoryUserRepository;
import com.vlms.repository.TaskRepository;
import com.vlms.repository.UserRepository;
import com.vlms.service.EventBus;
import com.vlms.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceTest {

    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private EventBus eventBus;
    private TaskService taskService;

    private User pm;
    private User admin;
    private User finance;

    @BeforeEach
    void setUp() {
        taskRepository = new InMemoryTaskRepository();
        userRepository = new InMemoryUserRepository();
        eventBus = new EventBus();
        taskService = new TaskService(taskRepository, userRepository, eventBus);

        pm = new ProcurementManager("Priya Sharma", "priya@corp.com", "Procurement");
        admin = new Admin("Arjun Mehta", "arjun@corp.com");
        finance = new FinanceManager("Vikram Rao", "vikram@corp.com", "Finance");

        userRepository.save(pm);
        userRepository.save(admin);
        userRepository.save(finance);
    }

    @Test
    void testTaskCreationAndClaiming() {
        Task task = taskService.createTask(
                "Review Vendor Doc", "Check doc", "Procurement Group",
                TaskPriority.MEDIUM, TaskType.DOCUMENT_VERIFICATION, "WKF-1", "VND-1", 24
        );

        assertEquals(TaskStatus.ASSIGNED, task.getStatus());
        assertEquals("Procurement Group", task.getAssignedGroup());

        // PM claims task
        taskService.claimTask(task.getTaskId(), pm.getUserId());
        assertEquals(TaskStatus.CLAIMED, task.getStatus());
        assertEquals(pm.getUserId(), task.getAssignee());
    }

    @Test
    void testClaimTaskUnauthorizedThrowsException() {
        Task task = taskService.createTask(
                "Review Vendor Doc", "Check doc", "Procurement Group",
                TaskPriority.MEDIUM, TaskType.DOCUMENT_VERIFICATION, "WKF-1", "VND-1", 24
        );

        // Finance manager attempts to claim a procurement task
        assertThrows(UnauthorizedException.class, () -> {
            taskService.claimTask(task.getTaskId(), finance.getUserId());
        });
    }

    @Test
    void testTaskCompletion() {
        Task task = taskService.createTask(
                "Review Vendor Doc", "Check doc", "Procurement Group",
                TaskPriority.MEDIUM, TaskType.DOCUMENT_VERIFICATION, "WKF-1", "VND-1", 24
        );

        taskService.completeTask(task.getTaskId(), "Docs verified successfully", pm.getUserId());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals("Docs verified successfully", task.getRemarks());
    }

    @Test
    void testSlaMonitoringAndEscalation() {
        Task task = taskService.createTask(
                "Urgent Task", "Hurry", "Procurement Group",
                TaskPriority.HIGH, TaskType.DOCUMENT_VERIFICATION, "WKF-1", "VND-1", 24
        );

        // Explicitly set due date to the past to simulate breach
        task.setEscalationDate(LocalDateTime.now().minusHours(1));
        taskRepository.update(task);

        // Monitor SLAs
        taskService.monitorSLAs();

        Task updated = taskService.findTaskOrThrow(task.getTaskId());
        assertTrue(updated.isEscalated());
        assertEquals(TaskStatus.ESCALATED, updated.getStatus());
        assertEquals("Admin Group", updated.getAssignedGroup());
        assertNull(updated.getAssignee()); // Unassigned from previous holder
    }
}
