package com.topicosavancados.auth_service.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // O jwt.secret precisa ter ao menos 32 bytes (256 bits).
        String testSecret = "MyUltraSecureSecretWithAtLeast32Bytes!!";
        // Injeta o valor manualmente (usando reflection).
        TestReflectionUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
    }

    @Test
    void testGenerateAndValidateToken_Valid() {
        String username = "testUser";
        UUID userId = UUID.randomUUID();
        String role = "ROLE_ADMIN";

        String token = jwtTokenProvider.generateToken(username, userId, role);
        assertNotNull(token);

        Claims claims = jwtTokenProvider.validateToken(token);
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role", String.class));
        assertEquals(userId.toString(), claims.get("userId", String.class));
    }

    @Test
    void testValidateToken_InvalidSignature() {
        String username = "testUser";
        UUID userId = UUID.randomUUID();
        String role = "ROLE_ADMIN";

        String token = jwtTokenProvider.generateToken(username, userId, role);
        // Manipula a assinatura
        String invalidToken = token.substring(0, token.length() - 5) + "abcde";

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> jwtTokenProvider.validateToken(invalidToken),
                "Deveria falhar para token com assinatura inválida");
        assertTrue(thrown.getMessage().contains("Invalid JWT token"));
    }

    @Test
    void testValidateToken_Expired() {
        // Exemplo de token propositalmente inválido/expirado
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
                + "maisAlgumLixoParaForcarErro";

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> jwtTokenProvider.validateToken(expiredToken),
                "Deveria falhar para token expirado/inválido");
        assertTrue(thrown.getMessage().contains("Invalid JWT token"));
    }
}

class TestReflectionUtils {
    private TestReflectionUtils() {}
    public static void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}