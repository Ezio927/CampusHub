package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class SpaFallbackIntegrationTest {

    @LocalServerPort
    int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void shouldServeIndexForFrontendRoute() {
        String html = restTemplate.getForObject("http://localhost:" + port + "/register", String.class);

        assertTrue(html.contains("<div id=\"app\"></div>"));
        assertTrue(html.contains("/assets/index-"));
    }

    @Test
    void shouldNotForwardApiRouteToIndex() {
        try {
            restTemplate.getForObject("http://localhost:" + port + "/api/not-existing", String.class);
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            return;
        }
        throw new AssertionError("Expected API 404 response");
    }
}
