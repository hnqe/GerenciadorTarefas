package com.topicosavancados.auth_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, UUID userId, String role) {
        long jwtExpirationMs = 86400000; // 1 dia
        SecretKey key = getSigningKey();

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .claim("role", role) // Adiciona a role no token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            // Analisa e valida o token
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // Define a chave de assinatura
                    .build()
                    .parseClaimsJws(token)
                    .getBody(); // Retorna os claims do token
        } catch (Exception e) {
            // Lança uma exceção em caso de token inválido
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        }
    }
}
