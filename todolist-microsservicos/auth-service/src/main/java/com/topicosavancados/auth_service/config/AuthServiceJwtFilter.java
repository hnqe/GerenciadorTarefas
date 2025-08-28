package com.topicosavancados.auth_service.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class AuthServiceJwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceJwtFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip JWT processing for public endpoints
        String path = request.getRequestURI();
        if (path != null && (path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/validate-token"))) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);

        try {
            Claims claims = jwtTokenProvider.validateToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            if (role == null) {
                role = "USER";
            }
            
            // Adiciona o prefixo ROLE_ se não estiver presente
            String authorityRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(authorityRole));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            String userIdStr = claims.get("userId", String.class);
            if (userIdStr != null) {
                authentication.setDetails(UUID.fromString(userIdStr));
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

}
