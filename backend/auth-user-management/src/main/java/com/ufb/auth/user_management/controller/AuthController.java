package com.ufb.auth.user_management.controller;

import com.ufb.auth.user_management.dto.AuthResponse;
import com.ufb.auth.user_management.dto.ClaimAccountRequest;
import com.ufb.auth.user_management.dto.ClaimStatusResponse;
import com.ufb.auth.user_management.dto.EmailCheckResponse;
import com.ufb.auth.user_management.dto.ForgotPasswordRequest;
import com.ufb.auth.user_management.dto.LoginRequest;
import com.ufb.auth.user_management.dto.LoginResponse;
import com.ufb.auth.user_management.dto.RefreshRequest;
import com.ufb.auth.user_management.dto.RegisterRequest;
import com.ufb.auth.user_management.dto.ResendTwoFactorRequest;
import com.ufb.auth.user_management.dto.ResendVerificationRequest;
import com.ufb.auth.user_management.dto.ResetPasswordRequest;
import com.ufb.auth.user_management.dto.UserResponse;
import com.ufb.auth.user_management.dto.VerifyEmailRequest;
import com.ufb.auth.user_management.dto.VerifyTwoFactorRequest;
import com.ufb.auth.user_management.service.UserService;
import com.ufb.auth.user_management.validation.EmailDomainValidator;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailDomainValidator emailDomainValidator;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<AuthResponse> verifyTwoFactor(@Valid @RequestBody VerifyTwoFactorRequest request) {
        return ResponseEntity.ok(userService.verifyTwoFactor(request));
    }

    @PostMapping("/resend-2fa")
    public ResponseEntity<Map<String, String>> resendTwoFactor(@Valid @RequestBody ResendTwoFactorRequest request) {
        userService.resendTwoFactor(request.email());
        return ResponseEntity.ok(Map.of("message", "If that account needs a code, a new one has been sent."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(userService.refresh(request.refreshToken()));
    }

    @PostMapping("/claim")
    public ResponseEntity<AuthResponse> claim(@Valid @RequestBody ClaimAccountRequest request) {
        return ResponseEntity.ok(userService.claim(request));
    }

    @GetMapping("/claim-status")
    public ResponseEntity<ClaimStatusResponse> claimStatus() {
        return ResponseEntity.ok(new ClaimStatusResponse(userService.bootstrapAdminNeedsClaim()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    @GetMapping("/email-check")
    public ResponseEntity<EmailCheckResponse> emailCheck(@RequestParam String email) {
        return ResponseEntity.ok(new EmailCheckResponse(emailDomainValidator.domainAcceptsMail(email)));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(userService.verifyEmail(request));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        userService.resendVerification(request.email());
        return ResponseEntity.ok(Map.of("message", "If that email needs verification, a new link has been sent."));
    }
}
