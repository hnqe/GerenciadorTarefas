package com.topicosavancados.auth_service.service;

import com.topicosavancados.auth_service.config.JwtTokenProvider;
import com.topicosavancados.auth_service.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(jwtTokenProvider, userService);
    }

    @Test
    void testCreateJwtForUser() {
        // Mock do retorno de userService.getUserByUsername
        String username = "testuser";
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setRole("ADMIN");

        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        // Mock do retorno do jwtTokenProvider.generateToken
        String expectedToken = "mockJwtToken";
        when(jwtTokenProvider.generateToken(eq(username), eq(userId), eq("ROLE_ADMIN")))
                .thenReturn(expectedToken);

        // Executa o método
        String actualToken = authService.createJwtForUser(username);

        // Verifica o resultado
        assertEquals(expectedToken, actualToken);

        // Verifica as interações
        verify(userService, times(1)).getUserByUsername(username);
        verify(jwtTokenProvider, times(1))
                .generateToken(eq(username), eq(userId), eq("ROLE_ADMIN"));
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = "validToken";
        Claims claimsMock = mock(Claims.class); // Mock dos Claims

        // Configura o mock para retornar os claims esperados
        when(jwtTokenProvider.validateToken(token)).thenReturn(claimsMock);

        // Executa o método
        boolean isValid = authService.validateToken(token);

        // Verifica o resultado
        assertTrue(isValid);

        // Verifica a interação
        verify(jwtTokenProvider, times(1)).validateToken(token);
    }
    
    @Test
    void testValidateToken_InvalidToken() {
        String token = "invalidToken";

        // Mock do método jwtTokenProvider.validateToken para lançar exceção
        doThrow(new RuntimeException("Invalid token")).when(jwtTokenProvider).validateToken(token);

        // Executa o método
        boolean isValid = authService.validateToken(token);

        // Verifica o resultado
        assertFalse(isValid);

        // Verifica a interação
        verify(jwtTokenProvider, times(1)).validateToken(token);
    }
}