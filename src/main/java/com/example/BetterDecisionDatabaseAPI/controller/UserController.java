package com.example.BetterDecisionDatabaseAPI.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.BetterDecisionDatabaseAPI.repository.UserRepository;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyData(Authentication authentication) {
        // Authentication will contain the username from the JWT
        return ResponseEntity.ok("Hello, " + authentication.getName() + "! This is your data.");
    }
}