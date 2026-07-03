package com.example.backend;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminSecurityIntegrationTest {

    @LocalServerPort int port;
    final RestTemplate rt = new RestTemplate();
    String baseUrl;

    @BeforeAll
    void init() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    @Test
    void shouldAllowAdminDashboardForAdminToken() {
        String token = login("admin", "admin123");
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);

        var resp = rt.exchange(baseUrl + "/admin/dashboard", HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        assertEquals(20000, resp.getBody().get("code"));
    }

    @Test
    void shouldRejectAdminDashboardWithoutToken() {
        try {
            rt.getForObject(baseUrl + "/admin/dashboard", Map.class);
            fail("Expected forbidden response");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

    @Test
    void shouldRejectAdminDashboardForStudentToken() {
        String username = "studguard";
        rt.postForObject(baseUrl + "/auth/register",
                Map.of("username", username, "password", "pass123", "studentId", "231234099", "realName", "Student"), Map.class);
        String token = login(username, "pass123");
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);

        try {
            rt.exchange(baseUrl + "/admin/dashboard", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            fail("Expected forbidden response");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

    private String login(String username, String password) {
        var resp = rt.postForObject(baseUrl + "/auth/login",
                Map.of("username", username, "password", password), Map.class);
        return (String) ((Map<?, ?>) resp.get("data")).get("token");
    }
}
