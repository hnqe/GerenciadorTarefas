package com.topicosavancados.auth_service.config;

import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserService userService;

    public AdminInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        String adminUsername = "admin";

        if (!userService.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            userService.createAdmin(admin);
            System.out.println("Admin user created.");
            System.out.println("Role for user " + adminUsername + ": ROLE_" + admin.getRole());
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
