package com.ufb.auth.user_management.exception;

public class InvalidClaimException extends RuntimeException {
    public InvalidClaimException() {
        super("Invalid, expired, or already-used claim token");
    }
}
