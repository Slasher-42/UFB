package com.ufb.auth.user_management.service.impl;

import com.ufb.auth.user_management.dto.AuthResponse;
import com.ufb.auth.user_management.dto.LoginRequest;
import com.ufb.auth.user_management.dto.LoginResponse;
import com.ufb.auth.user_management.dto.RegisterRequest;
import com.ufb.auth.user_management.dto.ResetPasswordRequest;
import com.ufb.auth.user_management.dto.UserResponse;
import com.ufb.auth.user_management.dto.VerifyTwoFactorRequest;
import com.ufb.auth.user_management.exception.AccountDisabledException;
import com.ufb.auth.user_management.exception.EmailAlreadyExistsException;
import com.ufb.auth.user_management.exception.EmailNotRegisteredException;
import com.ufb.auth.user_management.exception.EmailNotVerifiedException;
import com.ufb.auth.user_management.exception.InvalidEmailDomainException;
import com.ufb.auth.user_management.exception.InvalidPasswordException;
import com.ufb.auth.user_management.exception.InvalidTwoFactorCodeException;
import com.ufb.auth.user_management.exception.InvalidVerificationException;
import com.ufb.auth.user_management.dto.ClaimAccountRequest;
import com.ufb.auth.user_management.dto.CreateAdminRequest;
import com.ufb.auth.user_management.dto.VerifyEmailRequest;
import com.ufb.auth.user_management.exception.InvalidResetException;
import com.ufb.auth.user_management.exception.UserNotFoundException;
import com.ufb.auth.user_management.validation.EmailDomainValidator;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.ufb.auth.user_management.exception.InvalidClaimException;
import com.ufb.auth.user_management.exception.InvalidTokenException;
import com.ufb.auth.user_management.security.AccountVerificationNotifier;
import com.ufb.auth.user_management.security.OneTimeTokens;
import com.ufb.auth.user_management.security.PasswordResetNotifier;
import com.ufb.auth.user_management.security.TokenHasher;
import com.ufb.auth.user_management.security.TwoFactorCodeNotifier;
import com.ufb.auth.user_management.security.TwoFactorCodes;
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
    private final PasswordResetNotifier passwordResetNotifier;
    private final AccountVerificationNotifier accountVerificationNotifier;
    private final TwoFactorCodeNotifier twoFactorCodeNotifier;
    private final EmailDomainValidator emailDomainValidator;

    @Value("${ufb.admin.email}")
    private String bootstrapAdminEmail;

    @Value("${ufb.password-reset.expiry-hours}")
    private long resetTokenExpiryHours;

    @Value("${ufb.verification.expiry-hours}")
    private long verificationTokenExpiryHours;

    @Value("${ufb.two-factor.expiry-minutes}")
    private long twoFactorCodeExpiryMinutes;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!emailDomainValidator.domainAcceptsMail(request.email())) {
            throw new InvalidEmailDomainException();
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        String rawToken = OneTimeTokens.generate();
        Instant expiresAt = Instant.now().plus(verificationTokenExpiryHours, ChronoUnit.HOURS);

        User user = User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .passwordSet(true)
                .emailVerified(false)
                .verificationTokenHash(TokenHasher.sha256(rawToken))
                .verificationTokenExpiresAt(expiresAt)
                .build();
        User saved = userRepository.save(user);
        eventPublisher.publishRegistered(saved);
        accountVerificationNotifier.deliver(saved.getEmail(), rawToken, expiresAt);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (!emailDomainValidator.domainAcceptsMail(request.email())) {
            throw new InvalidEmailDomainException();
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(EmailNotRegisteredException::new);

        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }

        if (!user.isPasswordSet() || user.getPassword() == null
                || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (!user.isTwoFactorVerified()) {
            String rawCode = TwoFactorCodes.generate();
            Instant expiresAt = Instant.now().plus(twoFactorCodeExpiryMinutes, ChronoUnit.MINUTES);

            user.setTwoFactorCodeHash(TokenHasher.sha256(rawCode));
            user.setTwoFactorCodeExpiresAt(expiresAt);
            userRepository.save(user);

            twoFactorCodeNotifier.deliver(user.getEmail(), rawCode, expiresAt);
            return new LoginResponse(true, null);
        }

        return new LoginResponse(false, new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                toResponse(user)
        ));
    }

    @Override
    @Transactional
    public AuthResponse verifyTwoFactor(VerifyTwoFactorRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidTwoFactorCodeException::new);

        if (user.getTwoFactorCodeHash() == null || user.getTwoFactorCodeExpiresAt() == null) {
            throw new InvalidTwoFactorCodeException();
        }

        if (Instant.now().isAfter(user.getTwoFactorCodeExpiresAt())) {
            throw new InvalidTwoFactorCodeException();
        }

        String incomingHash = TokenHasher.sha256(request.code());
        if (!constantTimeEquals(incomingHash, user.getTwoFactorCodeHash())) {
            throw new InvalidTwoFactorCodeException();
        }

        user.setTwoFactorVerified(true);
        user.setTwoFactorCodeHash(null);
        user.setTwoFactorCodeExpiresAt(null);
        User saved = userRepository.save(user);

        return new AuthResponse(
                jwtService.generateAccessToken(saved),
                jwtService.generateRefreshToken(saved),
                toResponse(saved)
        );
    }

    @Override
    @Transactional
    public void resendTwoFactor(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isTwoFactorVerified() || !user.isPasswordSet() || !user.isEnabled()) {
                return;
            }

            String rawCode = TwoFactorCodes.generate();
            Instant expiresAt = Instant.now().plus(twoFactorCodeExpiryMinutes, ChronoUnit.MINUTES);

            user.setTwoFactorCodeHash(TokenHasher.sha256(rawCode));
            user.setTwoFactorCodeExpiresAt(expiresAt);
            userRepository.save(user);

            twoFactorCodeNotifier.deliver(user.getEmail(), rawCode, expiresAt);
        });
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
        user.setEmailVerified(true);
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
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isPasswordSet() || !user.isEnabled()) {
                return;
            }

            String rawToken = OneTimeTokens.generate();
            Instant expiresAt = Instant.now().plus(resetTokenExpiryHours, ChronoUnit.HOURS);

            user.setResetTokenHash(TokenHasher.sha256(rawToken));
            user.setResetTokenExpiresAt(expiresAt);
            userRepository.save(user);

            passwordResetNotifier.deliver(user.getEmail(), rawToken, expiresAt);
        });
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidResetException::new);

        if (user.getResetTokenHash() == null || user.getResetTokenExpiresAt() == null) {
            throw new InvalidResetException();
        }

        if (Instant.now().isAfter(user.getResetTokenExpiresAt())) {
            throw new InvalidResetException();
        }

        String incomingHash = TokenHasher.sha256(request.resetToken());
        if (!constantTimeEquals(incomingHash, user.getResetTokenHash())) {
            throw new InvalidResetException();
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetTokenHash(null);
        user.setResetTokenExpiresAt(null);
        User saved = userRepository.save(user);

        return new AuthResponse(
                jwtService.generateAccessToken(saved),
                jwtService.generateRefreshToken(saved),
                toResponse(saved)
        );
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidVerificationException::new);

        if (user.isEmailVerified()) {
            throw new InvalidVerificationException();
        }

        if (user.getVerificationTokenHash() == null || user.getVerificationTokenExpiresAt() == null) {
            throw new InvalidVerificationException();
        }

        if (Instant.now().isAfter(user.getVerificationTokenExpiresAt())) {
            throw new InvalidVerificationException();
        }

        String incomingHash = TokenHasher.sha256(request.verificationToken());
        if (!constantTimeEquals(incomingHash, user.getVerificationTokenHash())) {
            throw new InvalidVerificationException();
        }

        user.setEmailVerified(true);
        user.setVerificationTokenHash(null);
        user.setVerificationTokenExpiresAt(null);
        User saved = userRepository.save(user);

        return new AuthResponse(
                jwtService.generateAccessToken(saved),
                jwtService.generateRefreshToken(saved),
                toResponse(saved)
        );
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isEmailVerified() || !user.isPasswordSet() || !user.isEnabled()) {
                return;
            }

            String rawToken = OneTimeTokens.generate();
            Instant expiresAt = Instant.now().plus(verificationTokenExpiryHours, ChronoUnit.HOURS);

            user.setVerificationTokenHash(TokenHasher.sha256(rawToken));
            user.setVerificationTokenExpiresAt(expiresAt);
            userRepository.save(user);

            accountVerificationNotifier.deliver(user.getEmail(), rawToken, expiresAt);
        });
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
                .emailVerified(true)
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
