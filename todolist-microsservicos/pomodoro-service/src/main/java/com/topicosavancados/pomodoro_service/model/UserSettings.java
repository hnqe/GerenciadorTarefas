package com.topicosavancados.pomodoro_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer focusDurationMinutes = 25;

    @Column(nullable = false)
    private Integer shortBreakDurationMinutes = 5;

    @Column(nullable = false)
    private Integer longBreakDurationMinutes = 15;

    @Column(nullable = false)
    private Integer sessionsUntilLongBreak = 4;

    @Column(nullable = false)
    private Boolean autoStartBreaks = false;

    @Column(nullable = false)
    private Boolean autoStartFocus = false;

    @Column(nullable = false)
    private Boolean soundEnabled = true;

    @Column(nullable = false)
    private Boolean notificationsEnabled = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public UserSettings() {}

    public UserSettings(UUID userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getFocusDurationMinutes() {
        return focusDurationMinutes;
    }

    public void setFocusDurationMinutes(Integer focusDurationMinutes) {
        this.focusDurationMinutes = focusDurationMinutes;
    }

    public Integer getShortBreakDurationMinutes() {
        return shortBreakDurationMinutes;
    }

    public void setShortBreakDurationMinutes(Integer shortBreakDurationMinutes) {
        this.shortBreakDurationMinutes = shortBreakDurationMinutes;
    }

    public Integer getLongBreakDurationMinutes() {
        return longBreakDurationMinutes;
    }

    public void setLongBreakDurationMinutes(Integer longBreakDurationMinutes) {
        this.longBreakDurationMinutes = longBreakDurationMinutes;
    }

    public Integer getSessionsUntilLongBreak() {
        return sessionsUntilLongBreak;
    }

    public void setSessionsUntilLongBreak(Integer sessionsUntilLongBreak) {
        this.sessionsUntilLongBreak = sessionsUntilLongBreak;
    }

    public Boolean getAutoStartBreaks() {
        return autoStartBreaks;
    }

    public void setAutoStartBreaks(Boolean autoStartBreaks) {
        this.autoStartBreaks = autoStartBreaks;
    }

    public Boolean getAutoStartFocus() {
        return autoStartFocus;
    }

    public void setAutoStartFocus(Boolean autoStartFocus) {
        this.autoStartFocus = autoStartFocus;
    }

    public Boolean getSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(Boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}