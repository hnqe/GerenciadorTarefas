package com.topicosavancados.auth_service.service;

import com.topicosavancados.auth_service.exception.ResourceNotFoundException;
import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(UUID.randomUUID()); // Simula um ID gerado
            return savedUser;
        });

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals("USER", createdUser.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testCreateAdmin() {
        User admin = new User();
        admin.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(admin)).thenAnswer(invocation -> {
            User savedAdmin = invocation.getArgument(0);
            savedAdmin.setId(UUID.randomUUID()); // Simula um ID gerado
            return savedAdmin;
        });

        User createdAdmin = userService.createAdmin(admin);

        assertNotNull(createdAdmin);
        assertEquals("encodedPassword", createdAdmin.getPassword());
        assertEquals("ADMIN", createdAdmin.getRole());
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    void testGetUserByUsername_UserFound() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByUsername(username);

        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testGetUserByUsername_UserNotFound() {
        String username = "nonExistentUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByUsername(username));

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testExistsByUsername_UserExists() {
        String username = "existingUser";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean exists = userService.existsByUsername(username);

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsername(username);
    }

    @Test
    void testExistsByUsername_UserDoesNotExist() {
        String username = "nonExistentUser";

        when(userRepository.existsByUsername(username)).thenReturn(false);

        boolean exists = userService.existsByUsername(username);

        assertFalse(exists);
        verify(userRepository, times(1)).existsByUsername(username);
    }
}