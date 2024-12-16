package com.todolist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Column(name = "role")
    private String role; // Ex: "ROLE_USER", "ROLE_ADMIN"
}
