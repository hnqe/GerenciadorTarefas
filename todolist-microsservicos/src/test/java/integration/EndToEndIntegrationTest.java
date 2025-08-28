package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class EndToEndIntegrationTest {

    private static final String DOCKER_COMPOSE_FILE = "../docker-compose.yml";
    
    @Container
    static DockerComposeContainer<?> compose = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_FILE))
            .withExposedService("auth-service", 8080,
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(3)))
            .withExposedService("task-service", 8081,
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(3)))
            .withExposedService("pomodoro-service", 8082,
                    Wait.forHttp("/api/pomodoro/health")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(3)));

    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;
    private static String authServiceUrl;
    private static String taskServiceUrl;
    private static String pomodoroServiceUrl;

    @BeforeAll
    static void setUp() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        
        // Get service URLs from the compose container
        String authHost = compose.getServiceHost("auth-service", 8080);
        Integer authPort = compose.getServicePort("auth-service", 8080);
        authServiceUrl = "http://" + authHost + ":" + authPort;
        
        String taskHost = compose.getServiceHost("task-service", 8081);
        Integer taskPort = compose.getServicePort("task-service", 8081);
        taskServiceUrl = "http://" + taskHost + ":" + taskPort;
        
        String pomodoroHost = compose.getServiceHost("pomodoro-service", 8082);
        Integer pomodoroPort = compose.getServicePort("pomodoro-service", 8082);
        pomodoroServiceUrl = "http://" + pomodoroHost + ":" + pomodoroPort;
    }

    @Test
    void testCompleteUserJourney() throws Exception {
        // 1. Register a new user in auth-service
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("username", "e2euser");
        registerRequest.put("password", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> registerEntity = new HttpEntity<>(registerRequest, headers);

        ResponseEntity<Map> registerResponse = restTemplate.exchange(
                authServiceUrl + "/api/auth/register",
                HttpMethod.POST,
                registerEntity,
                Map.class
        );

        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getBody());
        assertEquals("User registered successfully", registerResponse.getBody().get("message"));

        // 2. Login with the registered user
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "e2euser");
        loginRequest.put("password", "password123");

        HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Map> loginResponse = restTemplate.exchange(
                authServiceUrl + "/api/auth/login",
                HttpMethod.POST,
                loginEntity,
                Map.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertEquals("e2euser", loginResponse.getBody().get("username"));
        
        String token = (String) loginResponse.getBody().get("token");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 3. Validate token with auth-service
        ResponseEntity<String> validationResponse = restTemplate.getForEntity(
                authServiceUrl + "/api/auth/validate-token?token=" + token,
                String.class
        );

        assertEquals(HttpStatus.OK, validationResponse.getStatusCode());
        assertEquals("true", validationResponse.getBody());

        // 4. Create a task using task-service with the JWT token
        Map<String, Object> taskRequest = new HashMap<>();
        taskRequest.put("title", "E2E Test Task");
        taskRequest.put("description", "This task was created in an end-to-end test");
        taskRequest.put("priority", 3);

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(token);
        HttpEntity<Map<String, Object>> taskEntity = new HttpEntity<>(taskRequest, authHeaders);

        ResponseEntity<Map> createTaskResponse = restTemplate.exchange(
                taskServiceUrl + "/api/tasks",
                HttpMethod.POST,
                taskEntity,
                Map.class
        );

        assertEquals(HttpStatus.CREATED, createTaskResponse.getStatusCode());
        assertNotNull(createTaskResponse.getBody());
        assertEquals("E2E Test Task", createTaskResponse.getBody().get("title"));
        assertEquals("TODO", createTaskResponse.getBody().get("status"));

        Integer taskId = (Integer) createTaskResponse.getBody().get("id");
        assertNotNull(taskId);

        // 5. Get all tasks to verify the created task
        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders);

        ResponseEntity<List> getTasksResponse = restTemplate.exchange(
                taskServiceUrl + "/api/tasks",
                HttpMethod.GET,
                getEntity,
                List.class
        );

        assertEquals(HttpStatus.OK, getTasksResponse.getStatusCode());
        assertNotNull(getTasksResponse.getBody());
        assertEquals(1, getTasksResponse.getBody().size());

        // 6. Update the task
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated E2E Test Task");
        updateRequest.put("description", "This task was updated in an end-to-end test");
        updateRequest.put("status", "IN_PROGRESS");
        updateRequest.put("priority", 1);

        HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updateRequest, authHeaders);

        ResponseEntity<Map> updateResponse = restTemplate.exchange(
                taskServiceUrl + "/api/tasks/edit/" + taskId,
                HttpMethod.PUT,
                updateEntity,
                Map.class
        );

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Updated E2E Test Task", updateResponse.getBody().get("title"));
        assertEquals("IN_PROGRESS", updateResponse.getBody().get("status"));

        // 7. Test pomodoro service integration (create a focus session)
        Map<String, Object> sessionRequest = new HashMap<>();
        sessionRequest.put("type", "FOCUS");
        sessionRequest.put("durationMinutes", 25);
        sessionRequest.put("taskId", taskId.toString());
        sessionRequest.put("taskTitle", "Updated E2E Test Task");

        HttpEntity<Map<String, Object>> sessionEntity = new HttpEntity<>(sessionRequest, authHeaders);

        ResponseEntity<Map> createSessionResponse = restTemplate.exchange(
                pomodoroServiceUrl + "/api/pomodoro/sessions",
                HttpMethod.POST,
                sessionEntity,
                Map.class
        );

        assertEquals(HttpStatus.OK, createSessionResponse.getStatusCode());
        assertNotNull(createSessionResponse.getBody());
        assertEquals("FOCUS", createSessionResponse.getBody().get("type"));
        assertEquals("WAITING", createSessionResponse.getBody().get("status"));
        assertEquals(25, createSessionResponse.getBody().get("plannedDurationMinutes"));

        String sessionId = (String) createSessionResponse.getBody().get("id");
        assertNotNull(sessionId);

        // 8. Get user's pomodoro sessions
        ResponseEntity<List> getSessionsResponse = restTemplate.exchange(
                pomodoroServiceUrl + "/api/pomodoro/sessions",
                HttpMethod.GET,
                getEntity,
                List.class
        );

        assertEquals(HttpStatus.OK, getSessionsResponse.getStatusCode());
        assertNotNull(getSessionsResponse.getBody());
        assertEquals(1, getSessionsResponse.getBody().size());

        // 9. Delete the task
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                taskServiceUrl + "/api/tasks/delete/" + taskId,
                HttpMethod.DELETE,
                getEntity,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // 10. Verify task was deleted
        ResponseEntity<List> finalGetResponse = restTemplate.exchange(
                taskServiceUrl + "/api/tasks",
                HttpMethod.GET,
                getEntity,
                List.class
        );

        assertEquals(HttpStatus.OK, finalGetResponse.getStatusCode());
        assertNotNull(finalGetResponse.getBody());
        assertEquals(0, finalGetResponse.getBody().size());
    }

    @Test
    void testServiceHealthChecks() {
        // Test auth-service health
        ResponseEntity<String> authHealth = restTemplate.getForEntity(
                authServiceUrl + "/actuator/health", String.class);
        assertEquals(HttpStatus.OK, authHealth.getStatusCode());

        // Test task-service health  
        ResponseEntity<String> taskHealth = restTemplate.getForEntity(
                taskServiceUrl + "/actuator/health", String.class);
        assertEquals(HttpStatus.OK, taskHealth.getStatusCode());

        // Test pomodoro-service health
        ResponseEntity<String> pomodoroHealth = restTemplate.getForEntity(
                pomodoroServiceUrl + "/api/pomodoro/health", String.class);
        assertEquals(HttpStatus.OK, pomodoroHealth.getStatusCode());
    }

    @Test
    void testUnauthorizedAccess() {
        // Try to access task-service without token
        ResponseEntity<String> unauthorizedResponse = restTemplate.exchange(
                taskServiceUrl + "/api/tasks",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, unauthorizedResponse.getStatusCode());

        // Try to access pomodoro-service without token
        ResponseEntity<String> pomodoroUnauthorized = restTemplate.exchange(
                pomodoroServiceUrl + "/api/pomodoro/sessions",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, pomodoroUnauthorized.getStatusCode());
    }
}