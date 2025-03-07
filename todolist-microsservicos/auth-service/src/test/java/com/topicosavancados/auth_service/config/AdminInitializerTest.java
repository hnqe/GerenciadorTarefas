package com.topicosavancados.auth_service.config;

import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminInitializer adminInitializer;

    @Test
    void run_WhenAdminNotExists_CreatesAdmin() {
        // Admin não existe
        when(userService.existsByUsername("admin")).thenReturn(false);

        adminInitializer.run();

        // Verifica se chamou createAdmin
        verify(userService, times(1)).createAdmin(any(User.class));
    }

    @Test
    void run_WhenAdminExists_DoNothing() {
        // Admin já existe
        when(userService.existsByUsername("admin")).thenReturn(true);

        adminInitializer.run();

        // Não chama createAdmin
        verify(userService, never()).createAdmin(any(User.class));
    }

}
