package com.example.BetterDecisionDatabaseAPI.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<String> getMyData(Authentication authentication) {
        // Authentication will contain the username from the JWT
        return ResponseEntity.ok("Hello, " + authentication.getName() + "! This is your data.");
    }
}