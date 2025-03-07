package com.topicosavancados.task_service.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        // Mock do JwtValidationFilter (injetado no construtor)
        JwtValidationFilter jwtValidationFilterMock = mock(JwtValidationFilter.class);
        securityConfig = new SecurityConfig(jwtValidationFilterMock);
    }

    @Test
    void testSecurityFilterChain() throws Exception {
        // Mock do HttpSecurity usando RETURNS_DEEP_STUBS
        // para permitir encadeamento de chamadas (authorizeHttpRequests, etc.).
        HttpSecurity http = mock(HttpSecurity.class, Answers.RETURNS_DEEP_STUBS);

        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http);

        assertNotNull(filterChain);
    }

    @Test
    void testCorsConfigurationSource() {
        // Chama o bean
        UrlBasedCorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertNotNull(source);
        assertFalse(source.getCorsConfigurations().isEmpty());
    }

}
