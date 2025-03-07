package com.topicosavancados.task_service.controller;

import com.topicosavancados.task_service.model.Task;
import com.topicosavancados.task_service.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskController taskController;

    @Test
    void testGetTasks() {
        when(authentication.getName()).thenReturn("testuser");

        Task t1 = new Task(); t1.setTitle("Task 1");
        Task t2 = new Task(); t2.setTitle("Task 2");
        List<Task> tasks = List.of(t1, t2);

        when(taskService.getAllTasks("testuser")).thenReturn(tasks);

        List<Task> result = taskController.getTasks(authentication);
        assertEquals(2, result.size());
        verify(taskService).getAllTasks("testuser");
    }

    @Test
    void testCreateTask() {
        Task newTask = new Task();
        newTask.setTitle("New Task");

        when(taskService.createTask(any(Task.class), any(Authentication.class)))
                .thenReturn(newTask);

        Task result = taskController.createTask(newTask, authentication);

        assertNotNull(result);
        assertEquals("New Task", result.getTitle());

        verify(taskService).createTask(newTask, authentication);
    }

    @Test
    void testUpdateTask() {
        UUID taskId = UUID.randomUUID();
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");

        when(taskService.updateTask(taskId, updatedTask)).thenReturn(updatedTask);

        Task result = taskController.updateTask(taskId, updatedTask);

        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        verify(taskService).updateTask(taskId, updatedTask);
    }

    @Test
    void testDeleteTask() {
        UUID taskId = UUID.randomUUID();

        doNothing().when(taskService).deleteTask(taskId);

        taskController.deleteTask(taskId);

        verify(taskService).deleteTask(taskId);
    }

}