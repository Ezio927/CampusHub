package com.example.backend.auth.security;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.UserRole;
import com.example.backend.config.JwtConfig;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtTokenProvider {

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateToken(Long userId, String username, UserRole role) {
        long expiresAt = System.currentTimeMillis() + jwtConfig.getExpirationMinutes() * 60_000L;
        String payload = userId + "|" + safe(username) + "|" + role.name() + "|" + expiresAt;
        String headerPart = base64Url(HEADER_JSON);
        String payloadPart = base64Url(payload);
        String signature = sign(headerPart + "." + payloadPart);
        return headerPart + "." + payloadPart + "." + signature;
    }

    public Long parseUserId(String token) {
        String[] payload = decodePayload(token);
        return Long.valueOf(payload[0]);
    }

    public String parseUsername(String token) {
        return decodePayload(token)[1];
    }

    public UserRole parseRole(String token) {
        return UserRole.valueOf(decodePayload(token)[2].toUpperCase(Locale.ROOT));
    }

    public long parseExpiresAt(String token) {
        return Long.parseLong(decodePayload(token)[3]);
    }

    public boolean validate(String token) {
        try {
            if (!hasValidStructure(token)) {
                return false;
            }
            String[] parts = token.split("\\.");
            String expected = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
            return Long.parseLong(decodePayload(token)[3]) >= System.currentTimeMillis();
        } catch (Exception ex) {
            return false;
        }
    }

    private String[] decodePayload(String token) {
        if (!hasValidStructure(token)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        String[] parts = token.split("\\.");
        if (!verifySignature(parts[0], parts[1], parts[2])) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|", -1);
        if (values.length != 4) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        return values;
    }

    private boolean hasValidStructure(String token) {
        return token != null && !token.trim().isEmpty() && token.split("\\.").length == 3;
    }

    private boolean verifySignature(String headerPart, String payloadPart, String signaturePart) {
        String expected = sign(headerPart + "." + payloadPart);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signaturePart.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signed = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signed);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("|", "_");
    }
}
