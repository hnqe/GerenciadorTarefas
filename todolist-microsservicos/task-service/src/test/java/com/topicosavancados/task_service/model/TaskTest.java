package com.topicosavancados.task_service.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testGetSetId() {
        Task task = new Task();
        UUID randomId = UUID.randomUUID();
        task.setId(randomId);

        assertEquals(randomId, task.getId());
    }

    @Test
    void testAllGettersAndSetters() {
        Task task = new Task();

        // ID
        UUID randomId = UUID.randomUUID();
        task.setId(randomId);
        assertEquals(randomId, task.getId());

        // Title
        String title = "Test Title";
        task.setTitle(title);
        assertEquals(title, task.getTitle());

        // Description
        String desc = "Some description";
        task.setDescription(desc);
        assertEquals(desc, task.getDescription());

        // DueDate
        LocalDate dueDate = LocalDate.now();
        task.setDueDate(dueDate);
        assertEquals(dueDate, task.getDueDate());

        // Status
        task.setStatus(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        // Priority
        String priority = "HIGH";
        task.setPriority(priority);
        assertEquals(priority, task.getPriority());

        // Username
        String username = "someUser";
        task.setUsername(username);
        assertEquals(username, task.getUsername());

        // UserId
        UUID userId = UUID.randomUUID();
        task.setUserId(userId);
        assertEquals(userId, task.getUserId());
    }

    @Test
    void testConstructorAllArgs() {
        UUID id = UUID.randomUUID();
        String title = "Constructor Title";
        String desc = "Constructor Desc";
        LocalDate date = LocalDate.of(2025, 1, 1);
        TaskStatus status = TaskStatus.IN_PROGRESS;
        String priority = "CRITICAL";
        String username = "constructUser";
        UUID userId = UUID.randomUUID();

        Task task = new Task(id, title, desc, date, status, priority, username, userId);

        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(desc, task.getDescription());
        assertEquals(date, task.getDueDate());
        assertEquals(status, task.getStatus());
        assertEquals(priority, task.getPriority());
        assertEquals(username, task.getUsername());
        assertEquals(userId, task.getUserId());
    }

}
