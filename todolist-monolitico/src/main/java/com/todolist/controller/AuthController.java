package com.todolist.controller;

import com.todolist.config.JwtTokenProvider;
import com.todolist.model.User;
import com.todolist.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    // Página de login personalizada
    @GetMapping("/login")
    public ModelAndView getLoginPage() {
        return new ModelAndView("login"); // Redireciona para login.html
    }

    // Página de registro personalizada
    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        return new ModelAndView("register"); // Redireciona para register.html
    }

    // Endpoint para login de usuário
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(username);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    // Endpoint para registro de novo usuário
    @PostMapping("/register")
    public ModelAndView registerUser(@RequestParam String username, @RequestParam String password) {
        // Verifica se o usuário já existe
        if (userService.existsByUsername(username)) {
            ModelAndView mav = new ModelAndView("register");
            mav.addObject("error", "Username is already taken!");
            return mav;
        }

        // Cria um novo usuário
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userService.createUser(user);

        // Redireciona para a página de login
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("success", "Registration successful! Please login.");
        return mav;
    }
}

class JwtResponse {
    private String token;

    public JwtResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}