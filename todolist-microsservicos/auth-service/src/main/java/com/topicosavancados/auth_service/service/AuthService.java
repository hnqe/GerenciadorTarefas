package com.topicosavancados.auth_service.service;

import com.topicosavancados.auth_service.config.JwtTokenProvider;
import com.topicosavancados.auth_service.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService; // Serviço para lidar com usuários

    public AuthService(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    public String createJwtForUser(String username) {
        User user = userService.getUserByUsername(username);
        UUID userId = user.getId();
        // Supondo user.getRole() = "ADMIN" ou "USER"
        String roleWithPrefix = "ROLE_" + user.getRole();
        return jwtTokenProvider.generateToken(username, userId, roleWithPrefix);
    }

    public boolean validateToken(String token) {
        try {
            // Valida o token usando o JwtTokenProvider
            jwtTokenProvider.validateToken(token);
            return true;
        } catch (Exception e) {
            // Log para debugging
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }
}