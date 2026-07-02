package com.ufb.auth.user_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler(EmailNotRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotRegistered(EmailNotRegisteredException ex) {
        Map<String, Object> b = body(HttpStatus.NOT_FOUND, ex.getMessage());
        b.put("fields", Map.of("email", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(b);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPassword(InvalidPasswordException ex) {
        Map<String, Object> b = body(HttpStatus.UNAUTHORIZED, ex.getMessage());
        b.put("fields", Map.of("password", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(b);
    }

    @ExceptionHandler(InvalidEmailDomainException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidEmailDomain(InvalidEmailDomainException ex) {
        Map<String, Object> b = body(HttpStatus.BAD_REQUEST, ex.getMessage());
        b.put("fields", Map.of("email", ex.getMessage()));
        return ResponseEntity.badRequest().body(b);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<Map<String, Object>> handleEmailNotVerified(EmailNotVerifiedException ex) {
        Map<String, Object> b = body(HttpStatus.FORBIDDEN, ex.getMessage());
        b.put("fields", Map.of("email", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(b);
    }

    @ExceptionHandler(InvalidVerificationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidVerification(InvalidVerificationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(InvalidTwoFactorCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTwoFactorCode(InvalidTwoFactorCodeException ex) {
        Map<String, Object> b = body(HttpStatus.BAD_REQUEST, ex.getMessage());
        b.put("fields", Map.of("code", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(b);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<Map<String, Object>> handleAccountDisabled(AccountDisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(HttpStatus.FORBIDDEN, ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    @ExceptionHandler(InvalidClaimException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidClaim(InvalidClaimException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(InvalidResetException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidReset(InvalidResetException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
        Map<String, Object> b = body(HttpStatus.BAD_REQUEST, "Validation failed");
        b.put("fields", fields);
        return ResponseEntity.badRequest().body(b);
    }

    private Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> b = new HashMap<>();
        b.put("timestamp", Instant.now().toString());
        b.put("status", status.value());
        b.put("error", status.getReasonPhrase());
        b.put("message", message);
        return b;
    }
}
