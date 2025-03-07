package com.topicosavancados.auth_service.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceJwtFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AuthServiceJwtFilter authServiceJwtFilter;

    @BeforeEach
    void setUp() {
        // Instancia manualmente a classe a ser testada, injetando o jwtTokenProvider mock
        authServiceJwtFilter = new AuthServiceJwtFilter(jwtTokenProvider);
        // Limpa o contexto de segurança antes de cada teste
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_withValidToken() throws Exception {
        // Cenário
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");

        // Mock dos claims
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testuser");
        when(claims.get("role", String.class)).thenReturn("ROLE_ADMIN");
        when(claims.get("userId", String.class))
                .thenReturn("11111111-1111-1111-1111-111111111111");

        // Mock do provider
        when(jwtTokenProvider.validateToken("validToken")).thenReturn(claims);

        // Execução
        authServiceJwtFilter.doFilterInternal(request, response, filterChain);

        // Verificações
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser",
                SecurityContextHolder.getContext().getAuthentication().getName());

        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withMissingHeader() throws Exception {
        // Não tem Authorization
        when(request.getHeader("Authorization")).thenReturn(null);

        authServiceJwtFilter.doFilterInternal(request, response, filterChain);

        // Verifica que chamou o chain, mas não setou security context
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withInvalidToken() throws Exception {
        // Authorization presente mas token inválido
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(jwtTokenProvider.validateToken("invalidToken"))
                .thenThrow(new RuntimeException("Invalid token"));

        authServiceJwtFilter.doFilterInternal(request, response, filterChain);

        // Verifica que o SecurityContext foi limpo
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

}
