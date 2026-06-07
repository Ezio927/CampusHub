package com.example.backend.auth.security;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts per username to block brute-force attacks.
 * After {@link #MAX_FAILURES} failures within {@link #WINDOW_MINUTES} minutes,
 * further attempts are rejected until the window passes.
 */
public final class LoginRateLimiter {

    private static final int MAX_FAILURES = 5;
    private static final int WINDOW_MINUTES = 15;

    private final Map<String, List<Instant>> failures = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        List<Instant> attempts = failures.get(username.toLowerCase());
        if (attempts == null) {
            return false;
        }
        Instant cutoff = Instant.now().minusSeconds(WINDOW_MINUTES * 60L);
        synchronized (attempts) {
            attempts.removeIf(ts -> ts.isBefore(cutoff));
            if (attempts.isEmpty()) {
                failures.remove(username.toLowerCase());
                return false;
            }
            return attempts.size() >= MAX_FAILURES;
        }
    }

    public void recordFailure(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        String key = username.toLowerCase();
        List<Instant> attempts = failures.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (attempts) {
            attempts.add(Instant.now());
        }
    }

    public void clear(String username) {
        if (username != null) {
            failures.remove(username.toLowerCase());
        }
    }
}
