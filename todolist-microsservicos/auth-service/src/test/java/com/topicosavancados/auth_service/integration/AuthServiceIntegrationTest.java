package com.topicosavancados.auth_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topicosavancados.auth_service.dto.AuthRequest;
import com.topicosavancados.auth_service.dto.JwtResponse;
import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("auth_service_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testCompleteAuthenticationFlow() throws Exception {
        // 1. Register a new user
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("integrationuser");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful!"));

        // Verify user was saved in database
        User savedUser = userRepository.findByUsername("integrationuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("integrationuser", savedUser.getUsername());

        // 2. Login with the registered user
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("integrationuser");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extract token from response
        String responseContent = loginResult.getResponse().getContentAsString();
        JwtResponse loginResponse = objectMapper.readValue(responseContent, JwtResponse.class);
        String token = loginResponse.getToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 3. Validate token
        mockMvc.perform(get("/api/auth/validate-token")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 4. Try to access protected endpoint with token (this should work after we implement it)
        // For now, just verify the token validation endpoint works
    }

    @Test
    void testInvalidLogin() throws Exception {
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDuplicateRegistration() throws Exception {
        // Register user first time
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("duplicateuser");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Try to register same user again
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken!"));
    }

    @Test
    void testDatabasePersistence() throws Exception {
        // Register multiple users
        for (int i = 1; i <= 3; i++) {
            AuthRequest registerRequest = new AuthRequest();
            registerRequest.setUsername("user" + i);
            registerRequest.setPassword("password" + i);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk());
        }

        // Verify all users were persisted
        assertEquals(3, userRepository.count());
        
        assertTrue(userRepository.findByUsername("user1").isPresent());
        assertTrue(userRepository.findByUsername("user2").isPresent());
        assertTrue(userRepository.findByUsername("user3").isPresent());
    }

    @Test
    void testJwtTokenValidation() throws Exception {
        // Register and login to get a token
        AuthRequest request = new AuthRequest();
        request.setUsername("tokenuser");
        request.setPassword("password123");

        // Register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Login and get token
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        JwtResponse loginResponse = objectMapper.readValue(responseContent, JwtResponse.class);
        String token = loginResponse.getToken();

        // Test valid token
        mockMvc.perform(get("/api/auth/validate-token")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Test invalid token
        mockMvc.perform(get("/api/auth/validate-token")
                        .param("token", "invalid.token.here"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}