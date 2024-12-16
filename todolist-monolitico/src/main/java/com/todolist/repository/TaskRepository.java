package com.todolist.repository;

import com.todolist.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserId(UUID userId);
    List<Task> findByUserIdAndDueDate(UUID userId, LocalDate dueDate);
}

