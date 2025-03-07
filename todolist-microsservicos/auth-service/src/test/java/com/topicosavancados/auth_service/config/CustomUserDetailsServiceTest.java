package com.topicosavancados.auth_service.config;

import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    // O @InjectMocks faz com que o userRepository seja injetado
    // automaticamente dentro de customUserDetailsService.
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_UserFound() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("encryptedPass");
        user.setRole("ADMIN");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encryptedPass", userDetails.getPassword());
        // Verifica que a authority é "ROLE_ADMIN"
        assertTrue(
                userDetails.getAuthorities().stream()
                        .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"))
        );

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        String username = "nonExistentUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(username)
        );

        verify(userRepository, times(1)).findByUsername(username);
    }

}
