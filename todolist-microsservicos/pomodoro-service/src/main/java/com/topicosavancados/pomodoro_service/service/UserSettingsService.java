package com.topicosavancados.pomodoro_service.service;

import com.topicosavancados.pomodoro_service.model.UserSettings;
import com.topicosavancados.pomodoro_service.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserSettingsService {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    public UserSettings getUserSettings(UUID userId, String username) {
        Optional<UserSettings> settings = userSettingsRepository.findByUserId(userId);
        
        if (settings.isEmpty()) {
            // Create default settings for new user
            UserSettings defaultSettings = new UserSettings(userId, username);
            return userSettingsRepository.save(defaultSettings);
        }
        
        return settings.get();
    }

    public UserSettings updateUserSettings(UUID userId, String username, UserSettings updatedSettings) {
        UserSettings settings = getUserSettings(userId, username);
        
        // Update all fields
        settings.setFocusDurationMinutes(updatedSettings.getFocusDurationMinutes());
        settings.setShortBreakDurationMinutes(updatedSettings.getShortBreakDurationMinutes());
        settings.setLongBreakDurationMinutes(updatedSettings.getLongBreakDurationMinutes());
        settings.setSessionsUntilLongBreak(updatedSettings.getSessionsUntilLongBreak());
        settings.setAutoStartBreaks(updatedSettings.getAutoStartBreaks());
        settings.setAutoStartFocus(updatedSettings.getAutoStartFocus());
        settings.setSoundEnabled(updatedSettings.getSoundEnabled());
        settings.setNotificationsEnabled(updatedSettings.getNotificationsEnabled());
        
        return userSettingsRepository.save(settings);
    }

    public void resetToDefaults(UUID userId, String username) {
        UserSettings settings = getUserSettings(userId, username);
        
        settings.setFocusDurationMinutes(25);
        settings.setShortBreakDurationMinutes(5);
        settings.setLongBreakDurationMinutes(15);
        settings.setSessionsUntilLongBreak(4);
        settings.setAutoStartBreaks(false);
        settings.setAutoStartFocus(false);
        settings.setSoundEnabled(true);
        settings.setNotificationsEnabled(true);
        
        userSettingsRepository.save(settings);
    }
}