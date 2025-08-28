package com.topicosavancados.pomodoro_service.repository;

import com.topicosavancados.pomodoro_service.model.PomodoroSession;
import com.topicosavancados.pomodoro_service.model.SessionStatus;
import com.topicosavancados.pomodoro_service.model.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, UUID> {

    List<PomodoroSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<PomodoroSession> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, SessionStatus status);

    Optional<PomodoroSession> findByUserIdAndStatus(UUID userId, SessionStatus status);

    List<PomodoroSession> findByUserIdAndTaskIdOrderByCreatedAtDesc(UUID userId, UUID taskId);

    List<PomodoroSession> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, SessionType type);

    @Query("SELECT COUNT(s) FROM PomodoroSession s WHERE s.userId = :userId AND s.status = 'COMPLETED' AND s.type = 'FOCUS'")
    Long countCompletedFocusSessionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT SUM(s.actualDurationMinutes) FROM PomodoroSession s WHERE s.userId = :userId AND s.status = 'COMPLETED' AND s.type = 'FOCUS'")
    Long sumFocusMinutesByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM PomodoroSession s WHERE s.userId = :userId AND s.createdAt >= :startDate AND s.createdAt <= :endDate ORDER BY s.createdAt DESC")
    List<PomodoroSession> findByUserIdAndDateRange(@Param("userId") UUID userId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM PomodoroSession s WHERE s.userId = :userId AND s.status = 'COMPLETED' AND s.type = 'FOCUS' AND s.createdAt >= :startOfDay AND s.createdAt <= :endOfDay")
    Long countTodayCompletedFocusSessionsByUserId(@Param("userId") UUID userId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    Optional<PomodoroSession> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT s FROM PomodoroSession s WHERE s.userId = :userId AND s.status IN :statuses ORDER BY s.createdAt DESC")
    Optional<PomodoroSession> findByUserIdAndStatusIn(@Param("userId") UUID userId, @Param("statuses") List<SessionStatus> statuses);
}