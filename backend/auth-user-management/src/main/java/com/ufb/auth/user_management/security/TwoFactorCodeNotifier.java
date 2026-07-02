package com.ufb.auth.user_management.security;

import java.time.Instant;

/**
 * Delivers a freshly generated one-time 2FA login code to the account owner.
 */
public interface TwoFactorCodeNotifier {
    void deliver(String recipientEmail, String rawCode, Instant expiresAt);
}
