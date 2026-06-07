package com.example.backend;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @LocalServerPort int port;
    final RestTemplate rt = new RestTemplate();
    String baseUrl;

    @BeforeAll
    void init() { baseUrl = "http://localhost:" + port + "/api"; }

    @SuppressWarnings("unchecked")
    @Test
    @org.junit.jupiter.api.Order(1)
    void shouldRegister() {
        var resp = rt.postForObject(baseUrl + "/auth/register",
                Map.of("username","itest_auth","password","pass123","studentId","201250020","realName","AuthTest"), Map.class);
        assertEquals(20000, resp.get("code"));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void shouldRejectDuplicateRegistration() {
        try {
            rt.postForObject(baseUrl + "/auth/register",
                    Map.of("username","itest_auth","password","pass123","studentId","201250021","realName","Dup"), Map.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.junit.jupiter.api.Order(3)
    void shouldLogin() {
        var resp = rt.postForObject(baseUrl + "/auth/login",
                Map.of("username","itest_auth","password","pass123"), Map.class);
        assertEquals(20000, resp.get("code"));
        assertNotNull(((Map<?,?>)resp.get("data")).get("token"));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void shouldRejectWrongPassword() {
        try {
            rt.postForObject(baseUrl + "/auth/login",
                    Map.of("username","itest_auth","password","wrong"), Map.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.junit.jupiter.api.Order(5)
    void shouldAccessProtectedApi() {
        var loginResp = rt.postForObject(baseUrl + "/auth/login",
                Map.of("username","itest_auth","password","pass123"), Map.class);
        String token = (String) ((Map<?,?>)loginResp.get("data")).get("token");

        var headers = new HttpHeaders(); headers.setBearerAuth(token);
        var resp = rt.exchange(baseUrl + "/users/me", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        assertEquals(20000, resp.getBody().get("code"));
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    void shouldRejectMissingToken() {
        try {
            rt.getForObject(baseUrl + "/users/me", Map.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    void shouldRejectInvalidToken() {
        try {
            var headers = new HttpHeaders(); headers.setBearerAuth("invalid.token.here");
            rt.exchange(baseUrl + "/users/me", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
    }
}
