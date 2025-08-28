package com.topicosavancados.pomodoro_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topicosavancados.pomodoro_service.dto.CreateSessionRequest;
import com.topicosavancados.pomodoro_service.dto.SessionResponse;
import com.topicosavancados.pomodoro_service.integration.TestJwtHelper;
import com.topicosavancados.pomodoro_service.model.PomodoroSession;
import com.topicosavancados.pomodoro_service.model.SessionStatus;
import com.topicosavancados.pomodoro_service.model.SessionType;
import com.topicosavancados.pomodoro_service.model.UserSettings;
import com.topicosavancados.pomodoro_service.repository.PomodoroSessionRepository;
import com.topicosavancados.pomodoro_service.repository.UserSettingsRepository;
import org.junit.jupiter.api.AfterEach;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class PomodoroServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("pomodoro_service_test")
            .withUsername("test")
            .withPassword("test");


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    private String validJwtToken;
    private UUID testUserId;
    private UUID testTaskId;

    @BeforeEach
    void setUp() throws Exception {
        pomodoroSessionRepository.deleteAll();
        userSettingsRepository.deleteAll();
        
        // Setup test data
        testUserId = UUID.fromString("12345678-1234-1234-1234-123456789012");
        testTaskId = UUID.fromString("87654321-4321-4321-4321-210987654321");
        validJwtToken = TestJwtHelper.generateValidToken("testuser", testUserId, "ROLE_USER");
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/pomodoro/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("pomodoro-service"));
    }

    @Test
    void testCompletePomodoroSessionFlow() throws Exception {

        // 1. Create a new pomodoro session
        CreateSessionRequest createRequest = new CreateSessionRequest();
        createRequest.setType(SessionType.FOCUS);
        createRequest.setDurationMinutes(25);
        createRequest.setTaskId(testTaskId);
        createRequest.setTaskTitle("Test Task");
        createRequest.setNotes("Integration test session");

        MvcResult createResult = mockMvc.perform(post("/api/pomodoro/sessions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FOCUS"))
                .andExpect(jsonPath("$.plannedDurationMinutes").value(25))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.taskId").value(testTaskId.toString()))
                .andExpect(jsonPath("$.taskTitle").value("Test Task"))
                .andReturn();

        // Extract session ID from response
        String responseContent = createResult.getResponse().getContentAsString();
        SessionResponse createdSession = objectMapper.readValue(responseContent, SessionResponse.class);
        UUID sessionId = createdSession.getId();

        // Verify session was saved in database
        PomodoroSession savedSession = pomodoroSessionRepository.findById(sessionId).orElse(null);
        assertNotNull(savedSession);
        assertEquals(SessionType.FOCUS, savedSession.getType());
        assertEquals(SessionStatus.WAITING, savedSession.getStatus());

        // 2. Start the session
        mockMvc.perform(post("/api/pomodoro/sessions/" + sessionId + "/start")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.startTime").exists());

        // 3. Pause the session
        mockMvc.perform(post("/api/pomodoro/sessions/" + sessionId + "/pause")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"))
                .andExpect(jsonPath("$.pausedAt").exists());

        // 4. Resume by starting again
        mockMvc.perform(post("/api/pomodoro/sessions/" + sessionId + "/start")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));

        // 5. Complete the session with notes
        Map<String, String> completeRequest = Map.of("notes", "Session completed successfully!");

        mockMvc.perform(post("/api/pomodoro/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.notes").value("Session completed successfully!"))
                .andExpect(jsonPath("$.endTime").exists());

        // 6. Get session by ID
        mockMvc.perform(get("/api/pomodoro/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.notes").value("Session completed successfully!"));

        // 7. Get all user sessions
        mockMvc.perform(get("/api/pomodoro/sessions")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        // 8. Get sessions by task
        mockMvc.perform(get("/api/pomodoro/sessions/task/" + testTaskId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].taskId").value(testTaskId.toString()));
    }

    @Test
    void testUserSettingsManagement() throws Exception {

        // 1. Get default user settings (should create if not exists)
        MvcResult settingsResult = mockMvc.perform(get("/api/pomodoro/settings")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.focusDurationMinutes").value(25))
                .andExpect(jsonPath("$.shortBreakDurationMinutes").value(5))
                .andExpect(jsonPath("$.longBreakDurationMinutes").value(15))
                .andReturn();

        // Extract settings from response
        String responseContent = settingsResult.getResponse().getContentAsString();
        UserSettings currentSettings = objectMapper.readValue(responseContent, UserSettings.class);

        // 2. Update user settings
        UserSettings updatedSettings = new UserSettings();
        updatedSettings.setFocusDurationMinutes(30);
        updatedSettings.setShortBreakDurationMinutes(10);
        updatedSettings.setLongBreakDurationMinutes(20);
        updatedSettings.setSoundEnabled(false);
        updatedSettings.setNotificationsEnabled(false);

        mockMvc.perform(put("/api/pomodoro/settings")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedSettings)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.focusDurationMinutes").value(30))
                .andExpect(jsonPath("$.shortBreakDurationMinutes").value(10))
                .andExpect(jsonPath("$.longBreakDurationMinutes").value(20))
                .andExpect(jsonPath("$.soundEnabled").value(false));

        // 3. Reset settings to defaults
        mockMvc.perform(post("/api/pomodoro/settings/reset")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.focusDurationMinutes").value(25))
                .andExpect(jsonPath("$.shortBreakDurationMinutes").value(5))
                .andExpect(jsonPath("$.longBreakDurationMinutes").value(15))
                .andExpect(jsonPath("$.soundEnabled").value(true));
    }

    @Test
    void testCurrentSessionManagement() throws Exception {

        // 1. Check for current session when none exists
        mockMvc.perform(get("/api/pomodoro/sessions/current")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent());

        // 2. Create and start a session
        CreateSessionRequest createRequest = new CreateSessionRequest();
        createRequest.setType(SessionType.SHORT_BREAK);
        createRequest.setDurationMinutes(5);

        MvcResult createResult = mockMvc.perform(post("/api/pomodoro/sessions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        SessionResponse createdSession = objectMapper.readValue(responseContent, SessionResponse.class);
        UUID sessionId = createdSession.getId();

        // Start the session
        mockMvc.perform(post("/api/pomodoro/sessions/" + sessionId + "/start")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk());

        // 3. Check current session - should return the running session
        mockMvc.perform(get("/api/pomodoro/sessions/current")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.type").value("SHORT_BREAK"));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // Try to access without token
        mockMvc.perform(get("/api/pomodoro/sessions"))
                .andExpect(status().isForbidden());

        // Try to create session without token
        CreateSessionRequest request = new CreateSessionRequest();
        request.setType(SessionType.FOCUS);
        request.setDurationMinutes(25);

        mockMvc.perform(post("/api/pomodoro/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testInvalidToken() throws Exception {

        mockMvc.perform(get("/api/pomodoro/sessions")
                        .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMultipleSessionsCreation() throws Exception {

        // Create multiple sessions of different types
        SessionType[] sessionTypes = {SessionType.FOCUS, SessionType.SHORT_BREAK, SessionType.LONG_BREAK};
        
        for (int i = 0; i < sessionTypes.length; i++) {
            CreateSessionRequest request = new CreateSessionRequest();
            request.setType(sessionTypes[i]);
            request.setDurationMinutes(25 - i * 5);
            request.setTaskTitle("Test Task " + (i + 1));

            mockMvc.perform(post("/api/pomodoro/sessions")
                            .header("Authorization", "Bearer " + validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // Verify all sessions were created in database
        assertEquals(3, pomodoroSessionRepository.count());

        // Get all sessions and verify count
        mockMvc.perform(get("/api/pomodoro/sessions")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void testSessionStopFunctionality() throws Exception {

        // Create and start a session
        CreateSessionRequest createRequest = new CreateSessionRequest();
        createRequest.setType(SessionType.CUSTOM);
        createRequest.setDurationMinutes(45);

        MvcResult createResult = mockMvc.perform(post("/api/pomodoro/sessions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        SessionResponse createdSession = objectMapper.readValue(responseContent, SessionResponse.class);
        UUID sessionId = createdSession.getId();

        // Start the session
        mockMvc.perform(post("/api/pomodoro/sessions/" + sessionId + "/start")
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk());

        // Stop the session with notes
        Map<String, String> stopRequest = Map.of("notes", "Stopped due to interruption");

        mockMvc.perform(post("/api/pomodoro/sessions/" + sessionId + "/stop")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.notes").value("Stopped due to interruption"))
                .andExpect(jsonPath("$.endTime").exists());
    }
}