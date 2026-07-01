package com.ufb.auth.user_management.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashes claim tokens with SHA-256 so the raw token is never stored.
 * The same method is used at seed time and at claim time, so the
 * stored hash and the incoming token's hash can be compared.
 */
public final class TokenHasher {

    private TokenHasher() {}

    public static String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present in every JVM; this never happens
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
