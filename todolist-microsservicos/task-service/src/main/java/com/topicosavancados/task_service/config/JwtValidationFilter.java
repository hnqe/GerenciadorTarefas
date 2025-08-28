package com.topicosavancados.task_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

import java.io.IOException;
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
                                    FilterChain filterChain) throws IOException, ServletException {

        String token = extractTokenFromRequest(request);
        logger.info("Authorization token received: {}", token);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = extractClaimsFromToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            if (role == null) {
                role = "USER";
            }
            
            String authorityRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            UUID userId = UUID.fromString(claims.get("userId", String.class));
            logger.info("Parsed token: username={}, userId={}, role={}", username, userId, role);
            Boolean isValid = validateTokenWithAuthService(token);

            if (Boolean.TRUE.equals(isValid)) {
                logger.info("Token is valid for user: {}", username);

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(authorityRole));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(userId);

                SecurityContextHolder.getContext().setAuthentication(authentication);
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
        return null;
    }

    protected Claims extractClaimsFromToken(String token) {
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
    protected Boolean validateTokenWithAuthService(String token) {
        try {
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