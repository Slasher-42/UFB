package com.ufb.auth.user_management.service;

import com.ufb.auth.user_management.dto.AuthResponse;
import com.ufb.auth.user_management.dto.ClaimAccountRequest;
import com.ufb.auth.user_management.dto.CreateAdminRequest;
import com.ufb.auth.user_management.dto.LoginRequest;
import com.ufb.auth.user_management.dto.RegisterRequest;
import com.ufb.auth.user_management.dto.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    AuthResponse claim(ClaimAccountRequest request);
    boolean bootstrapAdminNeedsClaim();
    List<UserResponse> listUsers();
    UserResponse setEnabled(Long userId, boolean enabled);
    void deleteUser(Long userId);
    UserResponse createAdmin(CreateAdminRequest request);
}
