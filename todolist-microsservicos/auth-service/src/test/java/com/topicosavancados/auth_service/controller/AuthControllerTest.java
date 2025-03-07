package com.topicosavancados.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topicosavancados.auth_service.dto.AuthRequest;
import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.service.AuthService;
import com.topicosavancados.auth_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Monta um MockMvc standalone sem filtros de segurança
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Retorna 200 e um JWT ao fazer login com credenciais válidas (standalone)")
    void testAuthenticateUser_Valid() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "testpass"));

        when(authService.createJwtForUser("testuser")).thenReturn("fake-jwt-token");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    @DisplayName("Retorna 400 quando o username já existe no registro (standalone)")
    void testRegisterUser_UsernameAlreadyExists() throws Exception {
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("existingUser");
        registerRequest.setPassword("anyPass");

        when(userService.existsByUsername("existingUser")).thenReturn(true);

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken!"));
    }

    @Test
    @DisplayName("Retorna 200 ao registrar novo usuário (standalone)")
    void testRegisterUser_Success() throws Exception {
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername("newUser");
        registerRequest.setPassword("123456");

        when(userService.existsByUsername("newUser")).thenReturn(false);
        when(userService.createUser(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful!"));
    }

}
