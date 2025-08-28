package com.topicosavancados.pomodoro_service.service;

import com.topicosavancados.pomodoro_service.dto.CreateSessionRequest;
import com.topicosavancados.pomodoro_service.dto.SessionResponse;
import com.topicosavancados.pomodoro_service.model.PomodoroSession;
import com.topicosavancados.pomodoro_service.model.SessionStatus;
import com.topicosavancados.pomodoro_service.model.SessionType;
import com.topicosavancados.pomodoro_service.model.UserSettings;
import com.topicosavancados.pomodoro_service.repository.PomodoroSessionRepository;
import com.topicosavancados.pomodoro_service.service.UserSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroServiceTest {

    @Mock
    private PomodoroSessionRepository pomodoroSessionRepository;

    @Mock
    private UserSettingsService userSettingsService;

    @InjectMocks
    private PomodoroService pomodoroService;

    @Test
    void testCreateSession() {
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        CreateSessionRequest request = new CreateSessionRequest();
        request.setType(SessionType.FOCUS);
        request.setDurationMinutes(25);

        UserSettings userSettings = new UserSettings();
        userSettings.setFocusDurationMinutes(25);
        userSettings.setShortBreakDurationMinutes(5);
        userSettings.setLongBreakDurationMinutes(15);

        PomodoroSession session = new PomodoroSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setUsername(username);
        session.setType(SessionType.FOCUS);
        session.setPlannedDurationMinutes(25);
        session.setStatus(SessionStatus.WAITING);
        session.setCreatedAt(LocalDateTime.now());

        when(pomodoroSessionRepository.findByUserIdAndStatus(userId, SessionStatus.RUNNING))
                .thenReturn(Optional.empty());
        when(userSettingsService.getUserSettings(userId, username)).thenReturn(userSettings);
        when(pomodoroSessionRepository.save(any(PomodoroSession.class))).thenReturn(session);

        SessionResponse result = pomodoroService.createSession(userId, username, request);

        assertNotNull(result);
        assertEquals(SessionType.FOCUS, result.getType());
        assertEquals(25, result.getPlannedDurationMinutes());
        assertEquals(SessionStatus.WAITING, result.getStatus());
        verify(pomodoroSessionRepository, times(1)).save(any(PomodoroSession.class));
    }

    @Test
    void testStartSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        
        PomodoroSession session = new PomodoroSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setStatus(SessionStatus.WAITING);
        session.setType(SessionType.FOCUS);
        session.setPlannedDurationMinutes(25);

        when(pomodoroSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(pomodoroSessionRepository.save(any(PomodoroSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponse result = pomodoroService.startSession(userId, sessionId);

        assertNotNull(result);
        assertEquals(SessionStatus.RUNNING, result.getStatus());
        assertNotNull(result.getStartTime());
        verify(pomodoroSessionRepository, times(1)).findById(sessionId);
        verify(pomodoroSessionRepository, times(1)).save(session);
    }

    @Test
    void testPauseSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        
        PomodoroSession session = new PomodoroSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setStatus(SessionStatus.RUNNING);
        session.setStartTime(LocalDateTime.now().minusMinutes(10));
        session.setTotalPausedMinutes(0.0);

        when(pomodoroSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(pomodoroSessionRepository.save(any(PomodoroSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponse result = pomodoroService.pauseSession(userId, sessionId);

        assertNotNull(result);
        assertEquals(SessionStatus.PAUSED, result.getStatus());
        assertNotNull(result.getPausedAt());
        verify(pomodoroSessionRepository, times(1)).findById(sessionId);
        verify(pomodoroSessionRepository, times(1)).save(session);
    }

    @Test
    void testCompleteSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String notes = "Session completed successfully";
        
        PomodoroSession session = new PomodoroSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setStatus(SessionStatus.RUNNING);
        session.setStartTime(LocalDateTime.now().minusMinutes(25));

        when(pomodoroSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(pomodoroSessionRepository.save(any(PomodoroSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponse result = pomodoroService.completeSession(userId, sessionId, notes);

        assertNotNull(result);
        assertEquals(SessionStatus.COMPLETED, result.getStatus());
        assertEquals(notes, result.getNotes());
        assertNotNull(result.getEndTime());
        verify(pomodoroSessionRepository, times(1)).findById(sessionId);
        verify(pomodoroSessionRepository, times(1)).save(session);
    }

    @Test
    void testStopSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String notes = "Session stopped";
        
        PomodoroSession session = new PomodoroSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setStatus(SessionStatus.RUNNING);

        when(pomodoroSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(pomodoroSessionRepository.save(any(PomodoroSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponse result = pomodoroService.stopSession(userId, sessionId, notes);

        assertNotNull(result);
        assertEquals(SessionStatus.CANCELLED, result.getStatus());
        assertEquals(notes, result.getNotes());
        assertNotNull(result.getEndTime());
        verify(pomodoroSessionRepository, times(1)).findById(sessionId);
        verify(pomodoroSessionRepository, times(1)).save(session);
    }

    @Test
    void testGetCurrentSession() {
        UUID userId = UUID.randomUUID();
        
        PomodoroSession session = new PomodoroSession();
        session.setId(UUID.randomUUID());
        session.setUserId(userId);
        session.setStatus(SessionStatus.RUNNING);
        session.setType(SessionType.FOCUS);

        when(pomodoroSessionRepository.findByUserIdAndStatus(userId, SessionStatus.RUNNING))
                .thenReturn(Optional.of(session));

        SessionResponse result = pomodoroService.getCurrentSession(userId);

        assertNotNull(result);
        assertEquals(SessionStatus.RUNNING, result.getStatus());
        assertEquals(SessionType.FOCUS, result.getType());
        verify(pomodoroSessionRepository, times(1)).findByUserIdAndStatus(userId, SessionStatus.RUNNING);
    }

    @Test
    void testGetCurrentSession_NotFound() {
        UUID userId = UUID.randomUUID();
        
        when(pomodoroSessionRepository.findByUserIdAndStatus(userId, SessionStatus.RUNNING))
                .thenReturn(Optional.empty());
        when(pomodoroSessionRepository.findByUserIdAndStatus(userId, SessionStatus.PAUSED))
                .thenReturn(Optional.empty());

        SessionResponse result = pomodoroService.getCurrentSession(userId);

        assertNull(result);
        verify(pomodoroSessionRepository, times(1)).findByUserIdAndStatus(userId, SessionStatus.RUNNING);
        verify(pomodoroSessionRepository, times(1)).findByUserIdAndStatus(userId, SessionStatus.PAUSED);
    }

    @Test
    void testGetUserSessions() {
        UUID userId = UUID.randomUUID();
        
        PomodoroSession session1 = new PomodoroSession();
        session1.setId(UUID.randomUUID());
        session1.setUserId(userId);
        session1.setStatus(SessionStatus.COMPLETED);
        
        PomodoroSession session2 = new PomodoroSession();
        session2.setId(UUID.randomUUID());
        session2.setUserId(userId);
        session2.setStatus(SessionStatus.CANCELLED);

        List<PomodoroSession> sessions = Arrays.asList(session1, session2);
        when(pomodoroSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(sessions);

        List<SessionResponse> result = pomodoroService.getUserSessions(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(SessionStatus.COMPLETED, result.get(0).getStatus());
        assertEquals(SessionStatus.CANCELLED, result.get(1).getStatus());
        verify(pomodoroSessionRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void testGetSessionById() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        
        PomodoroSession session = new PomodoroSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setType(SessionType.SHORT_BREAK);
        session.setPlannedDurationMinutes(5);

        when(pomodoroSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        SessionResponse result = pomodoroService.getSessionById(userId, sessionId);

        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        assertEquals(SessionType.SHORT_BREAK, result.getType());
        assertEquals(5, result.getPlannedDurationMinutes());
        verify(pomodoroSessionRepository, times(1)).findById(sessionId);
    }

    @Test
    void testGetSessionById_NotFound() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        
        when(pomodoroSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> pomodoroService.getSessionById(userId, sessionId));

        assertEquals("Session not found", exception.getMessage());
        verify(pomodoroSessionRepository, times(1)).findById(sessionId);
    }
}