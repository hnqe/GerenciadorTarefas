package com.topicosavancados.pomodoro_service.service;

import com.topicosavancados.pomodoro_service.dto.CreateSessionRequest;
import com.topicosavancados.pomodoro_service.dto.SessionResponse;
import com.topicosavancados.pomodoro_service.model.PomodoroSession;
import com.topicosavancados.pomodoro_service.model.SessionStatus;
import com.topicosavancados.pomodoro_service.model.SessionType;
import com.topicosavancados.pomodoro_service.model.UserSettings;
import com.topicosavancados.pomodoro_service.repository.PomodoroSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PomodoroService {

    @Autowired
    private PomodoroSessionRepository sessionRepository;

    @Autowired
    private UserSettingsService userSettingsService;

    public SessionResponse createSession(UUID userId, String username, CreateSessionRequest request) {
        // Check if user has an active session
        Optional<PomodoroSession> activeSession = sessionRepository.findByUserIdAndStatus(userId, SessionStatus.RUNNING);
        if (activeSession.isPresent()) {
            throw new RuntimeException("You already have an active session. Please stop it first.");
        }

        // Get user settings for default durations
        UserSettings settings = userSettingsService.getUserSettings(userId, username);
        
        Integer duration = request.getDurationMinutes();
        if (duration == null) {
            // Use default durations based on session type
            switch (request.getType()) {
                case FOCUS:
                    duration = settings.getFocusDurationMinutes();
                    break;
                case SHORT_BREAK:
                    duration = settings.getShortBreakDurationMinutes();
                    break;
                case LONG_BREAK:
                    duration = settings.getLongBreakDurationMinutes();
                    break;
                case CUSTOM:
                    duration = 25; // Default fallback
                    break;
            }
        }

        PomodoroSession session = new PomodoroSession(userId, username, request.getType(), duration);
        session.setTaskId(request.getTaskId());
        session.setTaskTitle(request.getTaskTitle());
        session.setNotes(request.getNotes());
        
        session = sessionRepository.save(session);
        return convertToResponse(session);
    }

    public SessionResponse startSession(UUID userId, UUID sessionId) {
        PomodoroSession session = getSessionByUserAndId(userId, sessionId);
        
        if (session.getStatus() != SessionStatus.WAITING && session.getStatus() != SessionStatus.PAUSED) {
            throw new RuntimeException("Session cannot be started in current state: " + session.getStatus());
        }

        // FIXED: Calculate paused time when resuming from pause with precise second-level accuracy
        if (session.getStatus() == SessionStatus.PAUSED && session.getPausedAt() != null) {
            // Calculate how long the session was paused in seconds (maximum precision)
            long pausedSeconds = ChronoUnit.SECONDS.between(session.getPausedAt(), LocalDateTime.now());
            // Add the precise seconds directly to avoid any conversion loss
            session.setTotalPausedSeconds(session.getTotalPausedSeconds() + (double) pausedSeconds);
            session.setPausedAt(null); // Clear the pausedAt timestamp
        }

        session.setStatus(SessionStatus.RUNNING);
        if (session.getStartTime() == null) {
            session.setStartTime(LocalDateTime.now());
        }
        
        session = sessionRepository.save(session);
        return convertToResponse(session);
    }

    public SessionResponse pauseSession(UUID userId, UUID sessionId) {
        PomodoroSession session = getSessionByUserAndId(userId, sessionId);
        
        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new RuntimeException("Only running sessions can be paused");
        }

        session.setStatus(SessionStatus.PAUSED);
        session.setPausedAt(LocalDateTime.now());
        
        // FIXED: Don't add paused time here - we'll add it when resuming
        // The pausedAt timestamp is sufficient to track when the pause started
        
        session = sessionRepository.save(session);
        return convertToResponse(session);
    }

    public SessionResponse stopSession(UUID userId, UUID sessionId, String notes) {
        PomodoroSession session = getSessionByUserAndId(userId, sessionId);
        
        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("Session is already finished");
        }

        session.setStatus(SessionStatus.CANCELLED);
        session.setEndTime(LocalDateTime.now());
        
        if (notes != null && !notes.trim().isEmpty()) {
            session.setNotes(notes);
        }

        // Calculate actual duration
        if (session.getStartTime() != null) {
            long actualMinutes = ChronoUnit.MINUTES.between(session.getStartTime(), LocalDateTime.now());
            session.setActualDurationMinutes((int) (actualMinutes - session.getTotalPausedMinutes()));
        }
        
        session = sessionRepository.save(session);
        return convertToResponse(session);
    }

    public SessionResponse completeSession(UUID userId, UUID sessionId, String notes) {
        PomodoroSession session = getSessionByUserAndId(userId, sessionId);
        
        if (session.getStatus() != SessionStatus.RUNNING && session.getStatus() != SessionStatus.PAUSED) {
            throw new RuntimeException("Only running or paused sessions can be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        
        if (notes != null && !notes.trim().isEmpty()) {
            session.setNotes(notes);
        }

        // Calculate actual duration
        if (session.getStartTime() != null) {
            long actualMinutes = ChronoUnit.MINUTES.between(session.getStartTime(), LocalDateTime.now());
            session.setActualDurationMinutes((int) (actualMinutes - session.getTotalPausedMinutes()));
        }
        
        session = sessionRepository.save(session);
        return convertToResponse(session);
    }

    public SessionResponse getCurrentSession(UUID userId) {
        Optional<PomodoroSession> activeSession = sessionRepository.findByUserIdAndStatus(userId, SessionStatus.RUNNING);
        if (activeSession.isEmpty()) {
            // Check for paused sessions
            activeSession = sessionRepository.findByUserIdAndStatus(userId, SessionStatus.PAUSED);
        }
        
        return activeSession.map(this::convertToResponse).orElse(null);
    }

    public List<SessionResponse> getUserSessions(UUID userId) {
        List<PomodoroSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<SessionResponse> getSessionsByTask(UUID userId, UUID taskId) {
        List<PomodoroSession> sessions = sessionRepository.findByUserIdAndTaskIdOrderByCreatedAtDesc(userId, taskId);
        return sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public SessionResponse getSessionById(UUID userId, UUID sessionId) {
        PomodoroSession session = getSessionByUserAndId(userId, sessionId);
        return convertToResponse(session);
    }

    private PomodoroSession getSessionByUserAndId(UUID userId, UUID sessionId) {
        Optional<PomodoroSession> session = sessionRepository.findById(sessionId);
        if (session.isEmpty()) {
            throw new RuntimeException("Session not found");
        }
        
        if (!session.get().getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to access this session");
        }
        
        return session.get();
    }

    private SessionResponse convertToResponse(PomodoroSession session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setUserId(session.getUserId());
        response.setUsername(session.getUsername());
        response.setTaskId(session.getTaskId());
        response.setTaskTitle(session.getTaskTitle());
        response.setType(session.getType());
        response.setStatus(session.getStatus());
        response.setPlannedDurationMinutes(session.getPlannedDurationMinutes());
        response.setActualDurationMinutes(session.getActualDurationMinutes());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setPausedAt(session.getPausedAt());
        response.setTotalPausedMinutes(session.getTotalPausedMinutes());
        response.setNotes(session.getNotes());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());

        // Calculate remaining minutes for running sessions
        if (session.getStatus() == SessionStatus.RUNNING && session.getStartTime() != null) {
            long elapsedMinutes = ChronoUnit.MINUTES.between(session.getStartTime(), LocalDateTime.now());
            int remainingMinutes = session.getPlannedDurationMinutes() - (int) elapsedMinutes + session.getTotalPausedMinutes().intValue();
            response.setRemainingMinutes(Math.max(0, remainingMinutes));
        }

        return response;
    }
}