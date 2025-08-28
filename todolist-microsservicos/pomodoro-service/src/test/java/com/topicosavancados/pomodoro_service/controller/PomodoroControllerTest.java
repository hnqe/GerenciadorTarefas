package com.topicosavancados.pomodoro_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topicosavancados.pomodoro_service.dto.CreateSessionRequest;
import com.topicosavancados.pomodoro_service.dto.SessionResponse;
import com.topicosavancados.pomodoro_service.model.SessionStatus;
import com.topicosavancados.pomodoro_service.model.SessionType;
import com.topicosavancados.pomodoro_service.service.PomodoroService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PomodoroControllerTest {

    @Mock
    private PomodoroService pomodoroService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PomodoroController pomodoroController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pomodoroController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
        userId = UUID.randomUUID();
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/pomodoro/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("pomodoro-service"));
    }

    @Test
    void testCreateSession() throws Exception {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setType(SessionType.FOCUS);
        request.setDurationMinutes(25);

        SessionResponse response = new SessionResponse();
        response.setId(UUID.randomUUID());
        response.setType(SessionType.FOCUS);
        response.setStatus(SessionStatus.WAITING);
        response.setPlannedDurationMinutes(25);
        response.setCreatedAt(LocalDateTime.now());

        when(pomodoroService.createSession(any(UUID.class), any(String.class), any(CreateSessionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/pomodoro/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", userId)
                .requestAttr("username", "testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FOCUS"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.plannedDurationMinutes").value(25));

        verify(pomodoroService, times(1)).createSession(eq(userId), eq("testUser"), any(CreateSessionRequest.class));
    }

    @Test
    void testStartSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        
        SessionResponse response = new SessionResponse();
        response.setId(sessionId);
        response.setStatus(SessionStatus.RUNNING);
        response.setStartTime(LocalDateTime.now());

        when(pomodoroService.startSession(userId, sessionId)).thenReturn(response);

        mockMvc.perform(post("/api/pomodoro/sessions/{sessionId}/start", sessionId)
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));

        verify(pomodoroService, times(1)).startSession(userId, sessionId);
    }

    @Test
    void testPauseSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        
        SessionResponse response = new SessionResponse();
        response.setId(sessionId);
        response.setStatus(SessionStatus.PAUSED);
        response.setPausedAt(LocalDateTime.now());

        when(pomodoroService.pauseSession(userId, sessionId)).thenReturn(response);

        mockMvc.perform(post("/api/pomodoro/sessions/{sessionId}/pause", sessionId)
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"));

        verify(pomodoroService, times(1)).pauseSession(userId, sessionId);
    }

    @Test
    void testCompleteSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        String notes = "Session completed successfully";
        
        SessionResponse response = new SessionResponse();
        response.setId(sessionId);
        response.setStatus(SessionStatus.COMPLETED);
        response.setNotes(notes);
        response.setEndTime(LocalDateTime.now());

        when(pomodoroService.completeSession(userId, sessionId, notes)).thenReturn(response);

        Map<String, String> requestBody = Map.of("notes", notes);

        mockMvc.perform(post("/api/pomodoro/sessions/{sessionId}/complete", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.notes").value(notes));

        verify(pomodoroService, times(1)).completeSession(userId, sessionId, notes);
    }

    @Test
    void testStopSession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        String notes = "Session stopped";
        
        SessionResponse response = new SessionResponse();
        response.setId(sessionId);
        response.setStatus(SessionStatus.CANCELLED);
        response.setNotes(notes);

        when(pomodoroService.stopSession(userId, sessionId, notes)).thenReturn(response);

        Map<String, String> requestBody = Map.of("notes", notes);

        mockMvc.perform(post("/api/pomodoro/sessions/{sessionId}/stop", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.notes").value(notes));

        verify(pomodoroService, times(1)).stopSession(userId, sessionId, notes);
    }

    @Test
    void testGetCurrentSession() throws Exception {
        SessionResponse response = new SessionResponse();
        response.setId(UUID.randomUUID());
        response.setType(SessionType.FOCUS);
        response.setStatus(SessionStatus.RUNNING);

        when(pomodoroService.getCurrentSession(userId)).thenReturn(response);

        mockMvc.perform(get("/api/pomodoro/sessions/current")
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FOCUS"))
                .andExpect(jsonPath("$.status").value("RUNNING"));

        verify(pomodoroService, times(1)).getCurrentSession(userId);
    }

    @Test
    void testGetCurrentSession_NoContent() throws Exception {
        when(pomodoroService.getCurrentSession(userId)).thenReturn(null);

        mockMvc.perform(get("/api/pomodoro/sessions/current")
                .requestAttr("userId", userId))
                .andExpect(status().isNoContent());

        verify(pomodoroService, times(1)).getCurrentSession(userId);
    }

    @Test
    void testGetUserSessions() throws Exception {
        SessionResponse session1 = new SessionResponse();
        session1.setId(UUID.randomUUID());
        session1.setStatus(SessionStatus.COMPLETED);

        SessionResponse session2 = new SessionResponse();
        session2.setId(UUID.randomUUID());
        session2.setStatus(SessionStatus.CANCELLED);

        List<SessionResponse> sessions = Arrays.asList(session1, session2);
        when(pomodoroService.getUserSessions(userId)).thenReturn(sessions);

        mockMvc.perform(get("/api/pomodoro/sessions")
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"));

        verify(pomodoroService, times(1)).getUserSessions(userId);
    }

    @Test
    void testGetSessionById() throws Exception {
        UUID sessionId = UUID.randomUUID();
        
        SessionResponse response = new SessionResponse();
        response.setId(sessionId);
        response.setType(SessionType.SHORT_BREAK);
        response.setPlannedDurationMinutes(5);

        when(pomodoroService.getSessionById(userId, sessionId)).thenReturn(response);

        mockMvc.perform(get("/api/pomodoro/sessions/{sessionId}", sessionId)
                .requestAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SHORT_BREAK"))
                .andExpect(jsonPath("$.plannedDurationMinutes").value(5));

        verify(pomodoroService, times(1)).getSessionById(userId, sessionId);
    }
}