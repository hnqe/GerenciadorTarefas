package com.topicosavancados.task_service.repository;

import com.topicosavancados.task_service.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUsername(String username);

    List<Task> findByUsernameAndDueDate(String username, LocalDate dueDate);
}
