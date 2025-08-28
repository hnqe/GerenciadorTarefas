package com.topicosavancados.task_service.controller;

import com.topicosavancados.task_service.dto.TaskStatsResponse;
import com.topicosavancados.task_service.model.Task;
import com.topicosavancados.task_service.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Retorna todas as tarefas do usuário autenticado
    @GetMapping
    public List<Task> getTasks(Authentication authentication) {
        String username = authentication.getName();
        return taskService.getAllTasks(username);
    }

    // Cria uma nova tarefa
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody @Valid Task task, Authentication authentication) {
        return taskService.createTask(task, authentication);
    }

    @PutMapping("/edit/{id}")
    public Task updateTask(@PathVariable UUID id, @RequestBody @Valid Task task) {
        return taskService.updateTask(id, task);
    }

    // Exclui uma tarefa
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable UUID id) {
        return taskService.getTaskById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskStatsResponse getTaskStats() {
        long totalTasks = taskService.getTotalTasks();
        long pendingTasks = taskService.getPendingTasks();
        long inProgressTasks = taskService.getInProgressTasks();
        long completedTasks = taskService.getCompletedTasks();
        
        return new TaskStatsResponse(totalTasks, pendingTasks, inProgressTasks, completedTasks);
    }
}