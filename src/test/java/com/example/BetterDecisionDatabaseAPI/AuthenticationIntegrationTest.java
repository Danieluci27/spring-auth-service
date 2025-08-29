package com.example.BetterDecisionDatabaseAPI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthIntegrationTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }
    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0");

    @LocalServerPort
    private int port;

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    void loginAndMeFlow() {
        // given: DataLoader should have inserted "alice" with password "password1"

        String baseUrl = "http://localhost:" + port;

        // Step 1: login
        String loginBody = "{\"username\":\"alice\",\"password\":\"password1\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> loginReq = new HttpEntity<>(loginBody, headers);

        ResponseEntity<String> loginRes = rest.postForEntity(
                baseUrl + "/auth/login",
                loginReq,
                String.class);

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginRes.getBody()).contains("token");

        // extract token
        String token = loginRes.getBody().split(":\"")[1].replace("\"}", "");

        // Step 2: call /user/me
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);
        HttpEntity<Void> meReq = new HttpEntity<>(authHeaders);

        ResponseEntity<String> meRes = rest.exchange(
                baseUrl + "/user/me",
                HttpMethod.GET,
                meReq,
                String.class);

        assertThat(meRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meRes.getBody()).contains("alice");
    }

    @Test
    void RegisterAndloginAndMeFlow() {
        // given: DataLoader should have inserted "alice" with password "password1"

        String baseUrl = "http://localhost:" + port;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Step 1: Register new user
        String registerBody = "{\"username\":\"daniel\",\"email\":\"daniel@example.com\",\"password\":\"pwjdwj123\"}";
        HttpEntity<String> registerReq = new HttpEntity<>(registerBody, headers);

        ResponseEntity<String> registerRes = rest.postForEntity(
                baseUrl + "/auth/register",
                registerReq,
                String.class);

        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Step 2: login
        // Case #1: wrong credentials on the first try
        String loginBody = "{\"username\":\"daniel\",\"password\":\"pwjdw123\"}";
        HttpEntity<String> loginReq = new HttpEntity<>(loginBody, headers);

        ResponseEntity<String> loginRes = rest.postForEntity(
                baseUrl + "/auth/login",
                loginReq,
                String.class);

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        //Case #2: right credentials on the second try
        loginBody = "{\"username\":\"daniel\",\"password\":\"pwjdwj123\"}";
        loginReq = new HttpEntity<>(loginBody, headers);

        loginRes = rest.postForEntity(
                baseUrl + "/auth/login",
                loginReq,
                String.class);

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginRes.getBody()).contains("token");

        // extract token
        String token = loginRes.getBody().split(":\"")[1].replace("\"}", "");

        // Step 3: call /user/me
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(token);
        HttpEntity<Void> meReq = new HttpEntity<>(authHeaders);

        ResponseEntity<String> meRes = rest.exchange(
                baseUrl + "/user/me",
                HttpMethod.GET,
                meReq,
                String.class);

        assertThat(meRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meRes.getBody()).contains("daniel");
    }
}