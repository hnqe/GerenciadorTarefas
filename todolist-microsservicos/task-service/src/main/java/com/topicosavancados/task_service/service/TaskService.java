package com.topicosavancados.task_service.service;

import com.topicosavancados.task_service.model.Task;
import com.topicosavancados.task_service.model.TaskStatus;
import com.topicosavancados.task_service.repository.TaskRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks(String username) {
        return taskRepository.findByUsername(username);
    }

    public List<Task> getTasksByDueDate(String username, LocalDate dueDate) {
        return taskRepository.findByUsernameAndDueDate(username, dueDate);
    }

    public Task createTask(Task task, Authentication authentication) {
        String username = authentication.getName();
        UUID userId = extractUserIdFromAuthentication(authentication);

        task.setUsername(username);
        task.setUserId(userId);
        
        // Ensure default status if not set
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        return taskRepository.save(task);
    }

    public Task updateTask(UUID taskId, Task updatedTask) {
        return taskRepository.findById(taskId).map(existingTask -> {
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setDueDate(updatedTask.getDueDate());
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setPriority(updatedTask.getPriority());

            return taskRepository.save(existingTask);
        }).orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
    }

    public Task getTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
    }

    public void deleteTask(UUID taskId) {
        taskRepository.deleteById(taskId);
    }

    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        Object details = authentication.getDetails();
        if (!(details instanceof UUID)) {
            throw new IllegalStateException("Invalid or missing userId in authentication details");
        }
        return (UUID) details;
    }

    // Admin statistics methods
    public long getTotalTasks() {
        return taskRepository.count();
    }

    public long getTasksByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    public long getPendingTasks() {
        return getTasksByStatus(TaskStatus.TODO);
    }

    public long getInProgressTasks() {
        return getTasksByStatus(TaskStatus.IN_PROGRESS);
    }

    public long getCompletedTasks() {
        return getTasksByStatus(TaskStatus.COMPLETED);
    }
}