package com.example.backend.auth.security;

import com.example.backend.common.BusinessException;
import com.example.backend.common.enums.UserRole;
import com.example.backend.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setSecret("test-secret-key-for-unit-tests-123");
        config.setExpirationMinutes(60);
        provider = new JwtTokenProvider(config);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = provider.generateToken(1L, "alice", UserRole.STUDENT);
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
        assertTrue(provider.validate(token));
    }

    @Test
    void shouldParseTokenFields() {
        String token = provider.generateToken(42L, "bob", UserRole.ADMIN);
        assertEquals(42L, provider.parseUserId(token));
        assertEquals("bob", provider.parseUsername(token));
        assertEquals(UserRole.ADMIN, provider.parseRole(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(provider.validate(null));
        assertFalse(provider.validate(""));
        assertFalse(provider.validate("invalid.token"));
        assertFalse(provider.validate("a.b.c"));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = provider.generateToken(1L, "alice", UserRole.STUDENT);
        String tampered = token.substring(0, token.length() - 1) + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');
        assertFalse(provider.validate(tampered));
    }

    @Test
    void shouldThrowForInvalidTokenParsing() {
        assertThrows(BusinessException.class, () -> provider.parseUserId("bad.token.here"));
    }
}
