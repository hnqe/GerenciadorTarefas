package com.topicosavancados.task_service.service;

import com.topicosavancados.task_service.model.Task;
import com.topicosavancados.task_service.model.TaskStatus;
import com.topicosavancados.task_service.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTasks() {
        String username = "user1";
        List<Task> tasks = List.of(new Task(UUID.randomUUID(), "Task 1", "Description", LocalDate.now(), TaskStatus.CONCLUIDO, "High", username, UUID.randomUUID()));
        when(taskRepository.findByUsername(username)).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks(username);

        assertEquals(tasks.size(), result.size());
        assertEquals(tasks.get(0).getTitle(), result.get(0).getTitle());
        verify(taskRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetTasksByDueDate() {
        String username = "user1";
        LocalDate dueDate = LocalDate.now();
        List<Task> tasks = List.of(new Task(UUID.randomUUID(), "Task 1", "Description", dueDate, TaskStatus.PENDENTE, "High", username, UUID.randomUUID()));
        when(taskRepository.findByUsernameAndDueDate(username, dueDate)).thenReturn(tasks);

        List<Task> result = taskService.getTasksByDueDate(username, dueDate);

        assertEquals(tasks.size(), result.size());
        assertEquals(tasks.get(0).getDueDate(), result.get(0).getDueDate());
        verify(taskRepository, times(1)).findByUsernameAndDueDate(username, dueDate);
    }

    @Test
    void testCreateTask() {
        Task task = new Task(UUID.randomUUID(), "New Task", "Description", LocalDate.now(), TaskStatus.EM_ANDAMENTO, "High", null, null);

        String username = "user1";
        UUID userId = UUID.randomUUID();

        when(authentication.getName()).thenReturn(username);
        when(authentication.getDetails()).thenReturn(userId);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(task, authentication);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(userId, result.getUserId());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testUpdateTask() {
        UUID taskId = UUID.randomUUID();
        Task existingTask = new Task(taskId, "Old Task", "Description", LocalDate.now(), TaskStatus.EM_ANDAMENTO,"Medium", "user1", UUID.randomUUID());
        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", LocalDate.now(), TaskStatus.CONCLUIDO, "High", "user1", UUID.randomUUID());

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.updateTask(taskId, updatedTask);

        assertNotNull(result);
        assertEquals(updatedTask.getTitle(), result.getTitle());
        assertEquals(updatedTask.getStatus(), result.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    void testUpdateTaskNotFound() {
        UUID taskId = UUID.randomUUID();
        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", LocalDate.now(), TaskStatus.CONCLUIDO, "High", "user1", UUID.randomUUID());

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.updateTask(taskId, updatedTask));

        assertEquals("Task not found with ID: " + taskId, exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testDeleteTask() {
        UUID taskId = UUID.randomUUID();

        doNothing().when(taskRepository).deleteById(taskId);

        assertDoesNotThrow(() -> taskService.deleteTask(taskId));

        verify(taskRepository, times(1)).deleteById(taskId);
    }
}