package com.vlms.repository;

import com.vlms.enums.TaskStatus;
import com.vlms.model.Task;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository implementation for human tasks.
 */
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<String, Task> taskStore = new HashMap<>();

    @Override
    public void save(Task task) {
        taskStore.put(task.getTaskId(), task);
    }

    @Override
    public void update(Task task) {
        taskStore.put(task.getTaskId(), task);
    }

    @Override
    public Optional<Task> findById(String taskId) {
        return Optional.ofNullable(taskStore.get(taskId));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(taskStore.values());
    }

    @Override
    public List<Task> findByAssignee(String assigneeId) {
        return taskStore.values().stream()
                .filter(t -> assigneeId.equals(t.getAssignee()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByGroup(String groupName) {
        return taskStore.values().stream()
                .filter(t -> groupName.equalsIgnoreCase(t.getAssignedGroup()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        return taskStore.values().stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByWorkflowInstanceId(String workflowInstanceId) {
        return taskStore.values().stream()
                .filter(t -> workflowInstanceId.equals(t.getWorkflowInstanceId()))
                .collect(Collectors.toList());
    }
}
