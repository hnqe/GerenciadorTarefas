package com.topicosavancados.task_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidationFilter.class);
    protected final WebClient webClient;
    private final String secretKey;

    public JwtValidationFilter(WebClient.Builder webClientBuilder,
                               @Value("${auth-service.url}") String authServiceUrl,
                               @Value("${jwt.secret}") String secretKey) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
        this.secretKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {

        String token = extractTokenFromRequest(request);
        logger.info("Authorization token received: {}", token);

        if (token == null) {
            logger.warn("Authorization token is missing");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // Extrai os claims (subject = username, userId, role, etc.)
            Claims claims = extractClaimsFromToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);  // ex: "ROLE_ADMIN" ou "ROLE_USER"
            if (role == null) {
                // Fallback se não vier nada
                role = "ROLE_USER";
            }

            // userId no claim
            UUID userId = UUID.fromString(claims.get("userId", String.class));
            logger.info("Parsed token: username={}, userId={}, role={}", username, userId, role);

            // Opcional: Validar o token chamando o AuthService
            Boolean isValid = validateTokenWithAuthService(token);

            if (Boolean.TRUE.equals(isValid)) {
                logger.info("Token is valid for user: {}", username);

                // Cria as authorities conforme a role do token
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(role));

                // Configura Authentication
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                // userId como details, se quiser usar depois
                authentication.setDetails(userId);

                // Insere no SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Continua o fluxo
                filterChain.doFilter(request, response);
            } else {
                logger.warn("Token validation failed for user: {}", username);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            logger.error("Error during token validation: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null; // Apenas aceita token no cabeçalho
    }

    private Claims extractClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Error extracting claims from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Opcional: Validar token consultando o AuthService
     * (Ex.: GET /api/auth/validate-token?token=xxx)
     */
    private Boolean validateTokenWithAuthService(String token) {
        try {
            // Chamada ao AuthService para confirmar que o token não foi revogado etc.
            Boolean isValid = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/auth/validate-token")
                            .queryParam("token", token)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(isValid);
        } catch (Exception e) {
            logger.error("Error validating token with AuthService: {}", e.getMessage(), e);
            return false;
        }
    }
}