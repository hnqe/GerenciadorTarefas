package com.todolist.service;

import com.todolist.exception.ResourceNotFoundException;
import com.todolist.model.User;
import com.todolist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Criptografa a senha
        user.setRole("USER"); // Define o papel padrão como USER (sem ROLE_)
        return userRepository.save(user);
    }

    public User createAdmin(User admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole("ADMIN"); // Define o papel como ADMIN (sem ROLE_)
        return userRepository.save(admin);
    }

    // Busca todos os usuários
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Busca usuário por ID
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Busca usuário por nome de usuário
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public List<User> getAllAdmins() {
        return userRepository.findByRole("ROLE_ADMIN");
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
