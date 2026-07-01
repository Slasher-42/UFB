package com.ufb.auth.user_management.security;

/**
 * Delivers a freshly generated one-time claim token to the account owner.
 * Current implementation writes to a restricted local file (dev).
 * Swap for an email-based implementation once notification-service exists —
 * no other code needs to change.
 */
public interface ClaimTokenNotifier {
    void deliver(String recipientEmail, String rawClaimToken, java.time.Instant expiresAt);
}
