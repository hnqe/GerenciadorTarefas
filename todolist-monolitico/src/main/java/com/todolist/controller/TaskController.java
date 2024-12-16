package com.todolist.controller;

import com.todolist.model.Task;
import com.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Task> getTasks(@RequestParam UUID userId) {
        return taskService.getAllTasks(userId);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody @Valid Task task, @RequestParam UUID userId) {
        Task newTask = taskService.createTask(task, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable UUID id, @RequestBody @Valid Task task) {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
