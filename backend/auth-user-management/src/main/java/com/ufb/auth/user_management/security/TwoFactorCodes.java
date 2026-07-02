package com.ufb.auth.user_management.security;

import java.security.SecureRandom;

/**
 * Generates the 6-digit numeric codes emailed for the one-time 2FA login challenge.
 */
public final class TwoFactorCodes {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TwoFactorCodes() {}

    public static String generate() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
