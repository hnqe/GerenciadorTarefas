package com.todolist.repository;

import com.todolist.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(String role);
    Optional<User> findByUsername(String username); // Atualizado para retornar Optional
    boolean existsByUsername(String username);     // Para verificar se o usuário já existe
}