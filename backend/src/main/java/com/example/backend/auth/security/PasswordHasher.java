package com.example.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PBKDF2_PREFIX = "pbkdf2";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        String salt = randomSalt();
        return PBKDF2_PREFIX + ":" + ITERATIONS + ":" + salt + ":" + pbkdf2(rawPassword, salt, ITERATIONS);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        String[] parts = encodedPassword.split(":");
        if (parts.length == 4 && PBKDF2_PREFIX.equals(parts[0])) {
            int iterations;
            try {
                iterations = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                return false;
            }
            String expected = pbkdf2(rawPassword, parts[2], iterations);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    parts[3].getBytes(StandardCharsets.UTF_8));
        }
        if (parts.length != 2 || parts[0].isEmpty()) {
            return false;
        }
        String expected = sha256(parts[0] + ":" + rawPassword);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                parts[1].getBytes(StandardCharsets.UTF_8));
    }

    private static String randomSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String pbkdf2(String rawPassword, String salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(),
                    salt.getBytes(StandardCharsets.UTF_8), iterations, KEY_LENGTH);
            byte[] encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encoded);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 unavailable", e);
        }
    }
}
