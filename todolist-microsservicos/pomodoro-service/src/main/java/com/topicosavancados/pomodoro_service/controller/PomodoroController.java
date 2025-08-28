package com.topicosavancados.pomodoro_service.controller;

import com.topicosavancados.pomodoro_service.dto.CreateSessionRequest;
import com.topicosavancados.pomodoro_service.dto.SessionResponse;
import com.topicosavancados.pomodoro_service.service.PomodoroService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pomodoro")
@CrossOrigin(origins = "http://localhost:3000")
public class PomodoroController {

    @Autowired
    private PomodoroService pomodoroService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "pomodoro-service"));
    }

    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> createSession(
            @RequestBody CreateSessionRequest request,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");
        
        SessionResponse session = pomodoroService.createSession(userId, username, request);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<SessionResponse> startSession(
            @PathVariable UUID sessionId,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        SessionResponse session = pomodoroService.startSession(userId, sessionId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/pause")
    public ResponseEntity<SessionResponse> pauseSession(
            @PathVariable UUID sessionId,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        SessionResponse session = pomodoroService.pauseSession(userId, sessionId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/stop")
    public ResponseEntity<SessionResponse> stopSession(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String notes = body != null ? body.get("notes") : null;
        
        SessionResponse session = pomodoroService.stopSession(userId, sessionId, notes);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<SessionResponse> completeSession(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String notes = body != null ? body.get("notes") : null;
        
        SessionResponse session = pomodoroService.completeSession(userId, sessionId, notes);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/current")
    public ResponseEntity<SessionResponse> getCurrentSession(HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        SessionResponse session = pomodoroService.getCurrentSession(userId);
        
        if (session == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getUserSessions(HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        List<SessionResponse> sessions = pomodoroService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/task/{taskId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByTask(
            @PathVariable UUID taskId,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        List<SessionResponse> sessions = pomodoroService.getSessionsByTask(userId, taskId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getSessionById(
            @PathVariable UUID sessionId,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        SessionResponse session = pomodoroService.getSessionById(userId, sessionId);
        return ResponseEntity.ok(session);
    }
}