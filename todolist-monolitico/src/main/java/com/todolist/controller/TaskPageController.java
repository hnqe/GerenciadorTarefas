package com.todolist.controller;

import com.todolist.model.Task;
import com.todolist.service.TaskService;
import com.todolist.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("tasks")
public class TaskPageController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskPageController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    // Página inicial de tarefas
    @GetMapping
    public String getTasksPage(Authentication authentication, Model model) {
        // Obtém o usuário autenticado
        String username = authentication.getName();
        var user = userService.getUserByUsername(username);

        // Adiciona as tarefas do usuário ao modelo
        model.addAttribute("tasks", taskService.getAllTasks(user.getId()));

        // Adiciona uma nova tarefa vazia para o modal
        model.addAttribute("newTask", new Task());

        return "tasks"; // Arquivo HTML "tasks.html"
    }

    // Processa a criação de uma nova tarefa
    @PostMapping
    public String createTask(@ModelAttribute @Valid Task task, Authentication authentication) {
        String username = authentication.getName();
        var user = userService.getUserByUsername(username);
        taskService.createTask(task, user.getId());
        return "redirect:/tasks";
    }

    // Processa a atualização de uma tarefa
    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable UUID id, @ModelAttribute @Valid Task task) {
        taskService.updateTask(id, task);
        return "redirect:/tasks";
    }

    // Deleta uma tarefa
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }
}
