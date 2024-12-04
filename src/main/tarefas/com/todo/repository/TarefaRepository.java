package com.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.todo.model.Tarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
}
