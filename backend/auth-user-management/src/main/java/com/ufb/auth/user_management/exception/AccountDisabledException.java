package com.ufb.auth.user_management.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException() {
        super("Your account has been disabled. Please contact support.");
    }
}
