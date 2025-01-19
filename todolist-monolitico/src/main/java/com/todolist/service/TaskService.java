package com.todolist.service;

import com.todolist.exception.ResourceNotFoundException;
import com.todolist.model.Task;
import com.todolist.model.User;
import com.todolist.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public List<Task> getAllTasks(UUID userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getTasksByDueDate(String username, LocalDate date) {
        User user = userService.getUserByUsername(username);
        return taskRepository.findByUserIdAndDueDate(user.getId(), date);
    }

    public void createTask(Task task, UUID userId) {
        User user = userService.getUserById(userId);
        task.setUser(user);
        taskRepository.save(task);
    }

    public void updateTask(UUID id, Task updatedTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setStatus(updatedTask.getStatus());
        taskRepository.save(existingTask);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}
