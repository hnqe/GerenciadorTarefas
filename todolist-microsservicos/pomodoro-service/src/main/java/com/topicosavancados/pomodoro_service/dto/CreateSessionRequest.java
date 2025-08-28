package com.topicosavancados.pomodoro_service.dto;

import com.topicosavancados.pomodoro_service.model.SessionType;

import java.util.UUID;

public class CreateSessionRequest {
    
    private SessionType type;
    private Integer durationMinutes;
    private UUID taskId;
    private String taskTitle;
    private String notes;

    // Constructors
    public CreateSessionRequest() {}

    public CreateSessionRequest(SessionType type, Integer durationMinutes) {
        this.type = type;
        this.durationMinutes = durationMinutes;
    }

    // Getters and Setters
    public SessionType getType() {
        return type;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}