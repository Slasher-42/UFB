package com.ufb.auth.user_management.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {}
