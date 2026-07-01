package com.ufb.auth.user_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimAccountRequest(
        @NotBlank @Email String email,
        @NotBlank String claimToken,
        @NotBlank @Size(min = 8, max = 100) String newPassword
) {}
