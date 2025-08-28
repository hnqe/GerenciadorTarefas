package com.topicosavancados.pomodoro_service.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PomodoroSessionTest {

    @Test
    void testPomodoroSessionCreation() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        
        PomodoroSession session = new PomodoroSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setUsername(username);
        session.setType(SessionType.FOCUS);
        session.setStatus(SessionStatus.WAITING);
        session.setPlannedDurationMinutes(25);
        session.setTotalPausedMinutes(0.0);

        assertEquals(sessionId, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(username, session.getUsername());
        assertEquals(SessionType.FOCUS, session.getType());
        assertEquals(SessionStatus.WAITING, session.getStatus());
        assertEquals(25, session.getPlannedDurationMinutes());
        assertEquals(0.0, session.getTotalPausedMinutes());
    }

    @Test
    void testPomodoroSessionTimestamps() {
        PomodoroSession session = new PomodoroSession();
        LocalDateTime now = LocalDateTime.now();
        
        session.setCreatedAt(now);
        session.setStartTime(now);
        session.setPausedAt(now);
        session.setEndTime(now);

        assertEquals(now, session.getCreatedAt());
        assertEquals(now, session.getStartTime());
        assertEquals(now, session.getPausedAt());
        assertEquals(now, session.getEndTime());
    }

    @Test
    void testPomodoroSessionNotes() {
        PomodoroSession session = new PomodoroSession();
        String notes = "This is a test session with some notes";
        
        session.setNotes(notes);
        
        assertEquals(notes, session.getNotes());
    }

    @Test
    void testSessionTypeValues() {
        assertEquals("FOCUS", SessionType.FOCUS.toString());
        assertEquals("SHORT_BREAK", SessionType.SHORT_BREAK.toString());
        assertEquals("LONG_BREAK", SessionType.LONG_BREAK.toString());
        assertEquals("CUSTOM", SessionType.CUSTOM.toString());
    }

    @Test
    void testSessionStatusValues() {
        assertEquals("WAITING", SessionStatus.WAITING.toString());
        assertEquals("RUNNING", SessionStatus.RUNNING.toString());
        assertEquals("PAUSED", SessionStatus.PAUSED.toString());
        assertEquals("COMPLETED", SessionStatus.COMPLETED.toString());
        assertEquals("CANCELLED", SessionStatus.CANCELLED.toString());
    }
}