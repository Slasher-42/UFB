package com.ufb.auth.user_management.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Nullable: a seeded-but-unclaimed admin has no password until they claim the account
    @Column(nullable = true)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    // False until the account owner has set their own password (true for normal signups)
    @Column(name = "password_set", nullable = false)
    private boolean passwordSet;

    // SHA-256 hash of the one-time claim token; null once claimed or for normal users
    @Column(name = "claim_token_hash")
    private String claimTokenHash;

    // When the claim token stops being valid; null once claimed or for normal users
    @Column(name = "claim_token_expires_at")
    private Instant claimTokenExpiresAt;

    // SHA-256 hash of the one-time password reset token; null unless a reset is in flight
    @Column(name = "reset_token_hash")
    private String resetTokenHash;

    // When the reset token stops being valid; null unless a reset is in flight
    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;

    // True once the account owner has confirmed they control this mailbox.
    // Self-registered users start false; claimed/admin-created accounts start true
    // because receiving and using a claim token already proves mailbox ownership.
    // DB default is true so this migration doesn't retroactively lock out accounts
    // that already existed (and could already log in) before this column was added.
    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean not null default true")
    private boolean emailVerified;

    // SHA-256 hash of the one-time email verification token; null once verified
    @Column(name = "verification_token_hash")
    private String verificationTokenHash;

    // When the verification token stops being valid; null once verified
    @Column(name = "verification_token_expires_at")
    private Instant verificationTokenExpiresAt;

    // True once the account owner has completed the one-time 2FA email-code
    // challenge on their first login. False for every new account; once true,
    // later logins skip the 2FA code step for good.
    // DB default is false so adding this column doesn't fail on a table that
    // already has rows (same reasoning as email_verified above).
    @Column(name = "two_factor_verified", nullable = false, columnDefinition = "boolean not null default false")
    private boolean twoFactorVerified;

    // SHA-256 hash of the current 2FA login code; null unless a challenge is in flight
    @Column(name = "two_factor_code_hash")
    private String twoFactorCodeHash;

    // When the 2FA login code stops being valid; null unless a challenge is in flight
    @Column(name = "two_factor_code_expires_at")
    private Instant twoFactorCodeExpiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) this.role = Role.USER;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
