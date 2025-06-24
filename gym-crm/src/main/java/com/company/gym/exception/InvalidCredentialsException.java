package com.company.gym.exception;

public class InvalidCredentialsException extends SecurityException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
