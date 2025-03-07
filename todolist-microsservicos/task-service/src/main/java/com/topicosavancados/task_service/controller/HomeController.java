package com.topicosavancados.task_service.controller;

import com.topicosavancados.task_service.model.Task;
import com.topicosavancados.task_service.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final WebClient webClient;
    private final TaskService taskService;

    public HomeController(WebClient.Builder webClientBuilder, TaskService taskService) {
        this.webClient = webClientBuilder.baseUrl("https://favqs.com/api").build();
        this.taskService = taskService;
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication object is null!");
        }

        String username = authentication.getName();

        // Ao invés de toda a lógica inline do webClient, chamamos o método auxiliar:
        String idea = fetchQuoteFromFavQs();

        // Verifica se o usuário tem ROLE_ADMIN
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String role = roles.contains("ROLE_ADMIN") ? "ADMIN" : "USER";

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("idea", idea);
        response.put("role", role);

        return response;
    }

    @GetMapping("/tasks/today")
    public List<Task> getTodayTasks(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication object is null!");
        }
        String username = authentication.getName();
        return taskService.getTasksByDueDate(username, LocalDate.now());
    }

    /**
     * Método protegido para buscar a quote no FavQs.
     */
    protected String fetchQuoteFromFavQs() {
        try {
            return webClient.get()
                    .uri("/qotd")
                    .retrieve()
                    .bodyToMono(FavQuoteResponse.class)
                    .map(response -> response.getQuote().getBody())
                    .block();
        } catch (Exception e) {
            System.err.println("Error fetching idea: " + e.getMessage());
            return "Failed to load ideas. Please try again later.";
        }
    }

    static class FavQuoteResponse {
        private Quote quote;
        static class Quote {
            private String body;
            public String getBody() {
                return body;
            }

            public void setBody(String body) {
                this.body = body;
            }
        }
        public Quote getQuote() {
            return quote;
        }

        public void setQuote(Quote quote) {
            this.quote = quote;
        }
    }

}
