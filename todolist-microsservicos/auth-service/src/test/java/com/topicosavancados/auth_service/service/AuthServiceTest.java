package com.topicosavancados.auth_service.service;

import com.topicosavancados.auth_service.config.JwtTokenProvider;
import com.topicosavancados.auth_service.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AuthService
 *
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @Test
    void testCreateJwtForUser() {
        String username = "testuser";
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setRole("ADMIN");

        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        String expectedToken = "mockJwtToken";
        when(jwtTokenProvider.generateToken(eq(username), eq(userId), eq("ROLE_ADMIN")))
                .thenReturn(expectedToken);

        String actualToken = authService.createJwtForUser(username);

        assertEquals(expectedToken, actualToken);
        verify(userService).getUserByUsername(username);
        verify(jwtTokenProvider).generateToken(username, userId, "ROLE_ADMIN");
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = "validToken";
        Claims claimsMock = mock(Claims.class);
        when(jwtTokenProvider.validateToken(token)).thenReturn(claimsMock);

        boolean isValid = authService.validateToken(token);

        assertTrue(isValid);
        verify(jwtTokenProvider).validateToken(token);
    }
    
    @Test
    void testValidateToken_InvalidToken() {
        String token = "invalidToken";
        doThrow(new RuntimeException("Invalid token")).when(jwtTokenProvider).validateToken(token);

        boolean isValid = authService.validateToken(token);

        assertFalse(isValid);
        verify(jwtTokenProvider).validateToken(token);
    }

}