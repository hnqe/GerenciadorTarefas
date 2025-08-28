package com.topicosavancados.auth_service.config;

import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);
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
            logger.info("Admin user created with username: {}", adminUsername);
        } else {
            logger.info("Admin user already exists: {}", adminUsername);
        }
    }

}
