package com.example.BetterDecisionDatabaseAPI.controller;

import com.example.BetterDecisionDatabaseAPI.repository.UserRepository;
import com.example.BetterDecisionDatabaseAPI.service.JWTService;
import com.example.BetterDecisionDatabaseAPI.dto.LoginRequest;
import com.example.BetterDecisionDatabaseAPI.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JWTService jwt;

    private static final Map<String, String> USERS = new ConcurrentHashMap<>();
    static {
        // username -> plaintext password (mock DB). Replace with PasswordEncoder later.
        USERS.put("user", "password");
        USERS.put("alice", "password1");
        USERS.put("bob", "p@ssw0rd");
    }

    public AuthController(JWTService jwt) { this.jwt = jwt; }

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        return userRepository.findByUsername(req.username())
                .filter(user -> user.getPassword().equals(req.password()))
                .map(user -> ResponseEntity.ok(new TokenResponse(jwt.generateToken(user.getUsername()))))
                .orElse(ResponseEntity.status(401).build());
    }
}