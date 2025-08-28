package com.topicosavancados.pomodoro_service.service;

import com.topicosavancados.pomodoro_service.model.UserSettings;
import com.topicosavancados.pomodoro_service.repository.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @InjectMocks
    private UserSettingsService userSettingsService;

    @Test
    void testGetUserSettings_ExistingUser() {
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        
        UserSettings existingSettings = new UserSettings(userId, username);
        existingSettings.setFocusDurationMinutes(30);
        existingSettings.setShortBreakDurationMinutes(10);

        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(existingSettings));

        UserSettings result = userSettingsService.getUserSettings(userId, username);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(username, result.getUsername());
        assertEquals(30, result.getFocusDurationMinutes());
        assertEquals(10, result.getShortBreakDurationMinutes());
        verify(userSettingsRepository, times(1)).findByUserId(userId);
        verify(userSettingsRepository, never()).save(any());
    }

    @Test
    void testGetUserSettings_NewUser() {
        UUID userId = UUID.randomUUID();
        String username = "newUser";

        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings result = userSettingsService.getUserSettings(userId, username);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(username, result.getUsername());
        // Default values
        assertEquals(25, result.getFocusDurationMinutes());
        assertEquals(5, result.getShortBreakDurationMinutes());
        assertEquals(15, result.getLongBreakDurationMinutes());
        assertEquals(4, result.getSessionsUntilLongBreak());
        assertFalse(result.getAutoStartBreaks());
        assertFalse(result.getAutoStartFocus());
        assertTrue(result.getSoundEnabled());
        assertTrue(result.getNotificationsEnabled());
        
        verify(userSettingsRepository, times(1)).findByUserId(userId);
        verify(userSettingsRepository, times(1)).save(any(UserSettings.class));
    }

    @Test
    void testUpdateUserSettings() {
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        
        UserSettings existingSettings = new UserSettings(userId, username);
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(existingSettings));
        when(userSettingsRepository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings updatedSettings = new UserSettings(userId, username);
        updatedSettings.setFocusDurationMinutes(30);
        updatedSettings.setShortBreakDurationMinutes(10);
        updatedSettings.setLongBreakDurationMinutes(20);
        updatedSettings.setSessionsUntilLongBreak(3);
        updatedSettings.setAutoStartBreaks(true);
        updatedSettings.setAutoStartFocus(true);
        updatedSettings.setSoundEnabled(false);
        updatedSettings.setNotificationsEnabled(false);

        UserSettings result = userSettingsService.updateUserSettings(userId, username, updatedSettings);

        assertNotNull(result);
        assertEquals(30, result.getFocusDurationMinutes());
        assertEquals(10, result.getShortBreakDurationMinutes());
        assertEquals(20, result.getLongBreakDurationMinutes());
        assertEquals(3, result.getSessionsUntilLongBreak());
        assertTrue(result.getAutoStartBreaks());
        assertTrue(result.getAutoStartFocus());
        assertFalse(result.getSoundEnabled());
        assertFalse(result.getNotificationsEnabled());
        
        verify(userSettingsRepository, times(1)).findByUserId(userId);
        verify(userSettingsRepository, times(1)).save(existingSettings);
    }

    @Test
    void testResetToDefaults() {
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        
        UserSettings existingSettings = new UserSettings(userId, username);
        existingSettings.setFocusDurationMinutes(30); // Different from default
        existingSettings.setShortBreakDurationMinutes(10); // Different from default
        
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(existingSettings));
        when(userSettingsRepository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        userSettingsService.resetToDefaults(userId, username);

        // Verify all values are reset to defaults
        assertEquals(25, existingSettings.getFocusDurationMinutes());
        assertEquals(5, existingSettings.getShortBreakDurationMinutes());
        assertEquals(15, existingSettings.getLongBreakDurationMinutes());
        assertEquals(4, existingSettings.getSessionsUntilLongBreak());
        assertFalse(existingSettings.getAutoStartBreaks());
        assertFalse(existingSettings.getAutoStartFocus());
        assertTrue(existingSettings.getSoundEnabled());
        assertTrue(existingSettings.getNotificationsEnabled());
        
        verify(userSettingsRepository, times(1)).findByUserId(userId);
        verify(userSettingsRepository, times(1)).save(existingSettings);
    }
}