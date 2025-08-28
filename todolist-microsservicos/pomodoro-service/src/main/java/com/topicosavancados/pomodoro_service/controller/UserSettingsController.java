package com.topicosavancados.pomodoro_service.controller;

import com.topicosavancados.pomodoro_service.model.UserSettings;
import com.topicosavancados.pomodoro_service.service.UserSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pomodoro/settings")
@CrossOrigin(origins = "http://localhost:3000")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    @GetMapping
    public ResponseEntity<UserSettings> getUserSettings(HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");
        
        UserSettings settings = userSettingsService.getUserSettings(userId, username);
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<UserSettings> updateUserSettings(
            @RequestBody UserSettings updatedSettings,
            HttpServletRequest httpRequest) {
        
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");
        
        UserSettings settings = userSettingsService.updateUserSettings(userId, username, updatedSettings);
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/reset")
    public ResponseEntity<UserSettings> resetToDefaults(HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String username = (String) httpRequest.getAttribute("username");
        
        userSettingsService.resetToDefaults(userId, username);
        UserSettings settings = userSettingsService.getUserSettings(userId, username);
        return ResponseEntity.ok(settings);
    }
}