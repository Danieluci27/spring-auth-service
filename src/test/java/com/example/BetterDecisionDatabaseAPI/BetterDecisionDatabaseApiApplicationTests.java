package com.example.BetterDecisionDatabaseAPI;
import com.example.BetterDecisionDatabaseAPI.controller.AuthController;
import com.example.BetterDecisionDatabaseAPI.controller.UserController;
import com.example.BetterDecisionDatabaseAPI.model.User;
import com.example.BetterDecisionDatabaseAPI.repository.UserRepository;
import com.example.BetterDecisionDatabaseAPI.service.EmailService;
import com.example.BetterDecisionDatabaseAPI.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BetterDecisionDatabaseApiApplicationTests {

    @Test
    void jwt_generateAndValidate_roundTrip() {
        JWTService svc = new JWTService();
        // inject test config
        ReflectionTestUtils.setField(svc, "secret", "0123456789_0123456789_0123456789_0123456789_");
        ReflectionTestUtils.setField(svc, "expirationMs", 5_000L);

        String token = svc.generateToken("alice");
        assertNotNull(token);
        assertEquals("alice", svc.extractUsername(token));
        assertTrue(svc.validateToken(token, "alice"));
        assertFalse(svc.validateToken(token, "bob"));
    }

    @Test
    void userMe_returnsGreeting_whenAuthenticationProvided() {
        UserRepository mockdb = mock(UserRepository.class);
        UserController userCtrl = new UserController(mockdb);
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", null, List.of());

        var response = userCtrl.getMyData(auth);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("alice"));
    }

    @Test
    void checkUserName_returnsTrue_whenUserNameExists() {
        UserRepository mockdb = mock(UserRepository.class);
        EmailService mockes = mock(EmailService.class);
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(mockdb.findByUsername("alice"))
                .thenReturn(Optional.of(new User("alice", "alice@example.com", "pw")));

        JWTService svc = new JWTService();
        AuthController authCtrl = new AuthController(svc, mockdb, mockes, redisTemplate, passwordEncoder);

        var response = authCtrl.checkUsername("alice");
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
    }

    @Test
    void checkEmail_returnsTrue_whenEmailExists() {
        UserRepository mockdb = mock(UserRepository.class);
        EmailService mockes = mock(EmailService.class);
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(mockdb.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new User("alice", "alice@example.com", "pw")));

        JWTService svc = new JWTService();
        AuthController authCtrl = new AuthController(svc, mockdb, mockes, redisTemplate, passwordEncoder);


        var response = authCtrl.checkEmail("alice@example.com");
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
    }
}
