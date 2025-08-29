package com.example.BetterDecisionDatabaseAPI;
import com.example.BetterDecisionDatabaseAPI.controller.UserController;
import com.example.BetterDecisionDatabaseAPI.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        UserController userCtrl = new UserController();
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", null, List.of());

        var response = userCtrl.getMyData(auth);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("alice"));
    }
}
