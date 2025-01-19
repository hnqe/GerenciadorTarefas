package com.todolist.controller;

import com.todolist.model.Task;
import com.todolist.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final WebClient webClient;
    private final TaskService taskService;

    // Injeta o WebClient configurado e o serviço de tarefas
    public HomeController(WebClient.Builder webClientBuilder, TaskService taskService) {
        this.webClient = webClientBuilder.baseUrl("https://favqs.com/api").build();
        this.taskService = taskService;
    }

    @GetMapping("/home")
    public String getHomePage(Authentication authentication, Model model) {
        String username = authentication.getName();
        model.addAttribute("username", username);

        // Busca frase motivadora
        String idea = webClient.get()
                .uri("/qotd")
                .retrieve()
                .bodyToMono(FavQuoteResponse.class)
                .map(response -> response.getQuote().getBody())
                .onErrorReturn("Failed to load ideas. Please try again later.")
                .block();
        model.addAttribute("idea", idea);

        // Busca as tarefas do dia
        List<Task> todayTasks = taskService.getTasksByDueDate(username, LocalDate.now());
        model.addAttribute("todayTasks", todayTasks);

        return "home"; // Renderiza o arquivo "home.html"
    }

    // Classe para mapear a resposta da API FavQs
    static class FavQuoteResponse {
        private Quote quote;

        static class Quote {
            private String body;

            public String getBody() {
                return body;
            }

        }

        public Quote getQuote() {
            return quote;
        }
    }
}