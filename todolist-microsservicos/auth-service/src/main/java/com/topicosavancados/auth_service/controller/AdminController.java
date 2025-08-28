package com.topicosavancados.auth_service.controller;

import com.topicosavancados.auth_service.dto.AdminStatsResponse;
import com.topicosavancados.auth_service.dto.UserSummaryResponse;
import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminDashboard() {
        return "Welcome to the admin panel!";
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatsResponse getSystemStats() {
        long totalUsers = userService.getTotalUsers();
        long totalAdmins = userService.getTotalAdmins();
        long totalRegularUsers = userService.getTotalRegularUsers();
        
        return new AdminStatsResponse(totalUsers, totalAdmins, totalRegularUsers);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummaryResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(user -> new UserSummaryResponse(user.getId(), user.getUsername(), user.getRole()))
                .collect(Collectors.toList());
    }
}

