package com.topicosavancados.pomodoro_service.repository;

import com.topicosavancados.pomodoro_service.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {

    Optional<UserSettings> findByUserId(UUID userId);
    
    Optional<UserSettings> findByUsername(String username);
    
    boolean existsByUserId(UUID userId);
}