package com.todolist.config;

import com.todolist.model.User;
import com.todolist.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserService userService;

    public AdminInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminUsername = "admin";

        if (!userService.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword("admin123"); // Defina a senha padrão aqui (de preferência codificada)
            admin.setRole("ADMIN");
            userService.createAdmin(admin);
            System.out.println("Admin user created.");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}


