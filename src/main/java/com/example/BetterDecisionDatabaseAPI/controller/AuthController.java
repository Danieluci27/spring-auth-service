package com.example.BetterDecisionDatabaseAPI.controller;

import com.example.BetterDecisionDatabaseAPI.repository.UserRepository;
import com.example.BetterDecisionDatabaseAPI.service.JWTService;
import com.example.BetterDecisionDatabaseAPI.dto.LoginRequest;
import com.example.BetterDecisionDatabaseAPI.model.User;
import com.example.BetterDecisionDatabaseAPI.dto.RegistrationRequest;
import com.example.BetterDecisionDatabaseAPI.dto.TokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JWTService jwt;
    private final UserRepository userRepository;

    public AuthController(JWTService jwt, UserRepository userRepository) {
        this.jwt = jwt;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        return userRepository.findByUsername(req.username())
                .filter(user -> user.getPassword().equals(req.password()))
                .map(user -> ResponseEntity.ok(new TokenResponse(jwt.generateToken(user.getUsername()))))
                .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationRequest req) {
        String safeUsername = HtmlUtils.htmlEscape(req.username()); //Prevent XSS

        if (userRepository.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.status(409).body("You already have account. Please login.");
        }
        else if (userRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.status(409).body(safeUsername + " already exists. Choose different username");
        }

        userRepository.save(new User(req.username(), req.email(), req.password()));
        return ResponseEntity.status(201).body(safeUsername + " created");
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        Boolean nameExist = userRepository.findByUsername(username).isPresent();
        return ResponseEntity.ok(nameExist);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        Boolean emailExist = userRepository.findByEmail(email).isPresent();
        return ResponseEntity.ok(emailExist);
    }
}