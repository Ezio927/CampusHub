package com.example.backend.auth.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void shouldHashAndMatch() {
        String hash = PasswordHasher.hash("hello123");
        assertNotNull(hash);
        assertTrue(hash.contains(":"));
        assertTrue(PasswordHasher.matches("hello123", hash));
    }

    @Test
    void shouldRejectWrongPassword() {
        String hash = PasswordHasher.hash("correct");
        assertFalse(PasswordHasher.matches("wrong", hash));
    }

    @Test
    void shouldRejectEmptyPassword() {
        assertFalse(PasswordHasher.matches("", PasswordHasher.hash("pw")));
    }

    @Test
    void shouldGenerateDifferentHashes() {
        String h1 = PasswordHasher.hash("same");
        String h2 = PasswordHasher.hash("same");
        assertNotEquals(h1, h2);
        assertTrue(PasswordHasher.matches("same", h1));
        assertTrue(PasswordHasher.matches("same", h2));
    }
}
