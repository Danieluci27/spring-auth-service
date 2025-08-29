package com.example.BetterDecisionDatabaseAPI.config;

import com.example.BetterDecisionDatabaseAPI.model.User;
import com.example.BetterDecisionDatabaseAPI.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("alice", "alice@example.com", "password1"));
                userRepository.save(new User("bob", "bob@example.com", "p@ssw0rd"));
                userRepository.save(new User("user", "user@example.com", "password"));
                System.out.println("Test users successfully inserted into MongoDB");
            }
        };
    }
}