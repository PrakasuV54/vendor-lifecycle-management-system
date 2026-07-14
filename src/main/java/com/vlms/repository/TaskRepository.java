package com.vlms.repository;

import com.vlms.enums.TaskStatus;
import com.vlms.model.Task;
import java.util.List;
import java.util.Optional;

/**
 * Interface definition for Task persistence.
 */
public interface TaskRepository {
    void save(Task task);
    void update(Task task);
    Optional<Task> findById(String taskId);
    List<Task> findAll();
    List<Task> findByAssignee(String assigneeId);
    List<Task> findByGroup(String groupName);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByWorkflowInstanceId(String workflowInstanceId);
}
