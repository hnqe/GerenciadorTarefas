package com.topicosavancados.auth_service.service;

import com.topicosavancados.auth_service.config.JwtTokenProvider;
import com.topicosavancados.auth_service.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

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
            jwtTokenProvider.validateToken(token);
            return true;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}