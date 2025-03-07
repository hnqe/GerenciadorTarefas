package com.topicosavancados.auth_service.service;

import com.topicosavancados.auth_service.exception.ResourceNotFoundException;
import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UserService.
 *
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(UUID.randomUUID()); // simula ID gerado pelo banco
            return savedUser;
        });

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals("USER", createdUser.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateAdmin() {
        User admin = new User();
        admin.setPassword("adminPass");

        when(passwordEncoder.encode("adminPass")).thenReturn("encodedAdminPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedAdmin = invocation.getArgument(0);
            savedAdmin.setId(UUID.randomUUID());
            return savedAdmin;
        });

        User createdAdmin = userService.createAdmin(admin);

        assertNotNull(createdAdmin);
        assertEquals("encodedAdminPass", createdAdmin.getPassword());
        assertEquals("ADMIN", createdAdmin.getRole());
        verify(userRepository, times(1)).save(any(User.class));
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
        String missingUsername = "missingUser";

        when(userRepository.findByUsername(missingUsername)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByUsername(missingUsername));
        assertTrue(ex.getMessage().contains(missingUsername));

        verify(userRepository, times(1)).findByUsername(missingUsername);
    }

    @Test
    void testExistsByUsername_UserExists() {
        String existingUsername = "existingUser";
        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        boolean exists = userService.existsByUsername(existingUsername);

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsername(existingUsername);
    }

    @Test
    void testExistsByUsername_UserDoesNotExist() {
        String nonExistentUser = "nonExistentUser";
        when(userRepository.existsByUsername(nonExistentUser)).thenReturn(false);

        boolean exists = userService.existsByUsername(nonExistentUser);

        assertFalse(exists);
        verify(userRepository, times(1)).existsByUsername(nonExistentUser);
    }
}