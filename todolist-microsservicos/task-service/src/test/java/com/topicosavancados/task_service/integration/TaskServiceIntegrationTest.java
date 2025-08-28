package com.topicosavancados.task_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topicosavancados.task_service.dto.TaskRequest;
import com.topicosavancados.task_service.dto.TaskResponse;
import com.topicosavancados.task_service.integration.TestJwtHelper;
import com.topicosavancados.task_service.model.Task;
import com.topicosavancados.task_service.model.TaskStatus;
import com.topicosavancados.task_service.repository.TaskRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class TaskServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("task_service_test")
            .withUsername("test")
            .withPassword("test");

    private static MockWebServer mockAuthService;

    @BeforeAll
    static void setUpMockServer() throws Exception {
        mockAuthService = new MockWebServer();
        mockAuthService.start();
    }

    @AfterAll
    static void tearDownMockServer() throws Exception {
        if (mockAuthService != null) {
            mockAuthService.shutdown();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        
        // Configure mock auth service URL
        registry.add("auth-service.url", () -> "http://localhost:" + mockAuthService.getPort());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    private String validJwtToken;
    private UUID testUserId;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        
        // Setup test data
        testUserId = UUID.fromString("12345678-1234-1234-1234-123456789012");
        validJwtToken = TestJwtHelper.generateValidToken("testuser", testUserId, "ROLE_USER");
        
        // Mock auth service responses
        mockAuthService.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("true")
                .addHeader("Content-Type", "application/json"));
    }

    @Test
    void testCompleteTaskManagementFlow() throws Exception {
        // Setup additional mock responses for the multiple calls
        for (int i = 0; i < 10; i++) {
            mockAuthService.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("true")
                    .addHeader("Content-Type", "application/json"));
        }

        // 1. Create a new task
        TaskRequest createRequest = new TaskRequest();
        createRequest.setTitle("Integration Test Task");
        createRequest.setDescription("This is a test task for integration testing");
        createRequest.setPriority(5);

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("This is a test task for integration testing"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value(5))
                .andReturn();

        // Extract task ID from response
        String responseContent = createResult.getResponse().getContentAsString();
        TaskResponse createdTask = objectMapper.readValue(responseContent, TaskResponse.class);
        UUID taskId = createdTask.getId();

        // Verify task was saved in database
        Task savedTask = taskRepository.findById(taskId).orElse(null);
        assertNotNull(savedTask);
        assertEquals("Integration Test Task", savedTask.getTitle());
        assertEquals(TaskStatus.TODO, savedTask.getStatus());

        // 2. Update the task
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Integration Test Task");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        updateRequest.setPriority(3);

        mockMvc.perform(put("/api/tasks/edit/" + taskId)
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value(3));

        // 3. Get all tasks
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Updated Integration Test Task"));

        // 4. Get task by ID
        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Test Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 5. Delete the task
        mockMvc.perform(delete("/api/tasks/delete/" + taskId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent());

        // Verify task was deleted from database
        assertFalse(taskRepository.findById(taskId).isPresent());

        // Verify no tasks remain
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // Try to access without token
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());

        // Try to create task without token
        TaskRequest request = new TaskRequest();
        request.setTitle("Unauthorized Task");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testInvalidToken() throws Exception {
        // Mock auth service to return false for invalid token
        mockAuthService.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("false")
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMultipleTasksCreation() throws Exception {
        // Setup mock responses for multiple calls
        for (int i = 0; i < 6; i++) {
            mockAuthService.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody("true")
                    .addHeader("Content-Type", "application/json"));
        }

        // Create multiple tasks
        for (int i = 1; i <= 3; i++) {
            TaskRequest request = new TaskRequest();
            request.setTitle("Task " + i);
            request.setDescription("Description for task " + i);
            request.setPriority(i);

            mockMvc.perform(post("/api/tasks")
                            .header("Authorization", "Bearer " + validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Verify all tasks were created in database
        assertEquals(3, taskRepository.count());

        // Get all tasks and verify count
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }
}