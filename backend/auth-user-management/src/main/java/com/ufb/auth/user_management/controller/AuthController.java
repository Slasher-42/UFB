package com.ufb.auth.user_management.controller;

import com.ufb.auth.user_management.dto.AuthResponse;
import com.ufb.auth.user_management.dto.ClaimAccountRequest;
import com.ufb.auth.user_management.dto.LoginRequest;
import com.ufb.auth.user_management.dto.RefreshRequest;
import com.ufb.auth.user_management.dto.RegisterRequest;
import com.ufb.auth.user_management.dto.UserResponse;
import com.ufb.auth.user_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(userService.refresh(request.refreshToken()));
    }

    @PostMapping("/claim")
    public ResponseEntity<AuthResponse> claim(@Valid @RequestBody ClaimAccountRequest request) {
        return ResponseEntity.ok(userService.claim(request));
    }
}
