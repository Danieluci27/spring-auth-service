package com.example.BetterDecisionDatabaseAPI.config;

import com.example.BetterDecisionDatabaseAPI.model.User;
import com.example.BetterDecisionDatabaseAPI.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("alice", "alice@example.com", passwordEncoder.encode("password1")));
                userRepository.save(new User("bob", "bob@example.com", passwordEncoder.encode("p@ssw0rd")));
                userRepository.save(new User("user", "user@example.com", passwordEncoder.encode("password")));
                System.out.println("Test users successfully inserted into MongoDB");
            }
        };
    }
}