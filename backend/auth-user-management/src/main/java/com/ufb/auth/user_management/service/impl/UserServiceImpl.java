package com.ufb.auth.user_management.service.impl;

import com.ufb.auth.user_management.dto.AuthResponse;
import com.ufb.auth.user_management.dto.LoginRequest;
import com.ufb.auth.user_management.dto.RegisterRequest;
import com.ufb.auth.user_management.dto.UserResponse;
import com.ufb.auth.user_management.exception.AccountDisabledException;
import com.ufb.auth.user_management.exception.EmailAlreadyExistsException;
import com.ufb.auth.user_management.exception.InvalidCredentialsException;
import com.ufb.auth.user_management.dto.ClaimAccountRequest;
import com.ufb.auth.user_management.dto.CreateAdminRequest;
import com.ufb.auth.user_management.exception.UserNotFoundException;
import java.util.List;
import com.ufb.auth.user_management.exception.InvalidClaimException;
import com.ufb.auth.user_management.exception.InvalidTokenException;
import com.ufb.auth.user_management.security.TokenHasher;
import java.time.Instant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import com.ufb.auth.user_management.model.Role;
import com.ufb.auth.user_management.model.User;
import com.ufb.auth.user_management.repository.UserRepository;
import com.ufb.auth.user_management.event.UserEventPublisher;
import com.ufb.auth.user_management.security.JwtService;
import com.ufb.auth.user_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventPublisher eventPublisher;

    @Value("${ufb.admin.email}")
    private String bootstrapAdminEmail;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .passwordSet(true)
                .build();
        User saved = userRepository.save(user);
        eventPublisher.publishRegistered(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isPasswordSet() || user.getPassword() == null) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                toResponse(user)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        final Claims claims;
        try {
            claims = jwtService.parse(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException();
        }

        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new InvalidTokenException();
        }

        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidTokenException::new);

        if (!user.isEnabled()) {
            throw new InvalidTokenException();
        }

        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                toResponse(user)
        );
    }

    @Override
    @Transactional
    public AuthResponse claim(ClaimAccountRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidClaimException::new);

        if (user.isPasswordSet()) {
            throw new InvalidClaimException();
        }

        if (user.getClaimTokenHash() == null || user.getClaimTokenExpiresAt() == null) {
            throw new InvalidClaimException();
        }

        if (Instant.now().isAfter(user.getClaimTokenExpiresAt())) {
            throw new InvalidClaimException();
        }

        String incomingHash = TokenHasher.sha256(request.claimToken());
        if (!constantTimeEquals(incomingHash, user.getClaimTokenHash())) {
            throw new InvalidClaimException();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordSet(true);
        user.setClaimTokenHash(null);
        user.setClaimTokenExpiresAt(null);
        User saved = userRepository.save(user);
        eventPublisher.publishRegistered(saved);

        return new AuthResponse(
                jwtService.generateAccessToken(saved),
                jwtService.generateRefreshToken(saved),
                toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean bootstrapAdminNeedsClaim() {
        return userRepository.findByEmail(bootstrapAdminEmail)
                .map(u -> !u.isPasswordSet())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        eventPublisher.publishUpdated(saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.delete(user);
        eventPublisher.publishDeleted(user);
    }

    @Override
    @Transactional
    public UserResponse createAdmin(CreateAdminRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User admin = User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .enabled(true)
                .passwordSet(true)
                .build();
        User saved = userRepository.save(admin);
        eventPublisher.publishRegistered(saved);
        return toResponse(saved);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(),
                u.getRole(), u.isEnabled(), u.getCreatedAt());
    }
}
