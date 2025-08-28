package com.topicosavancados.task_service.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class TestJwtHelper {
    
    private static final String SECRET = "myVeryLongAndSecureSecretKeyForHS512AlgorithmThatMustBeAtLeast512BitsLongToMeetJWTSpecificationRequirements";
    
    public static String generateValidToken(String username, UUID userId, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}