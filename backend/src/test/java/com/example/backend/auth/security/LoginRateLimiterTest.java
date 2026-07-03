package com.example.backend.auth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {

    @Test
    void shouldBlockAfterFiveFailures() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        String username = "alice";

        for (int i = 0; i < 4; i++) {
            limiter.recordFailure(username);
            assertFalse(limiter.isBlocked(username));
        }

        limiter.recordFailure(username);

        assertTrue(limiter.isBlocked(username));
    }

    @Test
    void shouldNormalizeUsernameCaseAndClearFailures() {
        LoginRateLimiter limiter = new LoginRateLimiter();

        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("Alice");
        }

        assertTrue(limiter.isBlocked("alice"));
        limiter.clear("ALICE");
        assertFalse(limiter.isBlocked("alice"));
    }
}
