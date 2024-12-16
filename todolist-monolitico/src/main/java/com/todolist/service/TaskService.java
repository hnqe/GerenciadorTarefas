package com.todolist.service;

import com.todolist.exception.ResourceNotFoundException;
import com.todolist.model.Task;
import com.todolist.model.User;
import com.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public List<Task> getAllTasks(UUID userId) {
        return taskRepository.findByUserId(userId);
    }

    public Task getTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    public List<Task> getTasksByDueDate(String username, LocalDate date) {
        User user = userService.getUserByUsername(username);
        return taskRepository.findByUserIdAndDueDate(user.getId(), date);
    }


    public Task createTask(Task task, UUID userId) {
        User user = userService.getUserById(userId);
        task.setUser(user);
        return taskRepository.save(task);
    }

    public Task updateTask(UUID id, Task updatedTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setStatus(updatedTask.getStatus());
        return taskRepository.save(existingTask);
    }

    public void deleteTask(UUID id) {
        taskRepository.deleteById(id);
    }
}
