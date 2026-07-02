package com.ufb.auth.user_management.exception;

public class InvalidTwoFactorCodeException extends RuntimeException {
    public InvalidTwoFactorCodeException() {
        super("Invalid or expired verification code");
    }
}
