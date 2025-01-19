package com.topicosavancados.auth_service.service;

import com.topicosavancados.auth_service.exception.ResourceNotFoundException;
import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    // Busca usuário por nome de usuário
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}