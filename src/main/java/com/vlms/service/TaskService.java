package com.vlms.service;

import com.vlms.enums.TaskPriority;
import com.vlms.enums.TaskStatus;
import com.vlms.enums.TaskType;
import com.vlms.enums.UserRole;
import com.vlms.event.VendorEvent;
import com.vlms.exception.UnauthorizedException;
import com.vlms.model.Task;
import com.vlms.model.User;
import com.vlms.repository.TaskRepository;
import com.vlms.repository.UserRepository;
import com.vlms.util.ConsoleLogger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TaskService orchestrates task assignment, claiming, completion, and SLAs.
 * Resembles Appian's User Task execution engine.
 */
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EventBus eventBus;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, EventBus eventBus) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.eventBus = eventBus;
    }

    public Task createTask(String title, String description, String assignedGroup, TaskPriority priority,
                           TaskType type, String workflowInstanceId, String relatedEntityId, int slaDurationHours) {
        Task task = new Task(title, description, assignedGroup, priority, type, workflowInstanceId, relatedEntityId, slaDurationHours);
        if (assignedGroup != null) {
            task.setStatus(TaskStatus.ASSIGNED);
        }
        taskRepository.save(task);
        
        // Publish event
        eventBus.publish(new VendorEvent("TASK_ASSIGNED")
                .withParam("taskId", task.getTaskId())
                .withParam("title", task.getTitle())
                .withParam("assignedGroup", task.getAssignedGroup())
                .withParam("entityId", task.getRelatedEntityId())
                .withParam("entityType", "Task"));
                
        return task;
    }

    public void claimTask(String taskId, String userId) {
        Task task = findTaskOrThrow(taskId);
        User user = findUserOrThrow(userId);

        if (!isUserInGroup(user, task.getAssignedGroup())) {
            throw new UnauthorizedException("User " + userId + " does not belong to group " + task.getAssignedGroup());
        }

        task.claim(userId);
        taskRepository.update(task);
        
        eventBus.publish(new VendorEvent("TASK_CLAIMED")
                .withParam("taskId", task.getTaskId())
                .withParam("userId", userId)
                .withParam("entityId", task.getRelatedEntityId()));
    }

    public void startTask(String taskId, String userId) {
        Task task = findTaskOrThrow(taskId);
        if (!userId.equals(task.getAssignee())) {
            throw new UnauthorizedException("Task is not claimed by user: " + userId);
        }
        task.startProgress();
        taskRepository.update(task);
    }

    public void completeTask(String taskId, String remarks, String userId) {
        Task task = findTaskOrThrow(taskId);
        User user = findUserOrThrow(userId);

        // Auto claim if not claimed
        if (task.getAssignee() == null) {
            claimTask(taskId, userId);
        } else if (!userId.equals(task.getAssignee())) {
            throw new UnauthorizedException("Task is claimed by another user: " + task.getAssignee());
        }

        task.complete(remarks);
        taskRepository.update(task);

        ConsoleLogger.info("  [TaskService] Human Task Completed: " + task.getTitle() + " by " + user.getName());

        eventBus.publish(new VendorEvent("TASK_COMPLETED")
                .withParam("taskId", task.getTaskId())
                .withParam("userId", userId)
                .withParam("userRole", user.getRole().name())
                .withParam("remarks", remarks)
                .withParam("workflowInstanceId", task.getWorkflowInstanceId())
                .withParam("entityId", task.getRelatedEntityId()));
    }

    public void cancelTask(String taskId, String remarks, String userId) {
        Task task = findTaskOrThrow(taskId);
        User user = findUserOrThrow(userId);

        if (user.getRole() != UserRole.ADMIN && !userId.equals(task.getAssignee())) {
            throw new UnauthorizedException("Only Admin or the assignee can cancel a task.");
        }

        task.cancel(remarks);
        taskRepository.update(task);

        eventBus.publish(new VendorEvent("TASK_CANCELLED")
                .withParam("taskId", task.getTaskId())
                .withParam("userId", userId)
                .withParam("remarks", remarks));
    }

    /**
     * Checks SLAs for all non-completed tasks.
     * Transitions overdue tasks to ESCALATED, updates group/assignee, and records logs.
     */
    public void monitorSLAs() {
        List<Task> tasks = taskRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
                continue;
            }

            if (now.isAfter(task.getEscalationDate()) && !task.isEscalated()) {
                // Escalate
                String escalationReason = "Task exceeded SLA time of " + task.getDueDate();
                task.escalate(escalationReason);
                
                // Reassign to Admin Group
                String oldGroup = task.getAssignedGroup();
                task.reassignGroup("Admin Group", "Escalation auto-reassignment");
                task.setAssignee(null); // Return to work queue
                taskRepository.update(task);

                ConsoleLogger.warn("  [SLA Alert] Task " + task.getTaskId() + " (\"" + task.getTitle() 
                        + "\") is OVERDUE! Escalated from " + oldGroup + " to Admin Group.");

                eventBus.publish(new VendorEvent("TASK_ESCALATED")
                        .withParam("taskId", task.getTaskId())
                        .withParam("title", task.getTitle())
                        .withParam("oldGroup", oldGroup)
                        .withParam("remarks", escalationReason)
                        .withParam("entityId", task.getRelatedEntityId())
                        .withParam("entityType", "Task"));
            }
        }
    }

    public boolean isUserInGroup(User user, String groupName) {
        if (user.getRole() == UserRole.ADMIN) return true; // Admins are superusers
        switch (groupName) {
            case "Procurement Group":
                return user.getRole() == UserRole.PROCUREMENT_MANAGER;
            case "Finance Group":
                return user.getRole() == UserRole.FINANCE_MANAGER;
            case "Compliance Group":
                return user.getRole() == UserRole.PROCUREMENT_MANAGER;
            case "Admin Group":
                return user.getRole() == UserRole.ADMIN;
            default:
                return false;
        }
    }

    public List<Task> getTasksByGroup(String groupName) {
        return taskRepository.findByGroup(groupName);
    }

    public List<Task> getTasksByAssignee(String userId) {
        return taskRepository.findByAssignee(userId);
    }

    public List<Task> getTasksByWorkflow(String workflowInstanceId) {
        return taskRepository.findByWorkflowInstanceId(workflowInstanceId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task findTaskOrThrow(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private User findUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
