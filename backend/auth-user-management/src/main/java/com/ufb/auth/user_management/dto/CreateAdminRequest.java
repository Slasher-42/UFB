package com.ufb.auth.user_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAdminRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 2, max = 120) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password
) {}
