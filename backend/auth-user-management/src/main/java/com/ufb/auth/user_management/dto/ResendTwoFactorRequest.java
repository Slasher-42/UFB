package com.ufb.auth.user_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendTwoFactorRequest(
        @NotBlank @Email String email
) {}
