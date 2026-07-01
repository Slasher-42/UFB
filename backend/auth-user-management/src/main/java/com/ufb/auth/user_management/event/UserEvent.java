package com.ufb.auth.user_management.event;

import com.ufb.auth.user_management.model.Role;

import java.time.Instant;

/**
 * Event published when a user is created, updated, or deleted.
 * Shape is a contract with downstream services — change carefully.
 */
public record UserEvent(
        String eventType,   // "user.registered" | "user.updated" | "user.deleted"
        Long userId,
        String email,
        String fullName,
        Role role,
        boolean enabled,
        Instant occurredAt
) {}
