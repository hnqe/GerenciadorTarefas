package com.topicosavancados.auth_service.controller;

import com.topicosavancados.auth_service.config.AuthRequest;
import com.topicosavancados.auth_service.config.JwtResponse;
import com.topicosavancados.auth_service.model.User;
import com.topicosavancados.auth_service.service.AuthService;
import com.topicosavancados.auth_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          AuthService authService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.userService = userService;
    }

    // LOGIN (recebe username e password em JSON)
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest request) {
        // 1) Tentar autenticar
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2) Gera o token JWT
        String jwt = authService.createJwtForUser(request.getUsername());

        // 3) Retorna em JSON
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    // REGISTER (também recebe JSON)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        userService.createUser(user);

        return ResponseEntity.ok("Registration successful!");
    }

    // VALIDA TOKEN
    @GetMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}