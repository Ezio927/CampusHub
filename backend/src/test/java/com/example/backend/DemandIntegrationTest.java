package com.example.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemandIntegrationTest {

    @LocalServerPort int port;
    final RestTemplate rt = new RestTemplate();
    final ObjectMapper om = new ObjectMapper();
    String baseUrl;
    String token;
    Long demandId;

    @SuppressWarnings("unchecked")
    @BeforeAll
    void setUp() throws Exception {
        baseUrl = "http://localhost:" + port + "/api";
        var resp = rt.postForObject(baseUrl + "/auth/login",
                Map.of("username","1","password","1"), Map.class);
        token = (String) ((Map<?,?>)resp.get("data")).get("token");
    }

    private HttpEntity<String> authJson(Map<String, Object> body) throws Exception {
        var h = new HttpHeaders(); h.setBearerAuth(token);
        h.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(om.writeValueAsString(body), h);
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.junit.jupiter.api.Order(1)
    void shouldCreateDemand() throws Exception {
        var req = new HashMap<String, Object>();
        req.put("title", "Test demand");
        req.put("description", "Need someone to help with a task");
        req.put("category", "ERRAND");
        req.put("rewardAmount", 3);
        req.put("rewardType", "MONEY");
        req.put("locationTag", "仙林校区");
        req.put("campus", "仙林校区");
        var resp = rt.exchange(baseUrl + "/demands", HttpMethod.POST, authJson(req), Map.class);
        assertEquals(20000, resp.getBody().get("code"));
        demandId = ((Number)((Map<?,?>)resp.getBody().get("data")).get("id")).longValue();
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void shouldRejectEmptyTitle() throws Exception {
        Thread.sleep(12000); // rate limit min interval
        try {
            rt.exchange(baseUrl + "/demands", HttpMethod.POST,
                    authJson(Map.of("title","","description","x","category","ERRAND","rewardAmount",3,"rewardType","MONEY")), Map.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertTrue(e.getStatusCode().is4xxClientError(),
                    "Expected 4xx, got " + e.getStatusCode());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.junit.jupiter.api.Order(3)
    void shouldSearchDemands() {
        var resp = rt.getForObject(baseUrl + "/demands?category=ERRAND", Map.class);
        assertEquals(20000, resp.get("code"));
        var content = (List<?>)((Map<?,?>)resp.get("data")).get("content");
        assertFalse(content.isEmpty());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void shouldGetDemandDetail() {
        var resp = rt.getForObject(baseUrl + "/demands/" + demandId, Map.class);
        assertEquals(20000, resp.get("code"));
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    void shouldReturnErrorForMissingDemand() {
        try {
            rt.getForObject(baseUrl + "/demands/99999", Map.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertTrue(e.getStatusCode().is4xxClientError());
        }
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    void shouldCancelDemand() throws Exception {
        var resp = rt.exchange(baseUrl + "/demands/" + demandId + "/cancel",
                HttpMethod.PUT, authJson(Map.of()), Map.class);
        assertEquals(20000, resp.getBody().get("code"));
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    void shouldNotCancelTwice() throws Exception {
        try {
            rt.exchange(baseUrl + "/demands/" + demandId + "/cancel",
                    HttpMethod.PUT, authJson(Map.of()), Map.class);
            fail("Expected exception");
        } catch (HttpClientErrorException e) {
            assertTrue(e.getStatusCode().is4xxClientError());
        }
    }
}
