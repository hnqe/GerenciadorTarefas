package com.topicosavancados.task_service.dto;

import com.topicosavancados.task_service.model.TaskStatus;

import java.util.UUID;

public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private UUID userId;
    private String priority;

    // Constructors
    public TaskRequest() {}

    public TaskRequest(String title, String description, TaskStatus status, UUID userId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.userId = userId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setPriority(int priority) {
        this.priority = String.valueOf(priority);
    }
}