package com.ufb.auth.user_management.dto;

public record LoginResponse(
        boolean twoFactorRequired,
        AuthResponse auth
) {}
