package com.ufb.auth.user_management.service;

import com.ufb.auth.user_management.dto.AuthResponse;
import com.ufb.auth.user_management.dto.ClaimAccountRequest;
import com.ufb.auth.user_management.dto.CreateAdminRequest;
import com.ufb.auth.user_management.dto.LoginRequest;
import com.ufb.auth.user_management.dto.LoginResponse;
import com.ufb.auth.user_management.dto.RegisterRequest;
import com.ufb.auth.user_management.dto.ResetPasswordRequest;
import com.ufb.auth.user_management.dto.UserResponse;
import com.ufb.auth.user_management.dto.VerifyEmailRequest;
import com.ufb.auth.user_management.dto.VerifyTwoFactorRequest;
import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    AuthResponse verifyTwoFactor(VerifyTwoFactorRequest request);
    void resendTwoFactor(String email);
    AuthResponse refresh(String refreshToken);
    AuthResponse claim(ClaimAccountRequest request);
    boolean bootstrapAdminNeedsClaim();
    void forgotPassword(String email);
    AuthResponse resetPassword(ResetPasswordRequest request);
    AuthResponse verifyEmail(VerifyEmailRequest request);
    void resendVerification(String email);
    List<UserResponse> listUsers();
    UserResponse setEnabled(Long userId, boolean enabled);
    void deleteUser(Long userId);
    UserResponse createAdmin(CreateAdminRequest request);
}
