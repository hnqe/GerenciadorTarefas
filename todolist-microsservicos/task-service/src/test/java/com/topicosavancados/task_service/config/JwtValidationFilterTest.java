package com.topicosavancados.task_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtValidationFilterTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private WebClient.RequestHeadersUriSpec requestUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private JwtValidationFilter jwtValidationFilter;

    @BeforeEach
    void setUp() {
        String authServiceUrl = "http://fake-auth-service";
        String secretKey = "MyUltraSecureSecretWithAtLeast32Bytes!!";  // >= 32 bytes

        when(webClientBuilder.baseUrl(authServiceUrl)).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        jwtValidationFilter = new JwtValidationFilter(webClientBuilder, authServiceUrl, secretKey);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_TokenMissing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtValidationFilter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");

        jwtValidationFilter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ValidTokenAuthServiceOk() throws Exception {
        // Preparação
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");

        JwtValidationFilter spyFilter = spy(jwtValidationFilter);

        io.jsonwebtoken.Claims claimsMock = mock(io.jsonwebtoken.Claims.class);
        when(claimsMock.getSubject()).thenReturn("testuser");
        when(claimsMock.get("role", String.class)).thenReturn("ROLE_ADMIN");
        when(claimsMock.get("userId", String.class))
                .thenReturn("11111111-1111-1111-1111-111111111111");

        doReturn(claimsMock).when(spyFilter).extractClaimsFromToken("validToken");

        doReturn(true).when(spyFilter).validateTokenWithAuthService("validToken");

        spyFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser",
                SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testValidateTokenWithAuthService_ReturnsTrue() {
        // O código real chama: webClient.get().uri(uriBuilder -> ...)...
        // Precisamos mockar com coringa '?' e cast, pois
        // uri(...) espera Function<UriBuilder, URI>, e a interface
        // RequestHeadersUriSpec<?> é genérica.

        when(webClient.get()).thenReturn(requestUriSpec);

        // 2) Cast explícito do 'thenReturn' para RequestHeadersSpec<?>
        when(requestUriSpec.uri(any(Function.class)))
                .thenReturn((WebClient.RequestHeadersSpec<?>) requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Precisamos retornar um Mono<Boolean> para bodyToMono:
        Mono<Boolean> monoBool = Mono.just(true);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(monoBool);

        boolean result = jwtValidationFilter.validateTokenWithAuthService("anyToken");

        assertTrue(result, "Deveria retornar true quando AuthService retorna true");

        verify(webClient).get();
        verify(requestUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
    }

    @Test
    void testValidateTokenWithAuthService_ThrowsException() {
        // Se por exemplo, a chamada "webClient.get()" lançar exceção, cai no catch e retorna false
        when(webClient.get()).thenThrow(new RuntimeException("Simulated error"));

        boolean result = jwtValidationFilter.validateTokenWithAuthService("anyToken");
        assertFalse(result, "Deveria retornar false se ocorrer exceção no try/catch");
    }

}
