package com.todolist.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    public String generateToken(String username) {
        // 1 dia em milissegundos
        long jwtExpirationMs = 86400000;
        // IMPORTANTE: Use uma chave mais segura e longa, armazenada de forma segura
        String jwtSecret = "yourSecretKeyMustBeLongerAndMoreSecureInRealApplication";

        // Converta a chave secreta em uma chave HMAC segura
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
}