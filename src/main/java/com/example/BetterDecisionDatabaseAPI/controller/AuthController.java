package com.example.BetterDecisionDatabaseAPI.controller;

import com.example.BetterDecisionDatabaseAPI.repository.UserRepository;
import com.example.BetterDecisionDatabaseAPI.service.EmailService;
import com.example.BetterDecisionDatabaseAPI.service.JWTService;
import com.example.BetterDecisionDatabaseAPI.dto.LoginRequest;
import com.example.BetterDecisionDatabaseAPI.model.User;
import com.example.BetterDecisionDatabaseAPI.dto.RegistrationRequest;
import com.example.BetterDecisionDatabaseAPI.dto.TokenResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JWTService jwt;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JWTService jwt, UserRepository userRepository, EmailService emailService, RedisTemplate<String, String> redisTemplate, PasswordEncoder passwordEncoder) {
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        return userRepository.findByUsername(req.username())
                .filter(user -> passwordEncoder.matches(req.password(), user.getPassword()))
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

    @PostMapping("/find/request")
    public ResponseEntity<String> requestVerification(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        return userRepository.findByEmail(email)
                .map(user -> {
                    String code = String.valueOf((int)(Math.random() * 900000) + 100000);
                    redisTemplate.opsForValue().set("verification:" + email, code, 2, TimeUnit.MINUTES);
                    emailService.sendVerificationCode(email, code);
                    return ResponseEntity.ok("Verification code sent to email");
                })
                .orElse(ResponseEntity.status(404).body("Email not found"));
    }

    @PostMapping("/find/verify")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String code = req.get("code");
        String expectedCode = redisTemplate.opsForValue().get("verification:" + email);
        if (expectedCode != null && expectedCode.equals(code)) {
            return userRepository.findByEmail(email)
                    .map(user -> {
                        redisTemplate.delete("verification:" + email);
                        String resetToken = UUID.randomUUID().toString();
                        String username = user.getUsername();
                        redisTemplate.opsForValue().set("reset_token:" + resetToken, user.getUsername(), 5, TimeUnit.MINUTES);
                        Map<String, String> response = Map.of(
                                "resetToken", resetToken,
                                "username", username
                        );
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.status(404).body(Map.of("error", "Email not found")));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired code"));
    }
    @PutMapping("/password/update")
    public ResponseEntity<String> updatePassword(@RequestBody Map<String, String> req) {
        String resetToken = req.get("resetToken");
        String username = req.get("username");
        String retrievedUsername = redisTemplate.opsForValue().get("reset_token:" + resetToken);
        if (resetToken == null || !username.equals(retrievedUsername)) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
        return userRepository.findByUsername(username)
                .map(user -> {
                    String newPassword = req.get("newPassword");
                    if (newPassword == null || newPassword.isEmpty()) {
                        return ResponseEntity.status(400).body("New password must not be empty");
                    }
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    redisTemplate.delete("reset_token:" + resetToken);
                    return ResponseEntity.ok("Password was successfully updated");
                })
                // 4. If not found, return 404
                .orElse(ResponseEntity.status(404).body("User not found"));
    }
}