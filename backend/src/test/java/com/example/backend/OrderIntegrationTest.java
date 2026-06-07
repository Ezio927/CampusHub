package com.example.backend;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderIntegrationTest {

    @LocalServerPort int port;
    final RestTemplate rt = new RestTemplate();
    String baseUrl;
    String pubToken;

    @SuppressWarnings("unchecked")
    @BeforeAll
    void setUp() throws Exception {
        baseUrl = "http://localhost:" + port + "/api";
        Thread.sleep(15000); // rate limit from prior tests
        var resp = rt.postForObject(baseUrl + "/auth/login", Map.of("username","admin","password","admin123"), Map.class);
        pubToken = (String) ((Map<?,?>)resp.get("data")).get("token");
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void shouldListMyOrders() {
        var h = new HttpHeaders(); h.setBearerAuth(pubToken);
        var resp = rt.exchange(baseUrl + "/orders?role=PUBLISHER", HttpMethod.GET, new HttpEntity<>(h), Map.class);
        assertEquals(20000, resp.getBody().get("code"));
        assertNotNull(resp.getBody().get("data"));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void shouldGetOrderHistory() {
        var h = new HttpHeaders(); h.setBearerAuth(pubToken);
        var resp = rt.exchange(baseUrl + "/orders/history", HttpMethod.GET, new HttpEntity<>(h), Map.class);
        assertEquals(20000, resp.getBody().get("code"));
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void shouldRejectOrderAccessWithoutToken() {
        try {
            rt.getForObject(baseUrl + "/orders", Map.class);
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("401") || e.getMessage().contains("403") || e.getMessage().contains("Unauthorized"));
        }
    }
}
